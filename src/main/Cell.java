package main;

abstract class Cell {

    // 0 1 2
    // 3   4
    // 5 6 7
    final static byte[][] DIRS = new byte[][]{{-1, -1}, {0, -1}, {1, -1}, {-1, 0}, {1, 0}, {-1, 1}, {0, 1}, {1, 1}};
    static Cells cells;

    float aggression = 1;
    float aggression2 = aggression * aggression;

    int x, y;
    int generation;
    int mut1, mut2, mut3;
    float energy;
    int color_famity, color_complexity, color_special, color_generations;
    private boolean alive = true;
    protected int age;

    final void init(Cells _cells) {
        cells = _cells;
    }

    abstract void prepare();

    abstract boolean act();

    void step(int new_x, int new_y) {
        eraseSelf();
        x = new_x;
        y = new_y;
        cells.setCell(this);
    }

    void eraseSelf() {
        cells.deleteCell(x, y);
    }

    int[] getFreeCell() {
        for (int i = 0, j = cells.nextInt(8); i < 8; i++, j++) {
            int dir = j & 0b111;
            int new_x = x + Cell.DIRS[dir][0];
            int new_y = y + Cell.DIRS[dir][1];
            if (cells.checkFree(new_x, new_y)) {
                return new int[]{new_x, new_y};
            }
        }

        return null;
    }

    boolean checkStats() { //false is dead
        if (energy >= (cells.energyLim * cells.energySplitDeathGap) / 100) {
            split();
        }
        if (energy > cells.energyLim) {
            energy = cells.energyLim;
            kill();
            return false;
        }
        if (energy <= 0) {
            starve();
            return false;
        }
        if (age++ > cells.maxAge && cells.maxAge <= 200) {
            kill();
            return false;
        }

        return true;
    }

    void grow() {
        energy += cells.lightMap[x][y] / aggression;
    }

    abstract void split();

    boolean isAlive() {
        return alive;
    }

    private void kill() {
        if (energy <= 0) eraseSelf();
        alive = false;
        setAllColors(0x444444);
    }

    private void starve() {
        energy = cells.energyStep;
        alive = false;
        setAllColors(0x222222);
    }

    void eatCell(Cell c) {
        energy += c.energy;
        if (energy > cells.energyLim) energy = cells.energyLim;
        c.eraseSelf();
    }

    void calcNewColors(Cell parent) {
        int parentGreen = (parent.color_famity >> 8) & 0xff;
        int r = (mut1 * 10 & 0xff) << 16;
        int g;
        if (mut2 > parent.mut2) g = ((parentGreen + 10) & 0xff) << 8;
        else g = parentGreen;
        int b = (mut3 * 20 & 0xff);
        color_famity = r | g | b;
        calcColors();
    }

    abstract void calcColors();

    private void setAllColors(int c) {
        color_famity = c;
        color_complexity = c;
        color_special = c;
        color_generations = c;
    }

    void copyParentColors(Cell c) {
        color_famity = c.color_famity;
        color_complexity = c.color_complexity;
        color_special = c.color_special;
        color_generations = c.color_generations;
    }

    int getColor(int colorType) {
        switch (colorType) {
            case Cells.FAMILY:
                return color_famity;
            case Cells.ENERGY:
                int l = (int) (255 * energy / cells.energyLim);
                return l << 8 | 0x000700;
            case Cells.COMPLEXITY:
                return color_complexity;
            case Cells.GENERATIONS:
                return color_generations;
            case Cells.SPECIAL:
                return color_special;
            case Cells.AGE:
                if (alive) {
                    int c = (int) (127f + 127f * Math.tanh((2f * age) / cells.maxAge - 1f));
                    return c | 0x501000;
                }
                return color_famity;
        }
        return 0;
    }

    boolean isWeaker(Cell c) {
        return aggression2 * energy > c.aggression2 * c.energy;
    }

    boolean isRelative(Cell c) { //true is relative
        return Math.abs(mut1 - c.mut1) + Math.abs(mut2 - c.mut2) + Math.abs(mut3 - c.mut3) <= cells.peacefulness;
    }
}
