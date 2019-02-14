package main;

import java.util.*;


class Cells {
    static Cell[][] cells;
    static short[][] lightMap;
    static Queue<Cell> queue;
    static Random random = new Random();


    static void init(int x, int y) {
        cells = new Cell[x][y];
        lightMap = new short[x][y];

        calcLightMap();

        cells[x / 2][y / 2] = new Cell(x / 2, y / 2);

        queue = new LinkedList<>();
        queue.add(cells[x / 2][y / 2]);
        random.setSeed(42);

    }

    static void calcLightMap() {
        int x = lightMap.length;
        int y = lightMap[0].length;
        for (int xx = 0; xx < x; xx++) {
            for (int yy = 0; yy < y; yy++) {
                int offX = Math.abs(x / 2 - xx);
                int offY = Math.abs(y / 2 - yy);
                int dist = (int) Math.sqrt(offX * offX + offY * offY);
                if (dist < Cell.lightPower) {
                    lightMap[xx][yy] = (short) (Cell.lightPower - dist);
                } else {
                    lightMap[xx][yy] = 0;
                }
            }
        }
    }

    static void calcLightMapDynamic() {
        int x = lightMap.length;
        int y = lightMap[0].length;
        for (int xx = 0; xx < x; xx++) {
            for (int yy = 0; yy < y; yy++) {
                int offX = Math.abs(x / 2 - xx);
                int offY = Math.abs(y / 2 - yy);
                int dist = (int) Math.sqrt(offX * offX + offY * offY);
                if (dist < Cell.lightPower) {
                    lightMap[xx][yy] = (short) (Cell.lightPower - dist);
                } else {
                    lightMap[xx][yy] = 0;
                }
            }
        }
    }

    static int getWidth() {
        return cells.length;
    }

    static int getHeight() {
        return cells[0].length;
    }


    static void DoTick() {
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
                        Cells.cells[c.x][c.y] = null;
                    }

                }

            }

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
