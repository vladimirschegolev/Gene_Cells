package main;

public class CellNeuro extends Cell {

    

    public CellNeuro(int x, int y) {
        this.x = x;
        this.y = y;


    }

    @Override
    boolean isAlive() {
        return false;
    }

    @Override
    boolean act() {
        return false;
    }
}
