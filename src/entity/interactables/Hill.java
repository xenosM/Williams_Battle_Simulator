package entity.interactables;

import entity.troops.Troop;
import java.util.Optional;

//hill gets genrerated on the map, then give damage bonus to the team that holds it.

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

    /*
     * The bonus only applies to the team that claimed the hill, and only while
     * one of their troops is standing on it. Nothing has to be undone when the
     * troop leaves, because the bonus is read from the cell at the moment of
     * the attack rather than written onto the troop.
     */
    @Override
    public int damageBonusFor(Troop troop) {
        return (holder == troop.getTeam()) ? damageBonus : 0;
    }

    @Override
    public Optional<TileEffectEvent> onEnter(Troop troop) {
        if (holder != null) {
            return Optional.empty(); // first team to arrive keeps it for the round
        }

        holder = troop.getTeam();

        return Optional.of(TileEffectEvent.of(
                this, troop, troop.getTeam() + " took the high ground (+" + damageBonus + " dmg)"));
    }

    @Override
    public boolean isConsumed() {
        return false;
    }

}
