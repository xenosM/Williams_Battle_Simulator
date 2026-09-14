package entity;

public class Castle implements Positioned {
    private int row;
    private int column;
    private int health;

    public Castle(int row, int column, int health) {
        this.row = row;
        this.column = column;
        this.health = health;
    }

    @Override
    public boolean isAt(int row, int column) {
        return this.row == row && this.column == column;
    }

    @Override
    public int getRow() {
        return row;
    }

    @Override
    public int getColumn() {
        return column;
    }

    public int getHealth() {
        return health;
    }

    public void takeDamage(int dmg) {
        health -= dmg;
    }

    public boolean isDestroyed() {
        return health <= 0;
    }

}