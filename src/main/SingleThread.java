package main;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.Queue;

public class SingleThread extends Cells {

    private static SingleThread cells;

    Queue<Cell> queue;

    public static SingleThread getInstance() {
        if (cells == null) {
            cells = new SingleThread();
        }
        return cells;
    }

    public void init(int x, int y, int type) {
        image = new ImageData(x, y);
        random.setSeed(42);
        cells_array = new Cell[x][y];
        lightMap = new float[x][y];

        width = x;
        height = y;

        calcLightMap();
        if (queue == null) queue = new ArrayDeque<>();
        queue.clear();

        if (type == 0) newCell(new CellGeneArray(x / 2, y / 2 , this));
        else newCell(new CellNeuro(x / 2, y / 2 , this));

        count = 0;
        repaint();
    }

    public boolean DoTick() {
        if (queue.size() > 0) {
            int size = queue.size();
            for (int i = 0; i < size; i++) {
                Cell c = queue.poll();
                if (c != null && c == cells_array[c.x][c.y]) {
                    if (c.isAlive()) {
                        if (c.act()) {
                            queue.add(c);
                        }
                    } else {
                        cells_array[c.x][c.y] = null;
                    }

                }

            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void newCell(Cell cell) {
        cells_array[cell.x][cell.y] = cell;
        queue.add(cell);
    }

    @Override
    public int size() {
        return queue.size();
    }


//    @Override
//    public void repaint() {
//        for (int y = 0,subsum = 0; y < cells_array[0].length; y++) {
//            subsum = y * cells_array.length;
//            for (int x = 0; x < cells_array.length; x++) {
//                if (cells_array[x][y] == null) {
//                    image.buffer[x + subsum] = 0;
//                } else {
//                    image.buffer[x + subsum] = cells_array[x][y].color.getRGB();
//                }
//            }
//        }
//    }

//    @Override
//    public void paint(Graphics g, double sizeX, double sizeY) {
//        for (int i = 0; i < width; i++) {
//            int x = (int) (i * sizeX);
//            for (int j = 0; j < height; j++) {
//                if (hasCell(i, j)) {
//                    try {
//                        g.setColor(getCell(i, j).color);
//                    } catch (Exception ignored) { }
//                    g.fillRect(x, (int) (j * sizeY), (int) sizeX + 1, (int) sizeY + 1);
//                }
//            }
//        }
//    }
}
