package main;

import java.util.ArrayDeque;
import java.util.Queue;

public class CellsParallel extends Cells {

    private static CellsParallel cells;

    private Queue<Cell> queue;

    public boolean parallel = false;

    static CellsParallel getInstance() {
        if (cells == null) {
            cells = new CellsParallel();
        }
        return cells;
    }

    public void init(int x, int y, int type) {
        image = new ImageData(x, y);
        setSeed(42);
        cells_array = new Cell[x][y];
        lightMap = new float[x][y];

        width = x;
        height = y;
        this.type = type;

        calcLightMap();
        if (queue == null) queue = new ArrayDeque<>();
        queue.clear();

        if (type == 0) newCell(new CellGeneArray(x / 2, y / 2, this));
        else if (type == 1) newCell(new CellNeuro(x / 2, y / 2, this));
        else newCell(new CellMutator(x / 2, y / 2, this));

        count = 0;
        repaint();
    }

    public boolean DoTick() {
        if (queue.size() > 0) {
            int size = queue.size();

            if (parallel)
                queue.parallelStream().forEach(Cell::prepare);

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

}
