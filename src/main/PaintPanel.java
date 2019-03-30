package main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


public class PaintPanel extends JPanel {
    private Point pos, off;
    private double off_x, off_y;
    private Cells cells;
    private double scale = 1;

    PaintPanel(Cells cells) {
        super();
        this.cells = cells;
        pos = new Point(0, 0);
        off = new Point(0, 0);
        MouseAdapter adapter = new MouseAdapter() {
            Point prev;

            @Override
            public void mouseMoved(MouseEvent e) {
                pos = e.getPoint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                pos = e.getPoint();
                if (prev != null && e.getModifiersEx() == MouseEvent.BUTTON1_DOWN_MASK) {
                    off_x += (double) (prev.x - e.getX()) / scale;
                    off_y += (double) (prev.y - e.getY()) / scale;
                    prev = e.getPoint();
                }
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                prev = e.getPoint();
                if (e.getModifiersEx() == MouseEvent.BUTTON2_DOWN_MASK) {
                    off_x = 0;
                    off_y = 0;
                    int sizePanel = Math.max(getWidth(), getHeight()),
                            sizeCells = Math.max(cells.width, cells.height);
                    scale = ((float) sizePanel) / sizeCells;
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                prev = null;
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (e.getWheelRotation() < 0) {
                    scale *= 1.1;
                } else {
                    scale /= 1.1;
                }
                repaint();
            }
        };

        addMouseMotionListener(adapter);
        addMouseListener(adapter);
        addMouseWheelListener(adapter);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int sizePanel = Math.max(getWidth(), getHeight()),
                        sizeCells = Math.max(cells.width, cells.height);
                scale = ((float) sizePanel) / sizeCells;
            }
        });
    }

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(Color.DARK_GRAY);
        g2.fillRect(0, 0, getWidth(), getHeight());

        cells.repaint();
        g2.translate( -off_x*scale,  -off_y *scale);
        g2.drawImage(cells.getImage(), 0, 0, (int) (cells.width * scale), (int) (cells.height * scale), null);
//        g2.translate(-off_x, -off_y);

//        if (pos != null) {
//            int x = (int) (pos.x / scale);
//            int y = (int) (pos.y / scale);
//            if (cells.checkOutBounds(x, y)) {
//                g.setColor(Color.black);
//                g.fillRect(0, 0, getWidth(), 30);
//                g.setColor(Color.white);
//                g.drawString("Light power: " + cells.lightMap[x][y], 10, 10);
//                if (cells.hasCell(x, y)) {
//                    Cell c = cells.getCell(x, y);
//                    g.setColor(c.color);
//                    g.drawString(c.toString(), 10, 20);
//                }
//            }
//        }
    }


}
