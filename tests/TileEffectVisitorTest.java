import entity.interactables.Coin;
import entity.interactables.Hill;
import entity.interactables.TileEffect;
import entity.interactables.TileEffectVisitor;
import entity.interactables.Trap;
import entity.troops.Troop;
import entity.troops.melee.Infantry;
import java.util.List;

// checks that accept() reaches the right visit method for every effect type,
// and that one visitor interface serves two different return types.
public class TileEffectVisitorTest {

    private static int failures = 0;

    public static void main(String[] args) {
        dispatchReachesTheRightMethod();
        visitorSeesEffectState();
        oneInterfaceTwoReturnTypes();

        if (failures > 0) {
            System.out.println(failures + " tile effect visitor checks FAILED.");
            System.exit(1);
        }
        System.out.println("Tile effect visitor checks passed.");
    }

    // a visitor that just reports which method ran
    private static final class NameVisitor implements TileEffectVisitor<String> {
        public String visitHill(Hill hill) { return "hill"; }
        public String visitCoin(Coin coin) { return "coin"; }
        public String visitTrap(Trap trap) { return "trap"; }
    }

    private static void dispatchReachesTheRightMethod() {
        NameVisitor visitor = new NameVisitor();
        check("hill dispatches to visitHill", "hill", new Hill(0, 0, 5).accept(visitor));
        check("coin dispatches to visitCoin", "coin", new Coin(0, 0, 25).accept(visitor));
        check("trap dispatches to visitTrap", "trap", new Trap(0, 0, 20, Troop.Team.teamA).accept(visitor));

        // the point of the pattern: a mixed list needs no type checks
        List<TileEffect> board = List.of(new Hill(1, 1, 5), new Coin(2, 2, 25),
                new Trap(3, 3, 20, Troop.Team.teamB));
        StringBuilder seen = new StringBuilder();
        for (TileEffect effect : board) {
            seen.append(effect.accept(visitor)).append(" ");
        }
        check("mixed list dispatches without instanceof", "hill coin trap ", seen.toString());
    }

    private static void visitorSeesEffectState() {
        TileEffectVisitor<Boolean> claimed = new TileEffectVisitor<>() {
            public Boolean visitHill(Hill hill) { return hill.isClaimed(); }
            public Boolean visitCoin(Coin coin) { return coin.getCollectedBy() != null; }
            public Boolean visitTrap(Trap trap) { return trap.isTriggered(); }
        };

        Hill hill = new Hill(0, 0, 5);
        check("unclaimed hill reports false", Boolean.FALSE, hill.accept(claimed));
        hill.onEnter(new Infantry(0, 0, 10, Troop.Team.teamA));
        check("claimed hill reports true", Boolean.TRUE, hill.accept(claimed));

        Trap trap = new Trap(0, 0, 20, Troop.Team.teamA);
        check("fresh trap reports false", Boolean.FALSE, trap.accept(claimed));
        trap.onEnter(new Infantry(0, 0, 10, Troop.Team.teamB));
        check("sprung trap reports true", Boolean.TRUE, trap.accept(claimed));
    }

    private static void oneInterfaceTwoReturnTypes() {
        TileEffectVisitor<Integer> worth = new TileEffectVisitor<>() {
            public Integer visitHill(Hill hill) { return hill.getDamageBonus(); }
            public Integer visitCoin(Coin coin) { return coin.getAmount(); }
            public Integer visitTrap(Trap trap) { return trap.getDamage(); }
        };
        check("generic visitor returns Integer", Integer.valueOf(25), new Coin(0, 0, 25).accept(worth));
        check("generic visitor returns String", "hill", new Hill(0, 0, 5).accept(new NameVisitor()));
    }

    private static void check(String what, Object expected, Object actual) {
        if (expected.equals(actual)) {
            System.out.println("  PASS  " + what);
        } else {
            System.out.println("  FAIL  " + what + "  expected " + expected + " got " + actual);
            failures++;
        }
    }
}
