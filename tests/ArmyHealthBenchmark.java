import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;
import view.ArmyHealthCollector;
import view.ArmyHealthSummary;
import view.GroupHealthSnapshot;

/* A small timing experiment, not a performance guarantee. Snapshot creation is excluded. */
public class ArmyHealthBenchmark {
    private static volatile ArmyHealthSummary result;

    public static void main(String[] args) {
        System.out.println("Aggregation only; median of 7 runs after 5 warm-up runs.");
        System.out.println("Processors: " + Runtime.getRuntime().availableProcessors());
        for (int count : new int[] { 50, 200_000 }) {
            List<GroupHealthSnapshot> groups = IntStream.range(0, count)
                    .mapToObj(i -> new GroupHealthSnapshot(i % 101, 100, (i % 101 + 9) / 10)).toList();
            ArmyHealthSummary expected = groups.stream().collect(new ArmyHealthCollector());
            for (int i = 0; i < 5; i++) {
                measure(groups, false, expected);
                measure(groups, true, expected);
            }
            long[] sequential = new long[7];
            long[] parallel = new long[7];
            for (int i = 0; i < 7; i++) {
                // Alternate the order to reduce the advantage of always running second.
                if (i % 2 == 0) {
                    sequential[i] = measure(groups, false, expected);
                    parallel[i] = measure(groups, true, expected);
                } else {
                    parallel[i] = measure(groups, true, expected);
                    sequential[i] = measure(groups, false, expected);
                }
            }
            Arrays.sort(sequential);
            Arrays.sort(parallel);
            System.out.printf(Locale.ROOT, "%d groups: sequential %.3f ms, parallel %.3f ms; equal results.%n",
                    count, sequential[3] / 1_000_000.0, parallel[3] / 1_000_000.0);
        }
        System.out.println("Small armies may be faster sequentially because parallel work has overhead.");
    }

    private static long measure(List<GroupHealthSnapshot> groups, boolean parallel, ArmyHealthSummary expected) {
        long start = System.nanoTime();
        result = (parallel ? groups.parallelStream() : groups.stream()).collect(new ArmyHealthCollector());
        long elapsed = System.nanoTime() - start;
        if (!result.equals(expected)) throw new AssertionError("Sequential and parallel results differ");
        return elapsed;
    }
}
