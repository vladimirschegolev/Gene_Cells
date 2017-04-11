package main;

import java.awt.*;
import java.util.Arrays;


public class Cell {

    private byte dir;
    private boolean alive = true;
    int energy, x, y;

    byte[] acts;
    int generation;
    Color color;


    static byte[][] dirs = new byte[][]{{0, 1}, {1, 0}, {1, 1}, {0, -1}, {-1, 0}, {-1, -1}, {1, -1}, {-1, 1}};
    static int peacefulness = 10;
    static float mutation = .08f;
    static int lightPower = 250;
    static int energyStep = 25;
    static int energySptitDeathGap = 400;
    static int energyLim = 1000;
    static int energyCadaver = 500;


    private Cell() {
    }


    public Cell(int x, int y) {
        this.x = x;
        this.y = y;
        energy = 50;
        dir = (byte) (Math.random() * 8);
        acts = new byte[]{0};
        color = Color.GREEN;
        generation = 1;
    }

    public Cell(Cell parent, int x, int y) {
        this.x = x;
        this.y = y;
        energy = parent.energy;
        dir = (byte) (Math.random() * 8);
        generation = parent.generation;
        if (Math.random() < mutation) {
            generation = parent.generation + 1;

            if (Math.random() > .8) {
                acts = new byte[parent.acts.length + 1];
                System.arraycopy(parent.acts, 0, acts, 0, parent.acts.length);
                acts[parent.acts.length] = (byte) (Math.random() * 5);
            } else {
                acts = Arrays.copyOf(parent.acts, parent.acts.length);
                acts[(int) (Math.random() * acts.length)] = (byte) (Math.random() * 5);
            }


            color = calcColor();
        } else {
            acts = Arrays.copyOf(parent.acts, parent.acts.length);
            color = parent.color;
        }

    }

    private Color calcColor() {
        int sum = 0, r, g, b;
        for (int i = 0; i < acts.length; i++) {
            sum += acts[i];
        }

        r = (acts.length * 37+ 100) % 255;
        g = (sum * 7 + 100) % 255;
        b = (generation * 3) % 255;
        return new Color(r, g, b);
    }

    public boolean act() {
        energy -= energyStep * acts.length;
        cycle:
        for (int i = 0; i < acts.length; i++) {

            switch (acts[i]) {
                case 0:
                    grow();
                    break cycle;
                case 1:
                    i += observe();
                    break;
                case 2:
                    if (++dir > 7) dir = 0;
                    break;
                case 3:
                    if (--dir < 0) dir = 7;
                    break;
                case 4:
                    if (move()) {
                        Cells.deleteCell(x,y);
                        return false;
                    }
                    break cycle;

            }
        }


        if (energy >= energyLim - energySptitDeathGap) {
            split();
        }
        if ((energy > energyLim || energy <= 0)) {
            kill();
            return false;
        }

        return true;
    }

    private void grow() {
        energy += Cells.lightMap[x][y];
    }

    private void split() {

        int rand = (int) (Math.random() * 50);
        for (int i = 0; i < 8; i++) {
            int j = (i + rand) % 8;
            int xx = x + dirs[j][0];
            int yy = y + dirs[j][1];
            if (Cells.check(xx, yy) && !Cells.hasCell(xx, yy)) {
                energy = energy / 2;
                Cells.setCell(xx, yy, new Cell(this, xx, yy));
                Cells.queue.add(Cells.cells[xx][yy]);
                return;
            }
        }
    }

    private int observe() {   // 1 - bad, 0 - good
        int xx = dirs[dir][0] + x, yy = dirs[dir][1] + y;

        if (xx < 0 || xx >= Cells.getWidth() || yy < 0 || yy >= Cells.getHeight())
            return 1;

        else if (Cells.hasCell(xx, yy)) {
            Cell c = Cells.getCell(xx, yy);
            if (!c.isAlive()) {
                return 0;
            } else {
                if (peacefulness > Math.abs(c.generation - generation)) {
                    return 1;
                } else if (acts.length > c.acts.length) {
                    return 0;
                } else {
                    return 1;
                }
            }
        } else {
            if (Cells.lightMap[x][y] > Cells.lightMap[xx][yy] || Cells.lightMap[xx][yy] == 0) {
                return 1;
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
            Cell c = Cells.getCell(newX, newY);
            if (c.isAlive()) {
                if (peacefulness < Math.abs(c.generation - generation)) {
                    if (acts.length > c.acts.length) {
                        energy += energyCadaver + c.energy;
                        c.kill();
                        step(newX, newY);
                    } else {
                        c.energy += energy + energyCadaver;
                        return true;
                    }
                }
            } else {
                energy += energyCadaver;
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

    public boolean isAlive() {
        return alive;
    }

    public Cell kill() {
        alive = false;
        color = Color.LIGHT_GRAY;
        return this;
    }

}
