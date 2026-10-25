package entity.troops;

import entity.Positioned;

/*
 * Anything that can be healed by a healer.
 */
public interface Healable extends Positioned {
    void beHealed(int healAmount);
}
