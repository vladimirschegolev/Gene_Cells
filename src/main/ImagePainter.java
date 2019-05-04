package main;

import java.awt.*;
import java.awt.event.*;

public class ImagePainter extends Container {
    private Point pos, pick;
    private double off_x, off_y;
    private Cells cells;
    private double scale = 1;

    ImagePainter(Cells cells) {
        super();
        this.cells = cells;
        pos = new Point(0, 0);

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
                    off_x -= (double) (prev.x - e.getX());
                    off_y -= (double) (prev.y - e.getY());
                    prev = e.getPoint();
                }
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                prev = e.getPoint();
                if (e.getModifiersEx() == MouseEvent.BUTTON2_DOWN_MASK) {
                    recalcScale();
                }
                if (e.getModifiersEx() == MouseEvent.BUTTON1_DOWN_MASK) {
                    pick = new Point(0, 0);
                    pick.x = (int) ((e.getX() - off_x)/scale);
                    pick.y = (int) ((e.getY() - off_y)/scale);
                }
                if (e.getModifiersEx() == MouseEvent.BUTTON3_DOWN_MASK) {
                    pick = null;
                }
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                prev = null;
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (e.getWheelRotation() < 0) {
                    scale *= 1.1;
                    off_x = off_x - (pos.x - off_x)*.1;
                    off_y = off_y - (pos.y - off_y)*.1;
                } else {
                    scale /= 1.1;
                    off_x = off_x + (pos.x - off_x)*(.1/1.1);
                    off_y = off_y + (pos.y - off_y)*(.1/1.1);
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
                recalcScale();
            }
        });
    }

    public void recalcScale() {
        off_x = 0;
        off_y = 0;
        int sizePanel = Math.max(getWidth(), getHeight()),
                sizeCells = Math.max(cells.getImage().getWidth(), cells.getImage().getHeight());
        scale = ((float) sizePanel) / sizeCells;
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(Color.DARK_GRAY);
        g2.fillRect(0, 0, getWidth(), getHeight());

        g2.drawImage(cells.getImage(),
                ((int) off_x), (int) off_y,
                (int) (cells.getImage().getWidth() * scale), (int) (cells.getImage().getHeight() * scale),
                null);

        if (pick != null) {
            if (cells.checkBounds(pick.x, pick.y)) {
                g.setColor(Color.black);
                g.fillRect(0, 0, getWidth(), 25);
                g.setColor(Color.white);
                g.drawString("Light power: " + cells.lightMap[pick.x][pick.y], 10, 10);
                if (cells.hasCell(pick.x, pick.y)) {
                    Cell c = cells.getCell(pick.x, pick.y);
                    g.setColor(new Color(c.color_family));
                    g.fillOval(8,13, 8,8);
                    g.setColor(Color.white);
                    g.drawString(c.toString(), 20, 20);
                }
            }
        }
    }


}
