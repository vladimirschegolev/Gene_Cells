package main;

import java.util.*;


class Cells {
    static Cell[][] cells;
    static float[][] lightMap;
    static Queue<Cell> queue;
    static Random random = new Random();
    private static int count;
    private static final double ROTATE_SPEED = Math.PI / 200;

    static int peacefulness = 10;
    static float mutation = .08f;
    static int lightPower = 150;
    static int energyStep = 25;
    static int energySplitDeathGap = 50;
    static int energyLim = 1000;


    static void init(int x, int y) {
        random.setSeed(5454545);
        cells = new Cell[x][y];
        lightMap = new float[x][y];

        calcLightMap();

        cells[x / 2][y / 2] = new CellGeneArray(x / 2, y / 2);
//        cells[x / 2][y / 2] = new CellNeuro(x / 2, y / 2);

        queue = new LinkedList<>();
        queue.add(cells[x / 2][y / 2]);

        count = 0;

    }

    static void calcLightMap() {
        calcGaussMap(lightPower, lightMap.length / 2, lightMap[0].length / 2);
    }

    static void calcLightMapDynamic() {
        int w = lightMap.length;
        int h = lightMap[0].length;
        int x_0 = (int) (w * (Math.sin(ROTATE_SPEED * count) + 2) / 4);
        int y_0 = (int) (h * (Math.cos(ROTATE_SPEED * count) + 2) / 4);
        calcGaussMap(lightPower, x_0, y_0);
        count++;
    }

    private static void calcGaussMap(float sigma, int x_0, int y_0) {
        double sigma2 = sigma * sigma;
        for (int x = 0; x < lightMap.length; x++) {
            int off_x = x_0 - x;
            for (int y = 0; y < lightMap[x].length; y++) {
                int off_y = y_0 - y;
                lightMap[x][y] = (float) (sigma * Math.exp(-(off_x * off_x + off_y * off_y)/sigma2));
            }
        }
    }

    static int getWidth() {
        return cells.length;
    }

    static int getHeight() {
        return cells[0].length;
    }


    static boolean DoTick() {
        if (queue.size() > 0) {
            int size = queue.size();
            for (int i = 0; i < size; i++) {
                Cell c = queue.poll();
                if (c != null && c == cells[c.x][c.y]) {
                    if (c.isAlive()) {
                        if (c.act()) {
                            queue.add(c);
                        }
                    } else {
                        cells[c.x][c.y] = null;
                    }

                }

            }
            return true;
        } else {
            return false;
        }

    }


    static boolean check(int x, int y) {
        return x >= 0 && y >= 0 && x < cells.length && y < cells[0].length;
    }

    static boolean hasCell(int x, int y) {
        return cells[x][y] != null;
    }

    static void deleteCell(int x, int y) {
        cells[x][y] = null;
    }

    static Cell getCell(int x, int y) {
        return cells[x][y];
    }

    static void setCell(int x, int y, Cell cell) {
        cells[x][y] = cell;
    }
}
