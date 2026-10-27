package entity.interactables;

import entity.troops.Troop;
import java.util.Optional;

// hill gets generated on the map, then gives a damage bonus to the team that holds it.

public class Hill extends TileEffect {

    /* ---- PRIVATE VARIABLES ---- */
    private final int damageBonus;
    private Troop.Team holder;

    /* ---- CONSTRUCTORS ---- */
    public Hill(int row, int column, int damageBonus) {
        // hills are neutral terrain, so no team owns them at placement time.
        super(row, column, null);
        this.damageBonus = damageBonus;
        holder = null;
    }

    /* ---- GETTER METHODS ---- */
    public int getDamageBonus() {
        return damageBonus;
    }

    public Troop.Team getHolder() {
        return holder;
    }

    /* ---- PUBLIC METHODS ---- */
    public boolean isClaimed() {
        return holder != null;
    }

    /* ---- OVERRIDES ---- */

    // only the holding team gets the bonus, and only while standing here.
    // read at the moment of the attack, so nothing is undone when it leaves.
    @Override
    public int damageBonusFor(Troop troop) {
        return (holder == troop.getTeam()) ? damageBonus : 0;
    }

    @Override
    public Optional<TileEffectEvent> onEnter(Troop troop) {
        // first team to arrive keeps it for the rest of the round
        if (holder != null) {
            return Optional.empty();
        }

        holder = troop.getTeam();

        return Optional.of(TileEffectEvent.of(
                this, troop, troop.getTeam() + " took the high ground (+" + damageBonus + " dmg)"));
    }

    @Override
    public boolean isConsumed() {
        return false;
    }

    @Override
    public <R> R accept(TileEffectVisitor<R> visitor) {
        return visitor.visitHill(this);
    }

}
