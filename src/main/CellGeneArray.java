package main;

import java.awt.*;
import java.util.Arrays;


class CellGeneArray extends Cell {

    private byte dir;
    private int index = 0;
    private byte[] acts;
    private float aggression = 1;


    private final static byte[][] dirs = new byte[][]{{0, 1}, {1, 0}, {1, 1}, {0, -1}, {-1, 0}, {-1, -1}, {1, -1}, {-1, 1}};

    private final static int geneLength = 60;


    CellGeneArray(int x, int y) {
        this.x = x;
        this.y = y;
        energy = 50;
        dir = (byte) Cells.random.nextInt(8);
        acts = new byte[]{0};
        color = Color.GREEN;
        generation = 1;

    }

    private CellGeneArray(CellGeneArray parent, int x, int y) {
        this.x = x;
        this.y = y;
        energy = parent.energy;
        dir = (byte) Cells.random.nextInt(8);
        generation = parent.generation;

        aggression = parent.aggression;

        if (Cells.random.nextFloat() < Cells.mutation) {
            generation = parent.generation + 1;

            double r = Cells.random.nextDouble();
            if (parent.acts.length < geneLength && r > .66) {
                acts = new byte[parent.acts.length + 1];
                System.arraycopy(parent.acts, 0, acts, 0, parent.acts.length);
                acts[parent.acts.length] = getRandAct();
            } else if (parent.acts.length > 1 && r > .33) {
                int remove = Cells.random.nextInt(parent.acts.length);
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
                acts[Cells.random.nextInt(acts.length)] = getRandAct();
            }
            if (Cells.random.nextBoolean()) {
                aggression *= 1.01;
            } else {
                aggression /= 1.01;
            }
            color = calcColor();
        } else {
            acts = Arrays.copyOf(parent.acts, parent.acts.length);
            color = parent.color;
        }

    }

    private byte getRandAct() {
        return (byte) (Cells.random.nextInt(11) - 5);
    }


    private Color calcColor() {
        int r, g, b;

        b = (acts.length * 15) % 255;
        g = Math.abs(200 - generation * 10) % 255;
        r = (int) ((aggression * 100) % 255);
        return new Color(r, g, b);
    }

    public boolean act() {
        cycle:
        for (int count = 0; count < 20; count++) {
            energy -= Cells.energyStep * aggression;
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
                    energy += Cells.energyStep * aggression * count;
                    break cycle;

            }
        }


        if (energy >= (Cells.energyLim * Cells.energySplitDeathGap) / 100) {
            split();
        }
        if (energy > Cells.energyLim) {
            energy = Cells.energyLim;
            kill();
            return false;
        }
        if (energy < 0) {
            energy = Cells.energyStep;
            starve();
            return false;
        }

        return true;
    }


    private void grow() {
        energy += Cells.lightMap[x][y] / aggression;
    }

    private void split() {

        for (int i = 0; i < 8; i++) {
            int j = Cells.random.nextInt(8);
            int xx = x + dirs[j][0];
            int yy = y + dirs[j][1];
            if (Cells.check(xx, yy) && !Cells.hasCell(xx, yy)) {
                energy = (int) (energy * .4);
                Cells.setCell(xx, yy, new CellGeneArray(this, xx, yy));
                Cells.queue.add(Cells.cells[xx][yy]);
                return;
            }
        }
    }

    private int observe() {   // 0:good 1:wall/relative 2:darker 3:darker then energystep 4:danger
        int xx = dirs[dir][0] + x, yy = dirs[dir][1] + y;

        if (!Cells.check(xx, yy))  //out of field
            return 1;

        else if (Cells.hasCell(xx, yy)) {
            CellGeneArray c = (CellGeneArray) Cells.getCell(xx, yy);
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
            if (Cells.lightMap[x][y] < Cells.energyStep) {
                return 3;
            } else if (Cells.lightMap[xx][yy] < Cells.lightMap[x][y]){
                return 2;
            } else {
                return 0;
            }
        }
    }

    private boolean move() {

        int newX = dirs[dir][0] + this.x;
        int newY = dirs[dir][1] + this.y;
        if (!Cells.check(newX, newY))
            return false;
        if (Cells.hasCell(newX, newY)) {
            CellGeneArray c = (CellGeneArray) Cells.getCell(newX, newY);
            if (c.isAlive()) {
                if (!isRelative(c)) {
                    if (isWeaker(c)) {
                        eatAliveCell(c);
                        c.kill();
                        c.erase();
//                        step(newX, newY);
                    } else {
                        c.eatAliveCell(this);
                        kill();
                        return true;
                    }
                }
            } else {
                eatCell(c);
                step(newX, newY);
            }
        } else {
            step(newX, newY);
        }
        return false;
    }


    private boolean isRelative(CellGeneArray c) {
        return Cells.peacefulness >= Math.abs(acts.length - c.acts.length) +
                Math.abs(aggression - c.aggression) * 100 +
                Math.abs(generation - c.generation);
    }

    private boolean isWeaker(CellGeneArray c) {
        return aggression * energy > c.aggression * c.energy;
    }

    private void eatAliveCell(CellGeneArray c) {
        energy -= c.energy * c.aggression / aggression;
    }


    @Override
    public String toString() {
        return String.format("gen: %d energy: %f aggression: %f gene: " + Arrays.toString(acts),
                generation, energy, aggression);
    }
}
