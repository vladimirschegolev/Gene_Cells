package main;


import java.util.Arrays;


class CellGeneArray extends Cell {

    private byte dir;
    private byte[] acts;
//    private float aggression = 1;
//    private float aggression2 = aggression * aggression;
    private boolean move = false, notReady = true;

    private final static int MAX_GENE_LENGTH = 40;


    CellGeneArray(int _x, int _y, Cells _cells) {
        init(_cells);
        x = _x;
        y = _y;
        energy = 50;
        dir = (byte) cells.nextInt(8);
        acts = new byte[]{0};
        color_famity = 0x009900;
        calcColors();
        generation = 1;
        mut1 = 0;
        mut2 = 0;
        mut3 = 0;
    }

    private CellGeneArray(CellGeneArray parent, int _x, int _y) {
        dir = (byte) cells.nextInt(8);
        x = _x;
        y = _y;
        energy = parent.energy;
        generation = parent.generation;
        aggression = parent.aggression;
        mut1 = parent.mut1;
        mut2 = parent.mut2;
        mut3 = parent.mut3;

        if (cells.nextFloat() < cells.mutation) {
            generation++;
            if (cells.nextBoolean()) {
                mut1++;
                if (parent.acts.length < MAX_GENE_LENGTH && cells.nextBoolean() || parent.acts.length == 1) {
                    acts = new byte[parent.acts.length + 1];
                    System.arraycopy(parent.acts, 0, acts, 0, parent.acts.length);
                    acts[parent.acts.length] = getRandAct();
                } else {
                    int remove = cells.nextInt(parent.acts.length);
                    acts = new byte[parent.acts.length - 1];
                    for (int i = 0; i < parent.acts.length; i++) {
                        if (i < remove) {
                            acts[i] = parent.acts[i];
                        } else if (i > remove) {
                            acts[i - 1] = parent.acts[i];
                        }
                    }
                }
            } else {
                mut2++;
                acts = Arrays.copyOf(parent.acts, parent.acts.length);
                acts[cells.nextInt(acts.length)] = getRandAct();
            }
            if (cells.nextBoolean()) {
                mut3++;
                if (aggression < .5f || cells.nextBoolean() && aggression < 5) {
                    aggression /= .9f;
                } else {
                    aggression *= .9f;
                }
                aggression2 = aggression * aggression2;
            }
            calcNewColors(parent);
        } else {
            acts = Arrays.copyOf(parent.acts, parent.acts.length);
            copyParentColors(parent);
        }

    }

    private byte getRandAct() {
        return (byte) (cells.nextInt(10));
    }

    @Override
    void calcColors() {
        color_complexity = ((int) ((255f * acts.length) / MAX_GENE_LENGTH) << 16) | 0x40;
        color_generations = cells.nextInt(0xffffff);
        color_special = ((int) (127f + 127f * Math.tanh(aggression - 1)) & 0xff) << 16;
    }


    @Override
    void prepare() {
        int energy_minus = 0;
        int index = 0;
        cycle:
        for (int count = 0; count < 32; count++, index++) {
            energy_minus++;
            if (index >= acts.length) index %= acts.length;
            switch (acts[index]) {
                case 0:
                    grow();
                    break cycle;
                case 1:
                    index += observe();
                    break;
                case 2:
                    if (++dir > 7) dir = 0;
                    break;
                case 3:
                    if (--dir < 0) dir = 7;
                    break;
                case 4:
                    move = true;
                    break cycle;
                case 5:
                    index += acts[++index < acts.length ? index : index % acts.length];
                    break;
            }
        }
        energy -= energy_minus * cells.energyStep * aggression;
        notReady = false;
    }

    public boolean act() { //false is dead

        if (notReady) {
            prepare();
        }

        if (move && move()) {
            return false;
        }

        move = false;
        notReady = true;

        return checkStats();
    }

    @Override
    void split() {
        int[] free = getFreeCell();
        if (free != null) {
            energy = (int) (energy * .4);
            cells.newCell(new CellGeneArray(this, free[0], free[1]));
        }
    }

    private int observe() {   // 0:good 2:wall/relative 4:darker 6:darker then energystep 8:danger
        int new_x = DIRS[dir][0] + x, new_y = DIRS[dir][1] + y;

        if (!cells.checkBounds(new_x, new_y))  //out of field
            return 2;

        else if (cells.hasCell(new_x, new_y)) {
            CellGeneArray c = (CellGeneArray) cells.getCell(new_x, new_y);
            if (!c.isAlive()) {
                return 0;
            } else {
                if (isRelative(c)) {
                    return 2;
                } else if (isWeaker(c)) {
                    return 0;
                } else {
                    return 8;
                }
            }
        } else {
            if (cells.lightMap[x][y] < cells.energyStep * aggression) {
                return 6;
            } else if (cells.lightMap[new_x][new_y] < cells.lightMap[x][y]) {
                return 4;
            } else {
                return 0;
            }
        }
    }

    private boolean move() { //true is dead
        int new_x = DIRS[dir][0] + this.x;
        int new_y = DIRS[dir][1] + this.y;
        if (!cells.checkBounds(new_x, new_y))
            return false;
        if (cells.hasCell(new_x, new_y)) {
            CellGeneArray c = (CellGeneArray) cells.getCell(new_x, new_y);
            if (c.isAlive()) {
                if (!isRelative(c)) {
                    if (isWeaker(c)) {
                        eatCell(c);
                        step(new_x, new_y);
                    } else {
                        c.eatCell(this);
                        return true;
                    }
                }
            } else {
                eatCell(c);
                step(new_x, new_y);
            }
        } else {
            step(new_x, new_y);
        }
        return false;
    }


//    private boolean bite(CellGeneArray c) {
//        float bite = 100 * aggression2;
//        if (bite > c.energy) {
//            energy += c.energy;
//            return true;
//        }
//        energy += bite;
//        c.energy -= bite;
//        return false;
//    }


    @Override
    public String toString() {
        return String.format("gen: %d mut: %d %d %d %n energy: %2.1f aggression: %2.3f age %d gene: " + Arrays.toString(acts),
                generation, mut1, mut2, mut3, energy, aggression, age);
    }
}
