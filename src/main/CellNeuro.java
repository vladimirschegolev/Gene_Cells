package main;


import java.util.Arrays;

public class CellNeuro extends Cell {

    private final static byte[][] dirs = new byte[][]{{0, 1}, {1, 0}, {1, 1}, {0, -1}, {-1, 0}, {-1, -1}, {1, -1}, {-1, 1}};
    private final static int MAX_HIDDEN_SIZE = 20;
    private final static float WEIGHT_SHIFT = .9999f;
    private final static float WEIGHT_START = .999f;

    private float[] input = new float[17];
    private float[] hidden;
    private float[] output = new float[10];

    private int mut1, mut2, mut3;

    private float[][] weights_hidden, weights_input;

    CellNeuro(int _x, int _y, Cells _cells) {
        cells = _cells;
        x = _x;
        y = _y;

        energy = 100;

        mut1 = 0;
        mut2 = 0;
        mut3 = 0;

        hidden = new float[1];

        weights_input = new float[input.length][hidden.length];

        for (int i = 0; i < input.length; i++) {
            weights_input[i][0] = 1;
        }

        weights_hidden = new float[hidden.length][output.length];

        for (int i = 0; i < output.length - 1; i++) {
            weights_hidden[0][i] = WEIGHT_START;
        }
        weights_hidden[0][8] = 1;

        color = 0x00ff00;
    }

    private CellNeuro(CellNeuro parent, int new_x, int new_y) {
        x = new_x;
        y = new_y;
        generation = parent.generation;
        energy = parent.energy;
        mut1 = parent.mut1;
        mut2 = parent.mut2;
        mut3 = parent.mut3;
        if (cells.random.nextFloat() > cells.mutation) {
            weights_hidden = parent.weights_hidden;
            weights_input = parent.weights_input;
            hidden = parent.hidden;
            color = parent.color;

        } else {
            generation++;
            switch (cells.random.nextInt(3)) {
                case 0:
                    if (cells.random.nextBoolean() && parent.hidden.length < MAX_HIDDEN_SIZE - 1) {
                        mut1++;
                        hidden = new float[parent.hidden.length + 1];
                        System.arraycopy(parent.hidden, 0, hidden, 0, parent.hidden.length);
                        hidden[parent.hidden.length] = WEIGHT_START;

                        weights_input = new float[input.length][hidden.length];

                        for (int i = 0; i < input.length; i++) {
                            for (int j = 0; j < hidden.length; j++) {
                                if (j < parent.hidden.length) {
                                    weights_input[i][j] = parent.weights_input[i][j];
                                } else {
                                    weights_input[i][j] = WEIGHT_START;
                                }
                            }
                        }

                        weights_hidden = new float[hidden.length][output.length];

                        for (int i = 0; i < hidden.length; i++) {
                            for (int j = 0; j < output.length; j++) {
                                if (i < parent.hidden.length) {
                                    weights_hidden[i][j] = parent.weights_hidden[i][j];
                                } else {
                                    weights_hidden[i][j] = WEIGHT_START;
                                }
                            }
                        }

                        break;
                    } else if (parent.hidden.length > 1 && parent.hidden.length < MAX_HIDDEN_SIZE - 1) {
                        mut1++;
                        hidden = new float[parent.hidden.length - 1];
                        System.arraycopy(parent.hidden, 0, hidden, 0, hidden.length);

                        weights_input = parent.weights_input;
                        weights_hidden = parent.weights_hidden;

                        break;
                    }
                case 1:
                    mut2++;
                    hidden = parent.hidden;

                    weights_input = parent.weights_input;

                    weights_hidden = new float[hidden.length][output.length];

                    mutArray(weights_hidden, parent.weights_hidden);

                    break;
                case 2:
                    mut3++;
                    hidden = parent.hidden;

                    weights_hidden = parent.weights_hidden;

                    weights_input = new float[input.length][hidden.length];

                    mutArray(weights_input, parent.weights_input);

                    break;
            }


            calcColor(parent);
        }
    }

    private void calcColor(CellNeuro parent) {
        color = parent.color;
        if (mut1 != parent.mut1) color += 21 << 16;
        if (mut2 != parent.mut2) color += 21 << 8;
        if (mut3 != parent.mut3) color += 21;
    }

    private void mutArray(float[][] target, float[][] source) {
        int j = cells.random.nextInt(target[0].length);
        for (int i = 0; i < target.length; i++) {
                if (cells.random.nextBoolean())
                    target[i][j] = source[i][j] * WEIGHT_SHIFT;
                else
                    target[i][j] = source[i][j] / WEIGHT_SHIFT;
            }

    }


    private void multVector(float[] vector, float[][] matrix, float[] target) {
        for (int i = 0; i < vector.length; i++) {
            for (int j = 0; j < target.length; j++) {
                target[j] += vector[i] * matrix[i][j];
            }
        }

    }

    private void normalize(float[] input) {
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

        hidden = new float[hidden.length];
        output = new float[output.length];

        multVector(input, weights_input, hidden);
        normalize(hidden);
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
        } else if (index == 9) {
            float min = 100;
            for (int i = 0; i < 8; i++) {
                if (min > output[i]) {
                    index = i;
                    min = output[i];
                }
            }
            if (index < 8) share(index);
        } else if (move(index)) {
            return false;
        }


        energy -= cells.energyStep * hidden.length;

        if (energy >= (cells.energyLim * cells.energySplitDeathGap) / 100) {
            split();
        }
        if (energy > cells.energyLim) {
            energy = cells.energyLim;
            kill();
            return false;
        }
        if (energy < 0) {
            energy = cells.energyStep;
            starve();
            return false;
        }


        return true;
    }

    @Override
    int getComplexity() {
        int l = (int) ((255f * hidden.length) / MAX_HIDDEN_SIZE);
        return (l  << 16) | 0x40;
    }

    private void share(int index) {
        int px = dirs[index][0] + x;
        int py = dirs[index][1] + y;
        if (cells.checkBounds(px, py)) {
            if (cells.hasCell(px, py)) {
                Cell c = cells.getCell(px, py);
                if (c.isAlive()) {
                    energy /= 2;
                    c.energy += energy;
                }
            }
        }
    }


    private boolean move(int index) {
        int xx = dirs[index][0] + x;
        int yy = dirs[index][1] + y;
        if (cells.checkBounds(xx, yy)) {
            if (cells.hasCell(xx, yy)) {
                CellNeuro c = (CellNeuro) cells.getCell(xx, yy);
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
            if (cells.checkBounds(xx, yy)) {
                input[i] = cells.lightMap[xx][yy];
                if (max < input[i]) max = input[i];
                if (cells.hasCell(xx, yy)) {
                    Cell c = cells.getCell(xx, yy);
                    if (c.isAlive()) {
                        if (isRelative(c))
                            input[i + 8] = -.5f;
                        else
                            input[i + 8] = energy / c.energy;
                    } else {
                        input[i + 8] = 1f;
                    }
                } else {
                    input[i + 8] = 0f;
                }
            } else {
                input[i] = -1f;
                input[i + 8] = -1f;
            }

        }
        for (int i = 0; i < 8; i++) {
            input[i] /= max;
        }
        input[16] = energy / cells.energyLim;
    }

    private void grow() {
        energy += cells.lightMap[x][y];
    }


    private void split() {
        int[] free = getFreeCell();
        if (free != null) {
            energy = (int) (energy * .4);
            cells.newCell(new CellNeuro(this, free[0], free[1]));
        }
    }

    private boolean isWeaker(Cell c) {
        return c.energy < energy;
    }

    private boolean isRelative(Cell c) {
        return Math.abs(c.generation - generation) < cells.peacefulness;
    }

    @Override
    public String toString() {
        return String.format("gen: %d energy: %f hidden: " + Arrays.toString(hidden), generation, energy);
    }
}
