package main;

import java.awt.*;

abstract class Cell {

    int x, y, generation;
    float energy;
    Color color;
    private boolean alive = true;


    abstract boolean act();

    void step(int x, int y) {
        Cells.setCell(x, y, this);
        Cells.setCell(this.x, this.y, null);
        this.x = x;
        this.y = y;
    }

    void erase() {
        Cells.deleteCell(x, y);
    }

    boolean isAlive() {
        return alive;
    }

    void kill() {
        alive = false;
        color = Color.LIGHT_GRAY;
    }

    void starve() {
        alive = false;
        color = Color.DARK_GRAY;
    }

    void eatCell(Cell c) {
        energy += c.energy;
    }


}
