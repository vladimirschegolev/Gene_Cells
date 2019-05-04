package main;

import java.util.Arrays;

public class CellMutator extends Cell {



    private int peacefulness = 10;
    private float mutation = .1f;
    private int energyStep = 25;
    private int maxAge = 100;
    private byte dir;
    private byte[] acts;
    private boolean move = false, notReady = true;

    private final static int MAX_GENE_LENGTH = 40;

    CellMutator(int _x, int _y, Cells _cells) {
        init(_cells);
        x = _x;
        y = _y;
        energy = 50;
        dir = (byte) cells.nextInt(8);
        acts = new byte[]{0};
        color_family = 0x009900;
        calcColors();
        generation = 1;
        mut1 = 0;
        mut2 = 0;
        mut3 = 0;
    }
    private CellMutator(CellMutator parent, int _x, int _y) {
        dir = (byte) cells.nextInt(8);
        x = _x;
        y = _y;
        energy = parent.energy;
        generation = parent.generation;

        peacefulness = parent.peacefulness;
        mutation = parent.mutation;
        energyStep = parent.energyStep;
        maxAge = parent.maxAge;

        mut1 = parent.mut1;
        mut2 = parent.mut2;
        mut3 = parent.mut3;

        if (cells.nextFloat() < mutation) {
            generation++;
            switch (cells.nextInt(3)) {
                case 0:
                    mut1++;
                    mutateActs(parent);
                    break;
                case 1:
                    mut2++;
                    mutateAggression();
                    acts = Arrays.copyOf(parent.acts, parent.acts.length);
                    break;
                case 2:
                    mut3++;
                    mutateAge();
                    mutatePeacefulness();
                    acts = Arrays.copyOf(parent.acts, parent.acts.length);
                    break;
            }
            mutateMutation();
            calcNewColors(parent);
        } else {
            acts = Arrays.copyOf(parent.acts, parent.acts.length);
            copyParentColors(parent);
        }

    }

    private void mutatePeacefulness() {
        if (peacefulness > 1 && cells.nextBoolean()) {
            peacefulness--;
        } else {
            peacefulness++;
        }
    }

    private void mutateMutation() {
        if (cells.nextBoolean()) {
            mutation += .001;
        } else {
            mutation -= .001;
        }
    }

    private void mutateAge() {
        if (cells.nextBoolean()) {
            maxAge++;
        } else {
            maxAge--;
        }
    }

    private void mutateAggression() {
        if (energyStep > 2 && cells.nextBoolean()) {
            energyStep--;
        } else {
            energyStep++;
        }
    }

    private void mutateActs(CellMutator parent) {
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
        if (cells.nextBoolean()) {
            acts[cells.nextInt(acts.length)] = getRandAct();
        }
    }

    private byte getRandAct() {
        return (byte) (cells.nextInt(10));
    }

    @Override
    void calcColors() {
        color_complexity = ((int) ((255f * acts.length) / MAX_GENE_LENGTH) << 16) | 0x40;
        color_generations = cells.nextInt(0xffffff);
        color_special = ((int) (127f + 127f * Math.tanh(energyStep / 25. - 1)) & 0xff) << 16;
    }


    @Override
    public void prepare() {
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
        energy -= energy_minus * energyStep;
        notReady = false;
    }

    @Override
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
    boolean checkStats() {
        if (energy >= cells.energyLim * .5) {
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
        if (age++ > maxAge && maxAge <= 200) {
            kill();
            return false;
        }

        return true;
    }

    @Override
    void split() {
        int[] free = getFreeCell();
        if (free != null) {
            energy = (int) (energy * .4);
            cells.newCell(new CellMutator(this, free[0], free[1]));
        }
    }

    private int observe() {   // 0:good 2:wall/relative 4:darker 6:darker then energystep 8:danger
        int new_x = DIRS[dir][0] + x, new_y = DIRS[dir][1] + y;

        if (!cells.checkBounds(new_x, new_y))  //out of field
            return 2;

        else if (cells.hasCell(new_x, new_y)) {
            CellMutator c = (CellMutator) cells.getCell(new_x, new_y);
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
            if (cells.lightMap[x][y] < energyStep) {
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
            CellMutator c = (CellMutator) cells.getCell(new_x, new_y);
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

    @Override
    boolean isWeaker(Cell c) {
        return energyStep > ((CellMutator) c).energyStep;
    }

    @Override
    void eraseSelf() {
        super.eraseSelf();
    }

    @Override
    void grow() {
        energy += cells.lightMap[x][y];
    }

    @Override
    void kill() {
        super.kill();
    }

    @Override
    void starve() {
        super.starve();
    }

    @Override
    public String toString() {
        return String.format("gen: %d peacefulness: %d mutFactor: %.3f energy: %.1f energy step: %d max age %d gene: " + Arrays.toString(acts),
                generation, peacefulness, mutation, energy, energyStep, maxAge);
    }
}
