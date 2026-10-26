package entity.interactables;

import entity.troops.Troop;
import java.util.Optional;

// gold spawns on the battlefield. The first troop to reach it collects the
// coin for their team.

public class Coin extends TileEffect {
    /* ---- PRIVATE VARIABLES ---- */
    private final int amount;
    private Troop.Team collectedBy;

    /* ---- CONSTRUCTORS ---- */
    public Coin(int row, int column, int amount) {
        // Gold is neutral until someone picks it up.
        super(row, column, null);
        this.amount = amount;
        collectedBy = null;
    }

    /* ---- GETTER METHODS ---- */
    public int getAmount() {
        return amount;
    }

    public Troop.Team getCollectedBy() {
        return collectedBy;
    }

    /* ---- OVERRIDES ---- */
    @Override
    public Optional<TileEffectEvent> onEnter(Troop troop) {
        if (collectedBy != null) {
            return Optional.empty(); // someone already took it this round
        }

        collectedBy = troop.getTeam();

        /*
         * The coin names its own payout rather than leaving a listener to work
         * out what a coin is worth. BudgetListener just applies whatever number
         * the event carries.
         */
        return Optional.of(TileEffectEvent.paying(
                this, troop, troop.getTeam() + " collected " + amount + " gold", amount));
    }

    // Unlike a Hill, a collected coin is spent and leaves the board.
    @Override
    public boolean isConsumed() {
        return collectedBy != null;
    }

}
