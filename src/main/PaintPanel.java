package main;

import javax.swing.*;
import java.awt.*;


public class PaintPanel extends JPanel {

    public PaintPanel() {

    }

    @Override
    public void paintComponent(Graphics g) {
        g.setColor(Color.black);
        g.fillRect(0, 0, getWidth(), getHeight());
        double sizeX = ((double) getWidth() / Cells.getWidth());
        double sizeY = ((double) getHeight() / Cells.getHeight());

        for (int i = 0; i < Cells.getWidth(); i++) {
            int x = (int) (i * sizeX);
            for (int j = 0; j < Cells.getHeight(); j++) {
                if (Cells.hasCell(i, j)) {
                    try {
                        g.setColor(Cells.getCell(i, j).color);
                    } catch (Exception e) {
//                        e.printStackTrace();
                    }

                    g.fillRect(x, (int) (j * sizeY), (int) sizeX + 1, (int) sizeY + 1);
                }
            }
        }
    }


}
