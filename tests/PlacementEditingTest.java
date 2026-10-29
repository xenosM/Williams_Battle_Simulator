import controller.Round;
import controller.states.PlaceState;
import entity.InvalidPlacementException;
import entity.Team;
import entity.troops.Troop;
import entity.troops.TroopType;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import javax.swing.SwingUtilities;
import view.BattlefieldPanel;

public class PlacementEditingTest {
    public static void main(String[] args) throws Exception {
        checkMovesAndRefunds();
        checkInvalidEdits();
        SwingUtilities.invokeAndWait(PlacementEditingTest::checkContextClick);
        System.out.println("Placement editing checks passed.");
    }

    private static Round placementRound() {
        Round round = new Round(new Team("teamA"), new Team("teamB"));
        round.setCurrentState(new PlaceState());
        return round;
    }

    private static void checkMovesAndRefunds() throws Exception {
        for (Troop.Team owner : Troop.Team.values()) {
            for (TroopType type : TroopType.values()) {
                Round round = placementRound();
                int column = owner == Troop.Team.teamA ? 2 : 20;
                Troop troop = round.buyTroop(owner, type, 3, 2, column);
                Team team = round.getOwnTeam(troop);
                int budget = team.getBudget();
                round.movePlacedTroop(troop, 3, column);
                check(troop.isAt(3, column) && round.isCellFree(2, column), "Move must free the old tile");
                check(team.getArmy().size() == 1 && team.getBudget() == budget
                        && troop.getInitialGroupSize() == 3, "Move must preserve the group and gold");
                round.movePlacedTroop(troop, 3, column);
                check(team.getBudget() == budget, "Choosing the current tile must not charge gold");
                int refund = round.removePlacedTroop(troop);
                check(refund == type.getTotalCost(3) && team.getBudget() == 500,
                        "Removal must refund the original purchase to its team");
                check(team.getArmy().isEmpty() && round.isCellFree(3, column), "Removal must free the tile");
                rejects(() -> round.removePlacedTroop(troop));
                rejects(() -> round.movePlacedTroop(troop, 4, column));
                check(team.getBudget() == 500, "A removed group must not be refunded twice");
            }
        }

        Round round = placementRound();
        Troop left = round.buyTroop(Troop.Team.teamA, TroopType.INFANTRY, 1, 0, 0);
        round.buyTroop(Troop.Team.teamA, TroopType.PRIEST, 1, 0, 1);
        round.buyTroop(Troop.Team.teamB, TroopType.INFANTRY, 1, 0, 20);
        check(round.canStartBattle(), "Both teams have attackers");
        round.removePlacedTroop(left);
        check(!round.canStartBattle(), "Removing the last attacker must block battle");
    }

    private static void checkInvalidEdits() throws Exception {
        Round round = placementRound();
        round.spawnTileEffects();
        // Find a free starting cell, avoiding the randomly placed terrain.
        int column = 0;
        while (!round.isCellFree(0, column)) column++;
        Troop troop = round.buyTroop(Troop.Team.teamA, TroopType.INFANTRY, 2, 0, column);
        int budget = round.getTeamA().getBudget();
        rejects(() -> round.movePlacedTroop(troop, -1, 0));
        rejects(() -> round.movePlacedTroop(troop, round.getRowCount(), 0));
        rejects(() -> round.movePlacedTroop(troop, 0, round.getColumnCount()));
        rejects(() -> round.movePlacedTroop(troop, 0, round.getColumnCount() / 2));
        var castle = round.getTeamACastle();
        rejects(() -> round.movePlacedTroop(troop, castle.getRow() + 1, castle.getColumn() + 1));
        var effect = round.getTileEffects().get(0);
        rejects(() -> round.movePlacedTroop(troop, effect.getRow(), effect.getColumn()));
        rejects(() -> round.removePlacedTroop(null));
        check(troop.isAt(0, column) && round.getTeamA().getBudget() == budget,
                "Rejected edits must leave the original position and budget unchanged");

        Round other = placementRound();
        Troop foreign = other.buyTroop(Troop.Team.teamA, TroopType.INFANTRY, 1, 0, 0);
        rejects(() -> round.removePlacedTroop(foreign));
        rejects(() -> round.movePlacedTroop(foreign, 1, 0));

        Round occupied = placementRound();
        Troop first = occupied.buyTroop(Troop.Team.teamA, TroopType.INFANTRY, 1, 0, 0);
        occupied.buyTroop(Troop.Team.teamA, TroopType.PRIEST, 1, 0, 1);
        rejects(() -> occupied.movePlacedTroop(first, 0, 1));
        check(first.isAt(0, 0), "Moving onto a group must not displace either group");
        occupied.buyTroop(Troop.Team.teamB, TroopType.INFANTRY, 1, 0, 20);
        occupied.startBattle();
        int combatBudget = occupied.getTeamA().getBudget();
        rejects(() -> occupied.movePlacedTroop(first, 1, 0));
        rejects(() -> occupied.removePlacedTroop(first));
        check(first.isAt(0, 0) && occupied.getTeamA().getBudget() == combatBudget,
                "Editing during combat must be rejected without changes");
    }

    private static void checkContextClick() {
        try {
            Round round = placementRound();
            Troop troop = round.buyTroop(Troop.Team.teamB, TroopType.ARCHER, 2, 2, 20);
            BattlefieldPanel field = new BattlefieldPanel();
            field.setRound(round);
            int[] menus = { 0 };
            field.setTroopMenuHandler((selected, event) -> {
                check(selected == troop, "Context click must find the right group");
                menus[0]++;
            });
            for (int width : new int[] { 900, 1920 }) {
                field.setSize(width, 680);
                Rectangle tile = field.getTileBounds(2, 20);
                field.dispatchEvent(new MouseEvent(field, MouseEvent.MOUSE_RELEASED, 0, 0,
                        tile.x + tile.width / 2, tile.y + tile.height / 2, 1, true, MouseEvent.BUTTON3));
            }
            check(menus[0] == 2, "Resizing must preserve context-menu hit detection");
            round.buyTroop(Troop.Team.teamA, TroopType.INFANTRY, 1, 0, 0);
            round.startBattle();
            Rectangle tile = field.getTileBounds(2, 20);
            field.dispatchEvent(new MouseEvent(field, MouseEvent.MOUSE_RELEASED, 0, 0,
                    tile.x + tile.width / 2, tile.y + tile.height / 2, 1, true, MouseEvent.BUTTON3));
            check(menus[0] == 2, "Combat must not open the editing menu");
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private static void rejects(Edit edit) throws Exception {
        try {
            edit.run();
            throw new AssertionError("Invalid edit was accepted");
        } catch (InvalidPlacementException expected) {
            // Validation must reject the edit before changing the army or budget.
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    @FunctionalInterface
    private interface Edit {
        void run() throws InvalidPlacementException;
    }
}
