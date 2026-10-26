package entity.interactables;

import entity.troops.Troop;

/*
 * What actually happened when a troop stepped onto an effect.
 *
 * The effect fills this in itself, including any budget change, so a listener
 * can react without knowing which kind of effect fired. That is the whole
 * point: BudgetListener reads budgetChange() and EventLogListener reads
 * description(), and neither has to ask whether this was a coin or a trap.
 */
public record TileEffectEvent(TileEffect effect, Troop troop, String description, int budgetChange) {

    /* ---- FACTORY METHODS ---- */

    // An event that changes nothing but is worth reporting.
    public static TileEffectEvent of(TileEffect effect, Troop troop, String description) {
        return new TileEffectEvent(effect, troop, description, 0);
    }

    // An event that pays the troop's team.
    public static TileEffectEvent paying(TileEffect effect, Troop troop, String description, int amount) {
        return new TileEffectEvent(effect, troop, description, amount);
    }
}
