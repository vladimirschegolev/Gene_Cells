package main;


import java.util.Arrays;

public class CellNeuro extends Cell {

    private final static byte[][] dirs = new byte[][]{{0, 1}, {1, 0}, {1, 1}, {0, -1}, {-1, 0}, {-1, -1}, {1, -1}, {-1, 1}};
    private final static int MAX_HIDDEN_SIZE = 20;
    private final static float WEIGHT_START = .999f;

    private float[] input = new float[18];
    private float[] hidden;
    private float[] output = new float[10];
    private float[][] weights_hidden, weights_input;

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

        hidden = new float[2];

        weights_input = new float[input.length][hidden.length];

        for (int i = 0; i < input.length; i++) {
            weights_input[i][0] = 1;
        }

        weights_hidden = new float[hidden.length][output.length];

        for (int i = 0; i < output.length - 1; i++) {
            weights_hidden[0][i] = cells.nextFloat();
        }
        weights_hidden[0][8] = 1;

        color_family = 0x009900;
        calcColors();
    }

    private CellNeuro(CellNeuro parent, int new_x, int new_y) {
        x = new_x;
        y = new_y;
        energy = parent.energy;
        generation = parent.generation;
        aggression = parent.aggression;
        mut1 = parent.mut1;
        mut2 = parent.mut2;
        mut3 = parent.mut3;
        if (cells.nextFloat() > cells.mutation) {
            weights_hidden = parent.weights_hidden;
            weights_input = parent.weights_input;
            hidden = new float[parent.hidden.length];
            System.arraycopy(parent.hidden, 0, hidden, 0, parent.hidden.length);
            copyParentColors(parent);
        } else {
            generation++;
            switch (cells.nextInt(3)) {
                case 0:
                    mut1++;
                    if (parent.hidden.length == 1 || cells.nextBoolean() && parent.hidden.length < MAX_HIDDEN_SIZE - 1) {
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
                    } else {
                        hidden = new float[parent.hidden.length - 1];
                        System.arraycopy(parent.hidden, 0, hidden, 0, hidden.length);
                        weights_input = parent.weights_input;
                        weights_hidden = parent.weights_hidden;
                    }
                    break;
                case 1:
                    mut2++;
                    if (cells.nextBoolean()) {
                        hidden = new float[parent.hidden.length];
                        System.arraycopy(parent.hidden, 0, hidden, 0, parent.hidden.length);
                        weights_input = parent.weights_input;
                        weights_hidden = new float[hidden.length][output.length];
                        mutArray(weights_hidden, parent.weights_hidden);
                    } else {
                        hidden = new float[parent.hidden.length];
                        System.arraycopy(parent.hidden, 0, hidden, 0, parent.hidden.length);
                        weights_hidden = parent.weights_hidden;
                        weights_input = new float[input.length][hidden.length];
                        mutArray(weights_input, parent.weights_input);
                    }
                    break;
                case 2:
                    mut3++;
                    hidden = new float[parent.hidden.length];
                    System.arraycopy(parent.hidden, 0, hidden, 0, parent.hidden.length);
                    weights_input = parent.weights_input;
                    weights_hidden = parent.weights_hidden;
                    if (aggression < .5f || cells.nextBoolean() && aggression < 5) {
                        aggression /= .9f;
                    } else {
                        aggression *= .9f;
                    }
                    aggression2 = aggression * aggression2;
                    break;
            }
            calcNewColors(parent);
        }
    }

    @Override
    void calcColors() {
        color_complexity = ((int) ((255f * hidden.length) / MAX_HIDDEN_SIZE) << 16) | 0x40;
        color_generations = cells.nextInt(0xffffff);
        color_special = ((int) (127f + 127f * Math.tanh(aggression - 1)) & 0xff) << 16;

    }

    private void mutArray(float[][] target, float[][] source) {
        for (int i = 0; i < target.length; i++) {
            for (int j = 0; j < target[i].length; j++) {
                if (cells.nextBoolean())
                    target[i][j] = source[i][j] * (1 - cells.mutation / 10);
                else
                    target[i][j] = source[i][j] / (1 - cells.mutation / 10);
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

    private void clear(float[] input) {
        Arrays.fill(input, 0);
    }

    @Override
    public void prepare() {
        observe();

        clear(hidden);
        clear(output);

        multVector(input, weights_input, hidden);
        normalize(hidden);
        hidden[0] = 1;
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
    public boolean act() {
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


        energy -= cells.energyStep * aggression;

        return checkStats();
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
                            c.eraseSelf();
                        } else {
                            c.eatCell(this);
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
        input[17] = 1;
    }

    @Override
    void split() {
        int[] free = getFreeCell();
        if (free != null) {
            energy = (int) (energy * .4);
            cells.newCell(new CellNeuro(this, free[0], free[1]));
        }
    }

    @Override
    public String toString() {
        StringBuilder array = new StringBuilder();
        array.append('[');
        for (float v : hidden) {
            array.append(String.format("%2.1f|", v));
        }
        array.setCharAt(array.length() - 1, ']');
        return String.format("gen: %d mut: %d %d %d age: %d aggression: %1.3f energy: %2.1f hidden: " + array.toString(),
                generation, mut1, mut2, mut3, age, aggression, energy);
    }
}
