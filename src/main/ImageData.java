package main;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

public class ImageData {
    final BufferedImage image;
    final int[] buffer;

    public ImageData(int size_x, int size_y) {
        image = new BufferedImage(size_x, size_y, BufferedImage.TYPE_INT_RGB);
        buffer = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
    }
}
