import entity.interactables.Hill;
import entity.troops.Troop;
import entity.troops.melee.Cavalry;
import entity.troops.melee.Infantry;
import entity.troops.ranged.Archer;
import entity.troops.support.Priest;

// checks that high ground extends range for ranged troops only, and only for
// the team holding the hill.
public class HillRangeTest {

    private static int failures = 0;

    public static void main(String[] args) {
        onlyRangedGainRange();
        onlyTheHolderGains();
        unclaimedHillGivesNothing();
        damageBonusStillWorksForEveryone();

        if (failures > 0) {
            System.out.println(failures + " hill range checks FAILED.");
            System.exit(1);
        }
        System.out.println("Hill range checks passed.");
    }

    private static void onlyRangedGainRange() {
        Hill hill = new Hill(0, 0, 5);
        Archer archer = new Archer(0, 0, 10, Troop.Team.teamA);
        hill.onEnter(archer); // teamA claims it

        check("archer gains range", 1, hill.rangeBonusFor(archer));
        check("infantry gains none", 0, hill.rangeBonusFor(new Infantry(0, 0, 10, Troop.Team.teamA)));
        check("cavalry gains none", 0, hill.rangeBonusFor(new Cavalry(0, 0, 10, Troop.Team.teamA)));
        check("priest gains none", 0, hill.rangeBonusFor(new Priest(0, 0, 10, Troop.Team.teamA)));
    }

    private static void onlyTheHolderGains() {
        Hill hill = new Hill(0, 0, 5);
        hill.onEnter(new Archer(0, 0, 10, Troop.Team.teamA));
        check("enemy archer gains none", 0,
                hill.rangeBonusFor(new Archer(0, 0, 10, Troop.Team.teamB)));
    }

    private static void unclaimedHillGivesNothing() {
        Hill hill = new Hill(0, 0, 5);
        check("unclaimed hill gives no range", 0,
                hill.rangeBonusFor(new Archer(0, 0, 10, Troop.Team.teamA)));
    }

    private static void damageBonusStillWorksForEveryone() {
        Hill hill = new Hill(0, 0, 5);
        Infantry infantry = new Infantry(0, 0, 10, Troop.Team.teamA);
        hill.onEnter(infantry);
        check("melee still gets the damage bonus", 5, hill.damageBonusFor(infantry));
        check("ranged also gets the damage bonus", 5,
                hill.damageBonusFor(new Archer(0, 0, 10, Troop.Team.teamA)));
    }

    private static void check(String what, int expected, int actual) {
        if (expected == actual) {
            System.out.println("  PASS  " + what + " (" + actual + ")");
        } else {
            System.out.println("  FAIL  " + what + "  expected " + expected + " got " + actual);
            failures++;
        }
    }
}
