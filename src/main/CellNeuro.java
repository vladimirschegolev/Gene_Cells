package main;


import java.util.Arrays;

public class CellNeuro extends Cell {

    private final static byte[][] dirs = new byte[][]{{0, 1}, {1, 0}, {1, 1}, {0, -1}, {-1, 0}, {-1, -1}, {1, -1}, {-1, 1}};
    private final static int MAX_HIDDEN_SIZE = 20;
    private final static float WEIGHT_START = .999f;

    private float[] input = new float[17];
    private float[] hidden;
    private float[] output = new float[10];
    private float[][] weights_hidden, weights_input;

    private int mut1, mut2, mut3;

    private boolean notReady = true;
    private int action;

    CellNeuro(int _x, int _y, Cells _cells) {
        init(_cells);
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

        color_generation = 0x00ff00;
        color_complexity = ((int) ((255f * hidden.length) / MAX_HIDDEN_SIZE) << 16) | 0x40;
    }

    private CellNeuro(CellNeuro parent, int new_x, int new_y) {
        x = new_x;
        y = new_y;
        generation = parent.generation;
        energy = parent.energy;
        mut1 = parent.mut1;
        mut2 = parent.mut2;
        mut3 = parent.mut3;
        if (cells.nextFloat() > cells.mutation) {
            weights_hidden = parent.weights_hidden;
            weights_input = parent.weights_input;
            hidden = parent.hidden;
            color_generation = parent.color_generation;
            color_complexity = parent.color_complexity;
        } else {
            generation++;
            switch (cells.nextInt(3)) {
                case 0:
                    if (cells.nextBoolean() && parent.hidden.length < MAX_HIDDEN_SIZE - 1) {
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
        color_generation = parent.color_generation;
        if (mut1 != parent.mut1) color_generation += 21 << 16;
        if (mut2 != parent.mut2) color_generation += 21 << 8;
        if (mut3 != parent.mut3) color_generation += 21;
        color_complexity = ((int) ((255f * hidden.length) / MAX_HIDDEN_SIZE) << 16) | 0x40;
    }

    private void mutArray(float[][] target, float[][] source) {
        for (int i = 0; i < target.length; i++) {
            for (int j = 0; j < target[i].length; j++) {
                if (cells.nextBoolean())
                    target[i][j] = source[i][j] * (1 - cells.mutation);
                else
                    target[i][j] = source[i][j] / (1 - cells.mutation);
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

    private void normalize(float[] input) {
        float max = 0;

        for (float v : input) {
            float k = Math.abs(v);
            if (max < k) max = k;
        }
        if (max == 0) return;
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
    public void prepare() {
        observe();

        clean(hidden);
        clean(output);

        hidden = new float[hidden.length];
        output = new float[output.length];

        multVector(input, weights_input, hidden);
        normalize(hidden);
        multVector(hidden, weights_hidden, output);
        float max = 0;
        action = 8;
        for (int i = 0; i < output.length; i++) {
            if (max < output[i]) {
                action = i;
                max = output[i];
            }
        }
        notReady = false;
    }

    @Override
    boolean act() {

//        if (age++ > 100) {
//            kill();
//            return false;
//        }

        if (notReady) {
            prepare();
        }
        notReady = true;
        if (action == 8) {
            grow();
        } else if (action == 9) {
            float min = 100;
            for (int i = 0; i < 8; i++) {
                if (min > output[i]) {
                    action = i;
                    min = output[i];
                }
            }
            if (action < 8) share(action);
        } else if (move(action)) {
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
        for (int i = 0, k = 8; i < dirs.length; i++, k++) {
            int xx = dirs[i][0] + x;
            int yy = dirs[i][1] + y;
            if (cells.checkBounds(xx, yy)) {
                input[i] = cells.lightMap[xx][yy];
                if (max < input[i]) max = input[i];
                if (cells.hasCell(xx, yy)) {
                    Cell c = cells.getCell(xx, yy);
                    if (c.isAlive()) {
                        if (isRelative(c))
                            input[k] = -.5f;
                        else if (c.energy > 1f)
                            input[k] = energy / c.energy;
                        else
                            input[k] = 0;
                    } else {
                        input[k] = 1f;
                    }
                } else {
                    input[k] = 0f;
                }
            } else {
                input[i] = -1f;
                input[k] = -1f;
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

    private boolean isWeaker(Cell cell) {
        CellNeuro c = (CellNeuro) cell;
        return energy > c.energy;
    }

    private boolean isRelative(Cell c) {
        return Math.abs(c.generation - generation) < cells.peacefulness;
    }

    @Override
    public String toString() {
        return String.format("gen: %d energy: %f hidden: " + Arrays.toString(hidden), generation, energy);
    }
}
