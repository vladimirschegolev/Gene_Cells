package main;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Motoko on 09.03.2017.
 */
public class Frame extends JFrame {

    //    private static Frame frame;
    private Lock lock = new ReentrantLock();
    private int sleepSimulation = 0, sleepRepaint = 33, sleepLight = 33;
    private int count = 0;
    private JLabel info;
    private boolean isRun = false, dynamicLight = false;
    private JTextField width, height;
    private PaintPanel paintPan;


    private Frame() throws HeadlessException {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);
        setTitle("Gene Cells");
        setLayout(new BorderLayout());

        paintPan = new PaintPanel();

        add(paintPan, BorderLayout.CENTER);
        add(getInstrumentsPanel(), BorderLayout.EAST);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setMinimumSize(new Dimension(500, 500));
        setBounds((screenSize.width - 1000) / 2, (screenSize.height - 800) / 2, 1000, 800);
        setPreferredSize(new Dimension(1000, 800));

        setVisible(true);

        new Thread(new TickTask()).start();
        new Thread(new MoveLightTask()).start();
        new Thread(new RepaintTask()).start();
    }

    private JPanel getInstrumentsPanel() {

        JSlider stepSimulation = new JSlider(0, 100);
        stepSimulation.setValue(sleepSimulation);
        stepSimulation.setMajorTickSpacing(10);
        stepSimulation.setMinorTickSpacing(2);
        stepSimulation.setPaintTicks(true);
        stepSimulation.setPaintLabels(true);
        stepSimulation.setBorder(BorderFactory.createTitledBorder("Шаг симуляции " + sleepSimulation + " мс"));
        stepSimulation.addChangeListener(e -> {
            sleepSimulation = stepSimulation.getValue();
            ((TitledBorder) stepSimulation.getBorder()).setTitle("Шаг симуляции " + sleepSimulation + " мс");
            stepSimulation.repaint();
        });

        JSlider stepRepaint = new JSlider(10, 100);
        stepRepaint.setValue(sleepRepaint);
        stepRepaint.setMajorTickSpacing(10);
        stepRepaint.setMinorTickSpacing(2);
        stepRepaint.setPaintTicks(true);
        stepRepaint.setPaintLabels(true);
        stepRepaint.setBorder(BorderFactory.createTitledBorder("Шаг перерисовки " + sleepRepaint + " мс"));
        stepRepaint.addChangeListener(e -> {
            sleepRepaint = stepRepaint.getValue();
            ((TitledBorder) stepRepaint.getBorder()).setTitle("Шаг перерисовки " + sleepRepaint + " мс");
            stepRepaint.repaint();
        });


        JSlider mutation = new JSlider(0, 50);
        mutation.setValue((int) (CellActArray.mutation * 100));
        mutation.setMajorTickSpacing(10);
        mutation.setMinorTickSpacing(1);
        mutation.setPaintTicks(true);
        mutation.setPaintLabels(true);
        mutation.setBorder(BorderFactory.createTitledBorder("Шанс мутации " + mutation.getValue() + "%"));
        mutation.addChangeListener(e -> {
            CellActArray.mutation = (float) mutation.getValue() / 100;
            ((TitledBorder) mutation.getBorder()).setTitle("Шанс мутации " + mutation.getValue() + "%");
            mutation.repaint();
        });

        JSlider peacefulness = new JSlider(0, 20);
        peacefulness.setValue(CellActArray.peacefulness);
        peacefulness.setMajorTickSpacing(10);
        peacefulness.setMinorTickSpacing(1);
        peacefulness.setPaintTicks(true);
        peacefulness.setPaintLabels(true);
        peacefulness.setBorder(BorderFactory.createTitledBorder("Миролюбивость " + peacefulness.getValue()));
        peacefulness.addChangeListener(e -> {
            CellActArray.peacefulness = peacefulness.getValue();
            ((TitledBorder) peacefulness.getBorder()).setTitle("Миролюбивость " + peacefulness.getValue());
            peacefulness.repaint();
        });

        JSlider energyLim = new JSlider(100, 2100);
        energyLim.setValue(CellActArray.energyLim);
        energyLim.setMajorTickSpacing(500);
        energyLim.setMinorTickSpacing(100);
        energyLim.setPaintTicks(true);
        energyLim.setPaintLabels(true);
        energyLim.setBorder(BorderFactory.createTitledBorder("Предел энергии " + energyLim.getValue()));
        energyLim.addChangeListener(e -> {
            CellActArray.energyLim = energyLim.getValue();
            ((TitledBorder) energyLim.getBorder()).setTitle("Предел энергии " + energyLim.getValue());
            energyLim.repaint();
        });

        JSlider energyGap = new JSlider(20, 100);
        energyGap.setValue(CellActArray.energySplitDeathGap);
        energyGap.setMajorTickSpacing(20);
        energyGap.setMinorTickSpacing(10);
        energyGap.setPaintTicks(true);
        energyGap.setPaintLabels(true);
        energyGap.setBorder(BorderFactory.createTitledBorder("Порог размножения " + energyGap.getValue() + "%"));
        energyGap.addChangeListener(e -> {
            CellActArray.energySplitDeathGap = energyGap.getValue();
            ((TitledBorder) energyGap.getBorder()).setTitle("Порог размножения " + energyGap.getValue() + "%");
            energyGap.repaint();
        });

        JSlider energyStep = new JSlider(1, 100);
        energyStep.setValue(CellActArray.energyStep);
        energyStep.setMajorTickSpacing(10);
        energyStep.setMinorTickSpacing(2);
        energyStep.setPaintTicks(true);
        energyStep.setPaintLabels(true);
        energyStep.setBorder(BorderFactory.createTitledBorder("Расход энергии за действие " + energyStep.getValue()));
        energyStep.addChangeListener(e -> {
            CellActArray.energyStep = energyStep.getValue();
            ((TitledBorder) energyStep.getBorder()).setTitle("Расход энергии за действие " + energyStep.getValue());
            energyStep.repaint();
        });

        info = new JLabel();
        updateInfo(0);

        JPanel instruments = new JPanel();

        instruments.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        c.fill = GridBagConstraints.BOTH;
        c.ipady = 0;
        c.gridx = 0;
        c.gridy = 0;
        instruments.add(getSizeAndButtonsPanel(), c);
        c.gridy++;
        instruments.add(getLightPanel(), c);
        c.gridy++;
        instruments.add(stepSimulation, c);
        c.gridy++;
        instruments.add(stepRepaint, c);
        c.gridy++;
        instruments.add(mutation, c);
        c.gridy++;
        instruments.add(peacefulness, c);
        c.gridy++;
        instruments.add(energyLim, c);
        c.gridy++;
        instruments.add(energyGap, c);
        c.gridy++;
        instruments.add(energyStep, c);
        c.gridy++;
        instruments.add(info, c);

        instruments.setPreferredSize(new Dimension(200, 200));

        return instruments;
    }

    private JPanel getLightPanel() {

        JSlider sliderLightPower = new JSlider(20, 500);
        sliderLightPower.setValue(CellActArray.lightPower);
        sliderLightPower.setMajorTickSpacing(80);
        sliderLightPower.setMinorTickSpacing(10);
        sliderLightPower.setPaintTicks(true);
        sliderLightPower.setPaintLabels(true);
        sliderLightPower.setBorder(BorderFactory.createTitledBorder("Интенсивность " + sliderLightPower.getValue()));
        sliderLightPower.addChangeListener(e -> {
            CellActArray.lightPower = sliderLightPower.getValue();

            if (!dynamicLight) {
                Cells.calcLightMap();
            }

            Cells.calcLightMap();
            ((TitledBorder) sliderLightPower.getBorder()).setTitle("Интенсивность " + sliderLightPower.getValue());
            sliderLightPower.repaint();
        });

        JSlider sliderRotationLight = new JSlider(1, 100);
        sliderRotationLight.setValue(CellActArray.energyStep);
        sliderRotationLight.setMajorTickSpacing(10);
        sliderRotationLight.setMinorTickSpacing(2);
        sliderRotationLight.setPaintTicks(true);
        sliderRotationLight.setPaintLabels(true);
        sliderRotationLight.setBorder(BorderFactory.createTitledBorder("Задержка вращения " + sleepLight));
        sliderRotationLight.addChangeListener(e -> {
            sleepLight = sliderRotationLight.getValue();
            ((TitledBorder) sliderRotationLight.getBorder()).setTitle("Задержка вращения " + sliderRotationLight.getValue());
            sliderRotationLight.repaint();
        });



        JRadioButton staticLightBtn = new JRadioButton("Статичный", !dynamicLight);
        JRadioButton dynamicLightBtn = new JRadioButton("Динамический", dynamicLight);

        ActionListener listener = e -> {
            if (dynamicLightBtn == e.getSource()) {
                dynamicLight = true;
            } else {
                dynamicLight = false;
                Cells.calcLightMap();
            }

        };

        staticLightBtn.addActionListener(listener);
        dynamicLightBtn.addActionListener(listener);


        ButtonGroup lightGroup = new ButtonGroup();
        lightGroup.add(staticLightBtn);
        lightGroup.add(dynamicLightBtn);

        JPanel light = new JPanel(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();

        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy = 0;
        light.add(sliderRotationLight, c);
        c.gridy = 1;
        light.add(sliderLightPower, c);

        c.weightx = .5;
        c.gridwidth = 1;
        c.gridx = 0;
        c.gridy = 2;
        light.add(staticLightBtn, c);

        c.weightx = .5;
        c.gridx = 1;
        c.gridy = 2;
        light.add(dynamicLightBtn, c);

        light.setBorder(BorderFactory.createTitledBorder("Освещение:"));
        return light;
    }

    private JPanel getSizeAndButtonsPanel() {

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

                Frame.this.repaint();
                count = 0;
                updateInfo(0);
                lock.unlock();
            }
        });

        JButton start = new JButton("Старт");
        start.addActionListener(new AbstractAction() {


            @Override
            public void actionPerformed(ActionEvent e) {
                if (isRun) {
                    isRun = false;
                    start.setText("Старт");
                } else {
                    isRun = true;
                    start.setText("Пауза");
                }
            }
        });

        JPanel sizeAndButtons = new JPanel(new GridBagLayout());
        sizeAndButtons.setBorder(BorderFactory.createTitledBorder("Соотношение сторон"));
        width = new JTextField(Cells.getWidth());
        width.setText(String.valueOf(Cells.getWidth()));
        height = new JTextField(Cells.getHeight());
        height.setText(String.valueOf(Cells.getHeight()));

        GridBagConstraints c = new GridBagConstraints();

        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(2, 2, 2, 2);
        c.gridx = 0;
        c.gridy = 0;
        sizeAndButtons.add(width, c);

        c.gridx = 1;
        sizeAndButtons.add(height, c);

        c.gridx = 0;
        c.gridy = 1;
        sizeAndButtons.add(start, c);

        c.gridx = 1;
        sizeAndButtons.add(reset, c);

        return sizeAndButtons;
    }

    private void updateInfo(long time) {
        info.setText(String.format("<html> Итерация: %d<br>Количество клеток: %d<br>Время тика (мс): %d<html>",
                count, Cells.queue.size(), time));
    }


    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        Cells.init(500, 500);
        new Frame();
    }

    private class TickTask implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(sleepSimulation);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (isRun) {
                    long start = System.currentTimeMillis();

                    lock.lock();
                    Cells.DoTick();
                    lock.unlock();

                    count++;
                    updateInfo(System.currentTimeMillis() - start);
                }
            }
        }
    }

    private class RepaintTask implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(sleepRepaint);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (isRun) {
                    paintPan.repaint();
                }
            }
        }
    }

    private class MoveLightTask implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    if (dynamicLight) Thread.sleep(sleepLight);
                    else Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (dynamicLight && isRun) {
                    Cells.calcLightMapDynamic();
                }
            }
        }
    }
}
