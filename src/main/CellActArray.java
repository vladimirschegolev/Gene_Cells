package main;

import java.awt.*;
import java.util.Arrays;


class CellActArray extends Cell {

    private byte dir;
    private boolean alive = true;
    private int energy;
    private int index = 0;
    private byte[] acts;
    private int generation;



    private final static byte[][] dirs = new byte[][]{{0, 1}, {1, 0}, {1, 1}, {0, -1}, {-1, 0}, {-1, -1}, {1, -1}, {-1, 1}};

    private final static int geneLength = 60;


    CellActArray(int x, int y) {
        this.x = x;
        this.y = y;
        energy = 50;
        dir = (byte) Cells.random.nextInt(8);
        acts = new byte[]{0};
        color = Color.GREEN;
        generation = 1;
    }

    private CellActArray(CellActArray parent, int x, int y) {
        this.x = x;
        this.y = y;
        energy = parent.energy;
        dir = (byte) Cells.random.nextInt(8);
        generation = parent.generation;

        if (Cells.random.nextDouble() < Cells.mutation) {
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
            color = calcColor(parent);
        } else {
            acts = Arrays.copyOf(parent.acts, parent.acts.length);
            color = parent.color;
        }
    }

    private byte getRandAct() {
        return (byte) (Cells.random.nextInt(11) - 5);
    }


    private Color calcColor(CellActArray parent) {
        int r, g, b;

        r = (acts.length * 15) % 255;
        g = Math.abs(200 - generation * 10) % 255;
        b = (parent.color.getBlue() + 5) % 255;
        return new Color(r, g, b);
    }

    public boolean act() {
        cycle:
        for (int count = 0; count < 20; count++) {
            energy -= Cells.energyStep;
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
                        Cells.deleteCell(x, y);
                        return false;
                    }
                    break cycle;
                case 5:
                    index += acts[Math.abs(++index % acts.length)];

            }
        }


        if (energy >= (Cells.energyLim * Cells.energySplitDeathGap)/100) {
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
        energy += Cells.lightMap[x][y];
    }

    private void split() {

        for (int i = 0; i < 8; i++) {
            int j = Cells.random.nextInt(8);
            int xx = x + dirs[j][0];
            int yy = y + dirs[j][1];
            if (Cells.check(xx, yy) && !Cells.hasCell(xx, yy)) {
                energy = (int) (energy * .4);
                Cells.setCell(xx, yy, new CellActArray(this, xx, yy));
                Cells.queue.add(Cells.cells[xx][yy]);
                return;
            }
        }
    }

    private int observe() {   // 0 - good
        int xx = dirs[dir][0] + x, yy = dirs[dir][1] + y;

        if (xx < 0 || xx >= Cells.getWidth() || yy < 0 || yy >= Cells.getHeight())  //out of field
            return 1;

        else if (Cells.hasCell(xx, yy)) {
            CellActArray c = (CellActArray) Cells.getCell(xx, yy);
            if (!c.isAlive()) {
                return 0;
            } else {
                if (isRelative(c)) {
                    return 1;
                } else if (isWeaker(c)) {
                    return 0;
                } else {
                    return 2;
                }
            }
        } else {
            if (Cells.lightMap[x][y] > Cells.lightMap[xx][yy] || Cells.lightMap[xx][yy] == 0) {
                return 3;
            } else {
                return 0;
            }
        }
    }

    private boolean move() {

        int newX = dirs[dir][0] + this.x;
        int newY = dirs[dir][1] + this.y;
        if (newX < 0 || newX >= Cells.getWidth() ||
                newY < 0 || newY >= Cells.getHeight())
            return false;
        if (Cells.hasCell(newX, newY)) {
            CellActArray c = (CellActArray) Cells.getCell(newX, newY);
            if (c.isAlive()) {
                if (isRelative(c)) {
                    if (isWeaker(c)) {
                        eatCell(c);
                        c.kill();
                        c.erase();
//                        step(newX, newY);
                    } else {
//                        c.eatCell(this);
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


    private void step(int x, int y) {
        Cells.setCell(x, y, this);
        Cells.setCell(this.x, this.y, null);
        this.x = x;
        this.y = y;
    }

    private void erase() {
        Cells.setCell(this.x, this.y, null);
    }

    public boolean isAlive() {
        return alive;
    }

    private void kill() {
        alive = false;
        color = Color.LIGHT_GRAY;
    }

    private void starve() {
        alive = false;
        color = Color.DARK_GRAY;
    }

    private boolean isRelative(CellActArray c) {
        return Cells.peacefulness < Math.abs(c.generation - generation);
    }

    private boolean isWeaker(CellActArray c) {
        return energy  > c.energy ;
    }

    private void eatCell(CellActArray c) {
        energy += c.energy;
    }

    @Override
    public String toString() {
        return String.format("gen: %d energy: %d gene length: %d", generation, energy, acts.length);
    }
}
