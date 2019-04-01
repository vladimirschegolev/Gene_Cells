package main;


import java.util.Arrays;


class CellGeneArray extends Cell {

    private byte dir;
    private int age = 0;
    private byte[] acts;
    private float aggression = 1;
    private float aggression2 = aggression * aggression;


    private final static int MAX_GENE_LENGTH = 30;


    CellGeneArray(int _x, int _y, Cells _cells) {
        cells = _cells;
        x = _x;
        y = _y;
        energy = 50;
        dir = (byte) cells.random.nextInt(8);
        acts = new byte[]{0};
        color = 0x00ff00;
        generation = 1;

    }

    private CellGeneArray(CellGeneArray parent, int _x, int _y) {
        x = _x;
        y = _y;
        energy = parent.energy;
        dir = (byte) cells.random.nextInt(8);
        generation = parent.generation;
        aggression = parent.aggression;

        if (cells.random.nextFloat() < cells.mutation) {
            generation++;

            if (parent.acts.length < MAX_GENE_LENGTH && cells.random.nextBoolean()) {
                acts = new byte[parent.acts.length + 1];
                System.arraycopy(parent.acts, 0, acts, 0, parent.acts.length);
                acts[parent.acts.length] = getRandAct();
            } else if (parent.acts.length > 1 && cells.random.nextBoolean()) {
                int remove = cells.random.nextInt(parent.acts.length);
                acts = new byte[parent.acts.length - 1];
                for (int i = 0; i < parent.acts.length; i++) {
                    if (i < remove) {
                        acts[i] = parent.acts[i];
                    } else if (i > remove) {
                        acts[i - 1] = parent.acts[i];
                    }
                }
            } else {
                acts = Arrays.copyOf(parent.acts, parent.acts.length);
                acts[cells.random.nextInt(acts.length)] = getRandAct();
            }
            if (cells.random.nextBoolean()) {
                if (cells.random.nextBoolean()) {
                    aggression *= .99f;
                } else {
                    aggression /= .99f;
                }
                aggression2 = aggression * aggression2;
            }
            color = calcColor();
        } else {
            acts = Arrays.copyOf(parent.acts, parent.acts.length);
            color = parent.color;
        }

    }

    private byte getRandAct() {
        return (byte) (cells.random.nextInt(11));
    }


    private int calcColor() {

        return (((int) (aggression * 100)) & 0xff) << 16 | ((200 - generation * 23) & 0xff) << 8 | (int) ((255f * acts.length) / MAX_GENE_LENGTH);
    }

    public boolean act() {

        int energy_minus = 0;
        cycle:
        for (int count = 0, index = 0; energy > 0 && count < 30; count++) {
            energy_minus++;
            switch (acts[Math.abs(index++ % acts.length)]) {
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
                    if (move()) {
                        return false;
                    }
                    break cycle;
                case 5:
                    index += acts[Math.abs(++index % acts.length)];
                    break;
                case -1:
                    energy_minus = 1;
                    break cycle;

            }
        }

        energy -= energy_minus * cells.energyStep * aggression;


        if (energy >= (cells.energyLim * cells.energySplitDeathGap) / 100) {
            split();
        }
        if (energy > cells.energyLim) {
            energy = cells.energyLim;
            kill();
            return false;
        }
        if (energy <= 0) {
            energy = cells.energyStep;
            starve();
            return false;
        }
        if (age++ > 50) {
            kill();
            return false;
        }

        return true;
    }

    @Override
    int getComplexity() {
        int l = (int) ((255f * acts.length) / MAX_GENE_LENGTH);
        return (l << 16) | 0x40;
    }


    private void grow() {
        energy += cells.lightMap[x][y] / aggression2;
    }

    private void split() {

        int[] free = getFreeCell();
        if (free != null) {
            energy = (int) (energy * .4);
            cells.newCell(new CellGeneArray(this, free[0], free[1]));
        }
    }

    private int observe() {   // 0:good 1:wall/relative 2:darker 3:darker then energystep 4:danger
        int new_x = DIRS[dir][0] + x, new_y = DIRS[dir][1] + y;

        if (!cells.checkBounds(new_x, new_y))  //out of field
            return 1;

        else if (cells.hasCell(new_x, new_y)) {
            CellGeneArray c = (CellGeneArray) cells.getCell(new_x, new_y);
            if (!c.isAlive()) {
                return 0;
            } else {
                if (isRelative(c)) {
                    return 1;
                } else if (isWeaker(c)) {
                    return 0;
                } else {
                    return 4;
                }
            }
        } else {
            if (cells.lightMap[x][y] < cells.energyStep) {
                return 3;
            } else if (cells.lightMap[new_x][new_y] < cells.lightMap[x][y]) {
                return 2;
            } else {
                return 0;
            }
        }
    }

    private boolean move() {

        int new_x = DIRS[dir][0] + this.x;
        int new_y = DIRS[dir][1] + this.y;
        if (!cells.checkBounds(new_x, new_y))
            return false;
        if (cells.hasCell(new_x, new_y)) {
            CellGeneArray c = (CellGeneArray) cells.getCell(new_x, new_y);
            if (c.isAlive()) {
                if (!isRelative(c)) {
                    if (isWeaker(c)) {
                        if (biteCell(c)) {
                            c.kill();
                            c.erase();
                            step(new_x, new_y);
                        }
                    } else {
                        if (c.biteCell(this)) {
                            kill();
                            return true;
                        }
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


    private boolean isRelative(CellGeneArray c) {
        return cells.peacefulness >= Math.abs(acts.length - c.acts.length) +
                Math.abs(aggression - c.aggression) * 100 +
                Math.abs(generation - c.generation);
    }

    private boolean isWeaker(CellGeneArray c) {
        return aggression > c.aggression;
    }


    private boolean biteCell(CellGeneArray c) {
        float bite = 100 * aggression;
        if (bite > c.energy) {
            energy += c.energy;
            return true;
        }
        energy += bite;
        c.energy -= bite;
        return false;
    }


    @Override
    public String toString() {
        return String.format("gen: %d energy: %f aggression: %f gene: " + Arrays.toString(acts),
                generation, energy, aggression);
    }
}
