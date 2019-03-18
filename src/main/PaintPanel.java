package main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


public class PaintPanel extends JPanel {
    private Point pos;

    PaintPanel() {
        super();
        MouseAdapter adapter = new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (e.getModifiersEx() == MouseEvent.BUTTON1_DOWN_MASK) {
                    pos = e.getPoint();
                    PaintPanel.this.repaint(0, 0, getWidth(), 50);
                } else {
                    pos = null;
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getModifiersEx() == MouseEvent.BUTTON1_DOWN_MASK) {
                    pos = e.getPoint();
                    PaintPanel.this.repaint(0, 0, getWidth(), 50);
                } else {
                    pos = null;
                }
            }
        };

        addMouseMotionListener(adapter);
        addMouseListener(adapter);
    }

    @Override
    public void paintComponent(Graphics g) {
        g.setColor(Color.black);
        g.fillRect(0, 0, getWidth(), getHeight());
        double sizeX = ((double) getWidth() / Cells.getWidth());
        double sizeY = ((double) getHeight() / Cells.getHeight());

//        for (int i = 0; i < Cells.getWidth(); i++) {
//            int x = (int) (i * sizeX);
//            for (int j = 0; j < Cells.getHeight(); j++) {
//                int c = (int) Cells.lightMap[i][j]%255;
//                g.setColor(new Color(c,c,c));
//                g.fillRect(x, (int) (j * sizeY), (int) sizeX + 1, (int) sizeY + 1);
//
//            }
//        }

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

        if (pos != null) {
            int x = (int) (pos.x / sizeX);
            int y = (int) (pos.y / sizeY);
            if (Cells.check(x, y)) {
                g.setColor(Color.white);
                g.drawString("Light power: " + Cells.lightMap[x][y], 10, 10);
                if (Cells.hasCell(x, y)) {
                    Cell c = Cells.getCell(x, y);
                    g.setColor(c.color);
                    g.drawString(c.toString(), 10, 20);
                }
            }
        }
    }


}
