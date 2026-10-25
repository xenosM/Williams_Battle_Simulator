package entity.troops;

import entity.Damageable;

import java.util.Optional;

/*
 * Base class for attacking troop types.
 * Melee and ranged troop classes extend this.
 */
public abstract class Attacker extends Troop implements Healable {
    private Damageable target;

    /* ---- CONSTRUCTORS ---- */
    public Attacker(int row, int column, Stats stats, int initialGroupSize, Team team) {
        super(row, column, stats, initialGroupSize, team);
    }

    /* ---- PUBLIC METHODS ---- */
    @Override
    public void beHealed(int healAmount) {
        groupHealth = Math.min(groupHealth + healAmount, getMaxHealth());
    }

    public void attack() {
        System.out.println(target + "attacked");
        this.target.takeDamage(getAttack()); // attack is proportional to the group size
        if(target.isDestroyed()) System.out.println(target + "dead");
    }

    /* ---- GETTER METHODS ---- */
    public int getAttack() {
        return stats.baseAttack() * getGroupSize();
    }

    public int getRange() {
        return stats.attackRange();
    }

    public Damageable getTarget() {
        return target;
    }

    /* ---- SETTER METHODS ---- */
    public void setTarget(Damageable target) {
        this.target = target;
    }

}
