package entity.interactables;

import entity.Positioned;
import entity.troops.Troop;
import java.util.Optional;

public abstract class TileEffect implements Positioned {
    /* ---- PRIVATE VARIABLES ---- */
    private final int row;
    private final int column;
    private final Troop.Team owner;

    /* ---- CONSTRUCTORS ---- */
    public TileEffect(int row, int column, Troop.Team owner) {
        this.row = row;
        this.column = column;
        this.owner = owner;
    }

    /* ---- GETTER METHODS ---- */
    @Override
    public int getRow() {
        return row;
    }

    @Override
    public int getColumn() {
        return column;
    }

    public Troop.Team getOwner() {
        return owner;
    }

    /* ---- PUBLIC METHODS ---- */
    @Override
    public boolean isAt(int row, int column) {
        return this.row == row && this.column == column;
    }

    /*
     * Extra damage this effect grants to a troop standing on it. Most effects
     * grant none, so the default is zero and only Hill overrides it. Doing it
     * this way keeps the combat code free of checks for which effect it is.
     */
    public int damageBonusFor(Troop troop) {
        return 0;
    }

    /* ---- ABSTRACT METHODS ---- */

    /*
     * Called when a troop moves onto this cell.
     *
     * Returns an event describing what happened, or empty when nothing did -
     * a spent trap, a coin already taken, a troop walking over its own team's
     * trap. Returning the event rather than acting directly is what lets Round
     * notify its listeners only on real changes.
     */
    public abstract Optional<TileEffectEvent> onEnter(Troop troop);

    // true once the effect is spent and should be removed
    // hills always return false, traps return true after firing.
    public abstract boolean isConsumed();

}
