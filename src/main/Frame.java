package main;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Motoko on 09.03.2017.
 */
public class Frame extends JFrame {
    static Thread thread;
    static JFrame frame;
    static Lock lock = new ReentrantLock();
    static boolean hasLock = false;
    static int ms = 100;
    private static int count = 0;
    private static JLabel info;

    JTextField width, height;

    public Frame() throws HeadlessException {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);
        setTitle("Gene Cells");

        setLayout(new BorderLayout());
        add(new PaintPanel(), BorderLayout.CENTER);

        JPanel instruments = new JPanel();
        instruments.setLayout(new GridLayout(11, 1));
        JButton reset = new JButton("Сброс");
        reset.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                lock.lock();
                try {
                    int x = Integer.valueOf(width.getText());
                    int y = Integer.valueOf(height.getText());
                    Cells.init(x, y);
                } catch (Exception ex) {
                    Cells.init(Cells.getWidth(), Cells.getHeight());
                }

                frame.repaint();
                count = 0;
                info.setText(String.format("<html> Итерация: %d<br>Количество клеток: %d<br>Время тика (мс): %d<html>", count++, Cells.queue.size(), 0));

                lock.unlock();
            }
        });
        JButton start = new JButton("Пауза");
        start.addActionListener(new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (hasLock) {
                    lock.unlock();
                    hasLock = false;
                    start.setText("Пауза");
                } else {
                    lock.lock();
                    hasLock = true;
                    start.setText("Продолжить");
                }
            }
        });

        JSlider light = new JSlider(20, 500);
        light.setValue(Cell.lightPower);
        light.setMajorTickSpacing(80);
        light.setMinorTickSpacing(10);
        light.setPaintTicks(true);
        light.setPaintLabels(true);
        light.setBorder(BorderFactory.createTitledBorder("Интенсивность света"));
        light.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                Cell.lightPower = light.getValue();
                Cells.calcLightMap();
            }
        });

        JSlider mut = new JSlider(0, 30);
        mut.setValue((int) (Cell.mutation * 100));
        mut.setMajorTickSpacing(10);
        mut.setMinorTickSpacing(1);
        mut.setPaintTicks(true);
        mut.setPaintLabels(true);
        mut.setBorder(BorderFactory.createTitledBorder("Шанс мутации"));
        mut.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                Cell.mutation = (float) mut.getValue() / 100;
            }
        });

        JSlider peace = new JSlider(0, 20);
        peace.setValue(Cell.peacefulness);
        peace.setMajorTickSpacing(10);
        peace.setMinorTickSpacing(1);
        peace.setPaintTicks(true);
        peace.setPaintLabels(true);
        peace.setBorder(BorderFactory.createTitledBorder("Миролюбивость"));
        peace.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                Cell.peacefulness = peace.getValue();
            }
        });

        JSlider energyLim = new JSlider(100, 2100);
        energyLim.setValue(Cell.energyLim);
        energyLim.setMajorTickSpacing(500);
        energyLim.setMinorTickSpacing(100);
        energyLim.setPaintTicks(true);
        energyLim.setPaintLabels(true);
        energyLim.setBorder(BorderFactory.createTitledBorder("Предел энергии"));
        energyLim.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                Cell.energyLim = energyLim.getValue();
            }
        });

        JSlider energyGap = new JSlider(0, 500);
        energyGap.setValue(Cell.energySptitDeathGap);
        energyGap.setMajorTickSpacing(100);
        energyGap.setMinorTickSpacing(10);
        energyGap.setPaintTicks(true);
        energyGap.setPaintLabels(true);
        energyGap.setBorder(BorderFactory.createTitledBorder("Зона размножения"));
        energyGap.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                Cell.energySptitDeathGap = energyGap.getValue();
            }
        });

        JSlider energyStep = new JSlider(0, 100);
        energyStep.setValue(Cell.energyStep);
        energyStep.setMajorTickSpacing(10);
        energyStep.setMinorTickSpacing(2);
        energyStep.setPaintTicks(true);
        energyStep.setPaintLabels(true);
        energyStep.setBorder(BorderFactory.createTitledBorder("Расход энергии за действие"));
        energyStep.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                Cell.energyStep = energyStep.getValue();
            }
        });

        JSlider step = new JSlider(0, 100);
        step.setValue(ms);
        step.setMajorTickSpacing(10);
        step.setMinorTickSpacing(2);
        step.setPaintTicks(true);
        step.setPaintLabels(true);
        step.setBorder(BorderFactory.createTitledBorder("Шаг симуляции(мс)"));
        step.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                ms = step.getValue();
            }
        });

        JSlider energyCadaver = new JSlider(50, 1050);
        energyCadaver.setValue(Cell.energyCadaver);
        energyCadaver.setMajorTickSpacing(200);
        energyCadaver.setMinorTickSpacing(10);
        energyCadaver.setPaintTicks(true);
        energyCadaver.setPaintLabels(true);
        energyCadaver.setBorder(BorderFactory.createTitledBorder("Калорийность трупа"));
        energyCadaver.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                Cell.energyCadaver = energyCadaver.getValue();
            }
        });

        JPanel size = new JPanel(new GridBagLayout());
        size.setBorder(BorderFactory.createTitledBorder("Соотношение сторон"));
        width = new JTextField(Cells.getWidth());
        width.setText(String.valueOf(Cells.getWidth()));
        height = new JTextField(Cells.getHeight());
        height.setText(String.valueOf(Cells.getHeight()));

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 2;
        c.gridx = 0;
        c.gridy = 0;

        size.add(width, c);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 2;
        c.gridx = 1;
        c.gridy = 0;
        size.add(height, c);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = .9;
        c.gridx = 2;
        c.gridy = 0;
        size.add(reset, c);

        info = new JLabel();

        instruments.add(size);
        instruments.add(start);
        instruments.add(step);
        instruments.add(light);
        instruments.add(mut);
        instruments.add(peace);
        instruments.add(energyLim);
        instruments.add(energyGap);
        instruments.add(energyStep);
        instruments.add(energyCadaver);
        instruments.add(info);

        instruments.setPreferredSize(new Dimension(200, 200));
        add(instruments, BorderLayout.EAST);
        setPreferredSize(new Dimension(1000, 800));
        pack();

        setVisible(true);
    }

    public static void main(String[] args) {

        Cells.init(500, 500);
        frame = new Frame();
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(ms);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                long start = System.currentTimeMillis();
                lock.lock();
                Cells.DoTick();
                lock.unlock();
                info.setText(String.format("<html> Итерация: %d<br>Количество клеток: %d<br>Время тика (мс): %d<html>", count++, Cells.queue.size(), System.currentTimeMillis() - start));

            }
        }).start();

        while (true) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            frame.getContentPane().repaint();
        }


    }
}
