package main;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Motoko on 09.03.2017.
 */
public class Frame extends JFrame {

    private static Frame frame;
    private static Lock lock = new ReentrantLock();
    private static boolean hasLock = false;
    private static int sleepSimulation = 0, sleepRepaint = 33;
    private static int count = 0;
    private static JLabel info;

    private JTextField width, height;
    private PaintPanel paintPan;

    private Frame() throws HeadlessException {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);
        setTitle("Gene Cells");
        setLayout(new BorderLayout());
        paintPan = new PaintPanel();
        add(paintPan, BorderLayout.CENTER);


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

        JSlider stepSimulation = new JSlider(0, 100);
        stepSimulation.setValue(sleepSimulation);
        stepSimulation.setMajorTickSpacing(10);
        stepSimulation.setMinorTickSpacing(2);
        stepSimulation.setPaintTicks(true);
        stepSimulation.setPaintLabels(true);
        stepSimulation.setBorder(BorderFactory.createTitledBorder("Шаг симуляции(" + sleepSimulation + " мс)"));
        stepSimulation.addChangeListener(e -> {
            sleepSimulation = stepSimulation.getValue();
            ((TitledBorder)stepSimulation.getBorder()).setTitle("Шаг симуляции(" + sleepSimulation + " мс)");
            stepSimulation.repaint();
        });

        JSlider stepRepaint = new JSlider(10, 100);
        stepRepaint.setValue(sleepRepaint);
        stepRepaint.setMajorTickSpacing(10);
        stepRepaint.setMinorTickSpacing(2);
        stepRepaint.setPaintTicks(true);
        stepRepaint.setPaintLabels(true);
        stepRepaint.setBorder(BorderFactory.createTitledBorder("Шаг перерисовки(" + sleepSimulation + " мс)"));
        stepRepaint.addChangeListener(e -> {
            sleepRepaint = stepRepaint.getValue();
            ((TitledBorder)stepRepaint.getBorder()).setTitle("Шаг перерисовки(" + sleepRepaint + " мс)");
            stepRepaint.repaint();
        });

        JSlider lightPower = new JSlider(20, 500);
        lightPower.setValue(Cell.lightPower);
        lightPower.setMajorTickSpacing(80);
        lightPower.setMinorTickSpacing(10);
        lightPower.setPaintTicks(true);
        lightPower.setPaintLabels(true);
        lightPower.setBorder(BorderFactory.createTitledBorder("Интенсивность света(" + lightPower.getValue() + ")"));
        lightPower.addChangeListener(e -> {
            Cell.lightPower = lightPower.getValue();
            Cells.calcLightMap();
            ((TitledBorder)lightPower.getBorder()).setTitle("Интенсивность света(" + lightPower.getValue() + ")");
            lightPower.repaint();
        });

        JSlider mutation = new JSlider(0, 50);
        mutation.setValue((int) (Cell.mutation * 100));
        mutation.setMajorTickSpacing(10);
        mutation.setMinorTickSpacing(1);
        mutation.setPaintTicks(true);
        mutation.setPaintLabels(true);
        mutation.setBorder(BorderFactory.createTitledBorder("Шанс мутации(" + mutation.getValue() + "%)"));
        mutation.addChangeListener(e -> {
            Cell.mutation = (float) mutation.getValue() / 100;
            ((TitledBorder)mutation.getBorder()).setTitle("Шанс мутации(" + mutation.getValue() + "%)");
            mutation.repaint();
        });

        JSlider peacefulpess = new JSlider(0, 20);
        peacefulpess.setValue(Cell.peacefulness);
        peacefulpess.setMajorTickSpacing(10);
        peacefulpess.setMinorTickSpacing(1);
        peacefulpess.setPaintTicks(true);
        peacefulpess.setPaintLabels(true);
        peacefulpess.setBorder(BorderFactory.createTitledBorder("Миролюбивость(" + peacefulpess.getValue() + ")"));
        peacefulpess.addChangeListener(e -> {
            Cell.peacefulness = peacefulpess.getValue();
            ((TitledBorder)peacefulpess.getBorder()).setTitle("Миролюбивость(" + peacefulpess.getValue() + ")");
            peacefulpess.repaint();
        });

        JSlider energyLim = new JSlider(100, 2100);
        energyLim.setValue(Cell.energyLim);
        energyLim.setMajorTickSpacing(500);
        energyLim.setMinorTickSpacing(100);
        energyLim.setPaintTicks(true);
        energyLim.setPaintLabels(true);
        energyLim.setBorder(BorderFactory.createTitledBorder("Предел энергии(" + energyLim.getValue() + ")"));
        energyLim.addChangeListener(e -> {
            Cell.energyLim = energyLim.getValue();
            ((TitledBorder)energyLim.getBorder()).setTitle("Предел энергии(" + energyLim.getValue() + ")");
            energyLim.repaint();
        });

        JSlider energyGap = new JSlider(0, 90);
        energyGap.setValue(Cell.energySptitDeathGap);
        energyGap.setMajorTickSpacing(20);
        energyGap.setMinorTickSpacing(10);
        energyGap.setPaintTicks(true);
        energyGap.setPaintLabels(true);
        energyGap.setBorder(BorderFactory.createTitledBorder("Зона размножения"));
        energyGap.addChangeListener(e -> {
            Cell.energySptitDeathGap = energyGap.getValue();
            ((TitledBorder)energyGap.getBorder()).setTitle("Зона размножения(" + energyGap.getValue() + "%)");
            energyGap.repaint();
        });

        JSlider energyStep = new JSlider(1, 100);
        energyStep.setValue(Cell.energyStep);
        energyStep.setMajorTickSpacing(10);
        energyStep.setMinorTickSpacing(2);
        energyStep.setPaintTicks(true);
        energyStep.setPaintLabels(true);
        energyStep.setBorder(BorderFactory.createTitledBorder("Расход энергии за действие(" + energyStep.getValue() + ")"));
        energyStep.addChangeListener(e -> {
            Cell.energyStep = energyStep.getValue();
            ((TitledBorder)energyStep.getBorder()).setTitle("Расход энергии за действие(" + energyStep.getValue() + ")");
            energyStep.repaint();
        });

        JSlider energyCadaver = new JSlider(50, 1001);
        energyCadaver.setValue(Cell.energyCadaver);
        energyCadaver.setMajorTickSpacing(150);
        energyCadaver.setMinorTickSpacing(100);
        energyCadaver.setPaintTicks(true);
        energyCadaver.setPaintLabels(true);
        energyCadaver.setBorder(BorderFactory.createTitledBorder("Калорийность трупа(" + energyCadaver.getValue() + ")"));
        energyCadaver.addChangeListener(e -> {
            Cell.energyCadaver = energyCadaver.getValue();
            ((TitledBorder)energyCadaver.getBorder()).setTitle("Калорийность трупа(" + energyCadaver.getValue() + ")");
            energyCadaver.repaint();
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

        JPanel instruments = new JPanel();
        instruments.setLayout(new GridLayout(12, 1));
        instruments.add(size);
        instruments.add(start);
        instruments.add(stepSimulation);
        instruments.add(stepRepaint);
        instruments.add(lightPower);
        instruments.add(mutation);
        instruments.add(peacefulpess);
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

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ignored) {
        }

        Cells.init(500, 500);
        frame = new Frame();
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(sleepSimulation);
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

        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(sleepRepaint);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                frame.getPaintPanel().repaint();
            }
        }).start();


    }

    private Component getPaintPanel() {
        return paintPan;
    }
}
