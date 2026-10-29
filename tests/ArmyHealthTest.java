import entity.Team;
import entity.troops.Troop;
import entity.troops.melee.Infantry;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collector;
import java.util.stream.IntStream;
import javax.swing.SwingUtilities;
import view.ArmyHealthCollector;
import view.ArmyHealthMonitor;
import view.ArmyHealthSummary;
import view.GroupHealthSnapshot;

public class ArmyHealthTest {
    public static void main(String[] args) throws Exception {
        checkThresholdsAndTotals();
        checkParallelResults();
        checkSnapshotIsolation();
        checkLatestRequestAfterRestart();
        checkEventThreadRequired();
        System.out.println("Army health collector and background-worker checks passed.");
    }

    private static void checkThresholdsAndTotals() {
        List<GroupHealthSnapshot> groups = List.of(
                new GroupHealthSnapshot(71, 100, 1),
                new GroupHealthSnapshot(710, 1000, 8),
                new GroupHealthSnapshot(70, 100, 1),
                new GroupHealthSnapshot(30, 100, 1),
                new GroupHealthSnapshot(300, 1000, 3),
                new GroupHealthSnapshot(29, 100, 1),
                new GroupHealthSnapshot(290, 1000, 3),
                new GroupHealthSnapshot(0, 100, 0));
        ArmyHealthSummary summary = groups.stream().collect(new ArmyHealthCollector());
        check(summary.equals(new ArmyHealthSummary(2, 3, 2, 1, 1500, 3500, 18)),
                "Thresholds, group sizes or health totals are incorrect");
        check(summary.groupCount() == 8 && summary.livingGroups() == 7, "Defeated groups count as living");
        check(Math.abs(summary.healthPercent() - 100.0 * 1500 / 3500) < 0.000001,
                "Army health must use total HP / starting HP, including defeated groups");
        ArmyHealthSummary empty = List.<GroupHealthSnapshot>of().parallelStream().collect(new ArmyHealthCollector());
        check(empty.equals(ArmyHealthSummary.empty()) && empty.healthPercent() == 0,
                "An empty army needs a valid zero summary");

        ArmyHealthSummary large = List.of(new GroupHealthSnapshot(Integer.MAX_VALUE, Integer.MAX_VALUE, 1),
                new GroupHealthSnapshot(Integer.MAX_VALUE, Integer.MAX_VALUE, 1)).stream()
                .collect(new ArmyHealthCollector());
        check(large.totalHealth() == 2L * Integer.MAX_VALUE && large.healthy() == 2,
                "Health sums and percentage comparisons must not overflow int");
    }

    private static void checkParallelResults() {
        List<GroupHealthSnapshot> groups = IntStream.range(0, 10_003)
                .mapToObj(i -> new GroupHealthSnapshot(i % 101, 100, (i % 101 + 9) / 10)).toList();
        ArmyHealthSummary expected = groups.stream().collect(new ArmyHealthCollector());
        ArmyHealthCollector collector = new ArmyHealthCollector();
        for (int repetition = 0; repetition < 12; repetition++) {
            check(groups.parallelStream().collect(collector).equals(expected),
                    "Parallel combination lost counts or health");
        }
        check(!collector.characteristics().contains(Collector.Characteristics.CONCURRENT)
                        && !collector.characteristics().contains(Collector.Characteristics.IDENTITY_FINISH),
                "Mutable accumulators must not be shared, and the finisher creates an immutable result");
        var first = collector.supplier().get();
        var second = collector.supplier().get();
        collector.accumulator().accept(first, new GroupHealthSnapshot(100, 100, 1));
        check(collector.finisher().apply(second).equals(ArmyHealthSummary.empty()),
                "Stream partitions must receive separate accumulators");
        check(collector.finisher().apply(collector.combiner().apply(first, second))
                        .equals(new ArmyHealthSummary(1, 0, 0, 0, 100, 100, 1)),
                "Combining an empty partition must preserve the result");
    }

    private static void checkSnapshotIsolation() throws Exception {
        Team a = new Team("teamA");
        Team b = new Team("teamB");
        Infantry troop = new Infantry(0, 0, 3, Troop.Team.teamA);
        a.addTroop(troop);
        ArmyHealthSummary expected = List.of(GroupHealthSnapshot.from(troop)).stream()
                .collect(new ArmyHealthCollector());
        CompletableFuture<ArmyHealthMonitor.Summaries> result = new CompletableFuture<>();
        SwingUtilities.invokeAndWait(() -> {
            ArmyHealthMonitor monitor = new ArmyHealthMonitor(summaries -> {
                if (!SwingUtilities.isEventDispatchThread()) {
                    result.completeExceptionally(new AssertionError("UI callback ran off the Swing event thread"));
                } else {
                    result.complete(summaries);
                }
            });
            monitor.request(a, b);
            // The worker must still see the copied values, not this mutated troop/list.
            troop.takeDamage(10_000);
            a.getArmy().clear();
        });
        var summaries = result.get(10, TimeUnit.SECONDS);
        check(summaries.teamA().equals(expected) && summaries.teamB().equals(ArmyHealthSummary.empty()),
                "Background processing read mutable troops instead of snapshots");
    }

    private static void checkLatestRequestAfterRestart() throws Exception {
        Team old = new Team("old round");
        old.addTroop(new Infantry(0, 0, 10, Troop.Team.teamA));
        Team freshA = new Team("fresh A");
        Team freshB = new Team("fresh B");
        CompletableFuture<ArmyHealthMonitor.Summaries> result = new CompletableFuture<>();
        AtomicInteger callbacks = new AtomicInteger();
        SwingUtilities.invokeAndWait(() -> {
            ArmyHealthMonitor monitor = new ArmyHealthMonitor(summaries -> {
                callbacks.incrementAndGet();
                result.complete(summaries);
            });
            // done() cannot run until this event finishes, so the old result is pending.
            monitor.request(old, freshB);
            monitor.reset();
            monitor.request(freshA, freshB);
            freshB.addTroop(new Infantry(0, 20, 2, Troop.Team.teamB));
            monitor.request(freshA, freshB); // supersedes the waiting empty snapshot
        });
        var summaries = result.get(10, TimeUnit.SECONDS);
        check(callbacks.get() == 1 && summaries.teamA().groupCount() == 0
                        && summaries.teamB().healthy() == 1 && summaries.teamB().soldiers() == 2,
                "A restarted round or newer request was overwritten by an old worker");
    }

    private static void checkEventThreadRequired() {
        ArmyHealthMonitor monitor = new ArmyHealthMonitor(ignored -> { });
        try {
            monitor.request(new Team("A"), new Team("B"));
            throw new AssertionError("Off-thread snapshot capture was accepted");
        } catch (IllegalStateException expected) {
            // Troops and Swing state are owned by the event thread.
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
