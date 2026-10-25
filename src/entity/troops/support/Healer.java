package entity.troops.support;

import entity.troops.Healable;
import entity.troops.Stats;
import entity.troops.Support;

/*
 * Base class for healing troop types.
 */
public abstract class Healer extends Support {


    private Healable target;
    /* ---- CONSTRUCTORS ---- */
    public Healer(int row, int column, Stats stats, int initialGroupSize, Team team) {
        super(row, column, stats, initialGroupSize, team);
    }

    /* ---- PUBLIC METHODS ---- */
    public void heal() {

        System.out.println(target + "healed");
        this.target.beHealed(multipliedHealAmount());
    }

    /* ---- PRIVATE METHODS ---- */
    // Heal grows with the square root of the group size (diminishing returns)
    private int multipliedHealAmount() {
        return (int) (stats.healAmount() * Math.sqrt(getGroupSize()));
    }

    /* ---- GETTER METHODS ---- */
    public int getHealAmount() {
        return stats.healAmount();
    }
    public Healable getTarget(){
        return  target;
    }
    /* ---- SETTER METHODS ---- */
    public void setTarget(Healable target){
        this.target=target;
    }
}
