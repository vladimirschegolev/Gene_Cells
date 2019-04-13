package main;


import java.awt.image.BufferedImage;
import java.util.*;


abstract class Cells {

    public static final int GENERATIONS = 0;
    public static final int ENERGY = 1;
    public static final int COMPLEXITY = 2;
    private static int colorType = 0;

    private final double ROTATE_SPEED = Math.PI / 200;
    float[][] lightMap;
    Random random = new Random();
    int count;
    int width, height;

    ImageData image;

    int peacefulness = 10;
    float mutation = .08f;
    int lightPower = 150;
    int energyStep = 25;
    int energySplitDeathGap = 50;
    int energyLim = 1000;
    Cell[][] cells_array;


    abstract void init(int x, int y, int type);

    abstract boolean DoTick();

    abstract void newCell(Cell cell);

    boolean checkBounds(int x, int y) {
        return x >= 0 && y >= 0 && x < cells_array.length && y < cells_array[0].length;
    }

    boolean hasCell(int x, int y) {
        return cells_array[x][y] != null;
    }

    void deleteCell(int x, int y) {
        cells_array[x][y] = null;
    }

    Cell getCell(int x, int y) {
        return cells_array[x][y];
    }

    void setCell(Cell cell) {
        cells_array[cell.x][cell.y] = cell;
    }

    int getWidth() {
        return width;
    }

    int getHeight() {
        return height;
    }

    void calcLightMapDynamic() {
        int w = lightMap.length;
        int h = lightMap[0].length;
        int x_0 = (int) (w * (Math.sin(ROTATE_SPEED * count) + 2) / 4);
        int y_0 = (int) (h * (Math.cos(ROTATE_SPEED * count) + 2) / 4);
        calcGaussMap(lightPower, x_0, y_0);
        count++;
    }

    boolean checkFree(int x, int y) {
        return checkBounds(x, y) && cells_array[x][y] == null;
    }

    void calcLightMap() {
        calcGaussMap(lightPower, lightMap.length / 2, lightMap[0].length / 2);
    }

    private void calcGaussMap(float sigma, int x_0, int y_0) {
        double sigma2 = sigma * sigma;
        for (int x = 0; x < lightMap.length; x++) {
            int off_x = x_0 - x;
            for (int y = 0; y < lightMap[x].length; y++) {
                int off_y = y_0 - y;
                lightMap[x][y] = (float) (sigma * Math.exp(-(off_x * off_x + off_y * off_y) / sigma2));
            }
        }
    }

    abstract int size();

    BufferedImage getImage() {
        return image.image;
    }

    void repaint() {
        for (int y = 0, subsum; y < cells_array[0].length; y++) {
            subsum = y * cells_array.length;
            for (int x = 0; x < cells_array.length; x++) {
                if (cells_array[x][y] == null) {
                    int l = (int) (100 * lightMap[x][y] / lightPower);
                    image.buffer[x + subsum] = (l  << 16) | (l  << 8) | l;
                } else {
                    image.buffer[x + subsum] = cells_array[x][y].getColor(colorType);
                }
            }
        }
    }

    void setColorType(int TYPE) {
        colorType = TYPE;
    }

    public int nextInt(int i) {
        return random.nextInt(i);
    }

    public boolean nextBoolean() {
        return random.nextBoolean();
    }

    public float nextFloat() {
        return random.nextFloat();
    }

    public void setSeed(long s) {
        random.setSeed(s);
    }
}
