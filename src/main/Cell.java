package main;

abstract class Cell {

    // 0 1 2
    // 3   4
    // 5 6 7
    final static byte[][] DIRS = new byte[][]{{-1, -1}, {0, -1}, {1, -1}, {-1, 0}, {1, 0}, {-1, 1}, {0, 1}, {1, 1}};
    static Cells cells;

    int x, y;
    int generation;
    float energy;
    int color;
    private boolean alive = true;

    abstract boolean act();

    void step(int new_x, int new_y) {
        cells.deleteCell(x, y);
        x = new_x;
        y = new_y;
        cells.setCell(this);
    }

    void erase() {
        cells.deleteCell(x, y);
    }

    int[] getFreeCell() {
        for (int i = 0, j = cells.random.nextInt(8); i < 8; i++, j++) {
            int dir = j & 0b111;
            int new_x = x + Cell.DIRS[dir][0];
            int new_y = y + Cell.DIRS[dir][1];
            if (cells.checkFree(new_x, new_y)) {
                return new int[]{new_x, new_y};
            }
        }

        return null;
    }

    boolean isAlive() {
        return alive;
    }

    void kill() {
        alive = false;
        color = 0x444444;
    }

    void starve() {
        alive = false;
        color = 0x222222;
    }

    void eatCell(Cell c) {
        energy += c.energy;
    }

    abstract int getComplexity();
}
