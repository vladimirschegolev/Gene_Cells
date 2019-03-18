package main;

import java.awt.*;
import java.util.Arrays;

public class CellNeuro extends Cell {

    private final static byte[][] dirs = new byte[][]{{0, 1}, {1, 0}, {1, 1}, {0, -1}, {-1, 0}, {-1, -1}, {1, -1}, {-1, 1}};
    private float[] input_light = new float[8];
    private float[] input_cells = new float[8];

    private float[] hidden = new float[8];
    private float[] output = new float[9];

    private float[][] weights_light, weights_cells, weights_hidden;

    public CellNeuro(int x, int y) {
        this.x = x;
        this.y = y;

        energy = 100;

        weights_light = new float[input_light.length][hidden.length];
        fillRandom(weights_light);

        weights_cells = new float[input_cells.length][hidden.length];
        fillRandom(weights_cells);

        weights_hidden = new float[hidden.length][output.length];
        fillRandom(weights_hidden);

        color = new Color(Cells.random.nextInt());
    }

    private CellNeuro(CellNeuro parent, int xx, int yy) {
        x = xx;
        y = yy;
        generation = parent.generation;
        if (Cells.random.nextFloat() > Cells.mutation) {
            weights_hidden = parent.weights_hidden;
            weights_cells = parent.weights_cells;
            weights_light = parent.weights_light;
            color = parent.color;

        } else {
            generation++;
            weights_hidden = new float[parent.weights_hidden.length][parent.weights_hidden[0].length];
            weights_cells = new float[parent.weights_cells.length][parent.weights_cells[0].length];
            weights_light = new float[parent.weights_light.length][parent.weights_light[0].length];
            for (int i = 0; i < parent.weights_hidden.length; i++) {
                for (int j = 0; j < parent.weights_hidden[i].length; j++) {
                    if (Cells.random.nextFloat() > Cells.mutation) {
                        weights_hidden[i][j] = parent.weights_hidden[i][j];
                    } else {
                        if (Cells.random.nextBoolean()) {
                            weights_hidden[i][j] = parent.weights_hidden[i][j] * 1.1f;
                        } else {
                            weights_hidden[i][j] = parent.weights_hidden[i][j] / 1.1f;
                        }
                    }
                }
            }

            for (int i = 0; i < parent.weights_cells.length; i++) {
                for (int j = 0; j < parent.weights_cells[i].length; j++) {
                    if (Cells.random.nextFloat() > Cells.mutation) {
                        weights_cells[i][j] = parent.weights_cells[i][j];
                    } else {
                        if (Cells.random.nextBoolean()) {
                            weights_cells[i][j] = parent.weights_cells[i][j] * 1.1f;
                        } else {
                            weights_cells[i][j] = parent.weights_cells[i][j] / 1.1f;
                        }
                    }
                }
            }

            for (int i = 0; i < parent.weights_light.length; i++) {
                for (int j = 0; j < parent.weights_light[i].length; j++) {
                    if (Cells.random.nextFloat() > Cells.mutation) {
                        weights_light[i][j] = parent.weights_light[i][j];
                    } else {
                        if (Cells.random.nextBoolean()) {
                            weights_light[i][j] = parent.weights_light[i][j] * 1.1f;
                        } else {
                            weights_light[i][j] = parent.weights_light[i][j] / 1.1f;
                        }
                    }
                }
            }

            color = new Color((int) (parent.color.getRGB() *  1.1));
        }
    }

    private void fillRandom(float[][] input) {
        for (int i = 0; i < input.length; i++) {
            for (int j = 0; j < input[i].length; j++) {
                input[i][j] = Cells.random.nextFloat();
            }
        }
    }

    private void multVector(float[] vector, float[][] matrix, float[] target) {
        for (int i = 0; i < vector.length; i++) {
            for (int j = 0; j < target.length; j++) {
                target[j] += vector[i] * matrix[i][j];
            }
        }

    }

    private void normalise(float[] input) {
        float max = 0;

        for (float v : input) {
            float k = Math.abs(v);
            if (max < k) max = k;
        }
        for (int i = 0; i < input.length; i++) {
            input[i] /= max;
        }

    }

    private void clean(float[] input) {
        for (int i = 0; i < input.length; i++) {
            input[i] = 0;
        }
    }

    @Override
    boolean act() {
        observe();

        clean(hidden);
        clean(output);

        multVector(input_light, weights_light, hidden);
        multVector(input_cells, weights_cells, hidden);
        normalise(hidden);
        multVector(hidden, weights_hidden, output);
        float max = 0;
        int index = 8;
        for (int i = 0; i < output.length; i++) {
            if (max < output[i]) {
                index = i;
                max = output[i];
            }
        }



        if (index == 8) {
            grow();
        } else {
            if(move(index)) {
                return false;
            }
        }

        energy -= Cells.energyStep;

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

    private boolean move(int index) {
        int xx = dirs[index][0] + x;
        int yy = dirs[index][1] + y;
        if (Cells.check(xx,yy)) {
            if (Cells.hasCell(xx, yy)) {
                CellNeuro c = (CellNeuro) Cells.getCell(xx, yy);
                if (c.isAlive()) {
                    if (!isRelative(c)) {
                        if (isWeaker(c)) {
                            eatCell(c);
                            c.erase();
                        } else {
                            kill();
                            return true;
                        }
                    }
                } else {
                    eatCell(c);
                    step(xx, yy);
                }
            } else {
                step(xx, yy);
            }
        }
        return false;
    }

    private void observe() {
        float max = 0;
        for (int i = 0; i < dirs.length; i++) {
            int xx = dirs[i][0] + x;
            int yy = dirs[i][1] + y;
            if (Cells.check(xx,yy)) {
                input_light[i] = Cells.lightMap[xx][yy];
                if (max < input_light[i]) max = input_light[i];
                if (Cells.hasCell(xx,yy)) {
                    Cell c = Cells.getCell(xx,yy);
                    if (c.isAlive()) {
                        if (isRelative(c)) {
                            input_light[i] = -1;
                        } else if (isWeaker(c)) {
                            input_light[i] = 1;
                        } else {
                            input_light[i] = -1;
                        }
                    } else {
                        input_cells[i] = 1;
                    }
                } else {
                    input_cells[i] = 0;
                }
            } else {
                input_light[i] = -1;
                input_cells[i] = -1;
            }

        }
        for (int i = 0; i < input_light.length; i++) {
            input_light[i] /= max;
        }
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
                Cells.setCell(xx, yy, new CellNeuro(this, xx, yy));
                Cells.queue.add(Cells.cells[xx][yy]);
                return;
            }
        }
    }
    
    private boolean isWeaker(Cell c) {
        return c.energy < energy;
    }

    private boolean isRelative(Cell c) {
        return Math.abs(c.generation - generation) < Cells.peacefulness;
    }

    @Override
    public String toString() {
        return String.format("gen: %d energy: %f output: " + Arrays.toString(output), generation, energy);
    }
}
