package main;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class Frame extends JFrame {

    private static Cells cells;
    private Lock lock = new ReentrantLock();
    private int sleepSimulation = 0, sleepRepaint = 33, sleepLight = 33;
    private int count = 0;
    private JLabel info;
    private boolean dynamicLight = false, isRun = false;
    private JTextField width, height;
    private PaintPanel paintPan;
    private ExecutorService executor = Executors.newCachedThreadPool();
    private TickTask tickTask = new TickTask();
    private MoveLightTask moveLightTask = new MoveLightTask();
    private int choice = 0;

    private Frame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);
        setTitle("Gene Cells");
        setLayout(new BorderLayout());

        paintPan = new PaintPanel(cells);

        add(paintPan, BorderLayout.CENTER);
        add(getInstrumentsPanel(), BorderLayout.EAST);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setMinimumSize(new Dimension(500, 500));
        setBounds((screenSize.width - 1000) / 2, (screenSize.height - 900) / 2, 1000, 900);
        setPreferredSize(new Dimension(1000, 900));

        setVisible(true);
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


        JSlider mutation = new JSlider(0, 500);
        mutation.setValue((int) (cells.mutation * 1000));
        mutation.setMajorTickSpacing(100);
        mutation.setMinorTickSpacing(10);
        mutation.setPaintTicks(true);
        mutation.setPaintLabels(true);
        mutation.setBorder(BorderFactory.createTitledBorder(String.format("Уровень мутации %2.1f%%", mutation.getValue()/10f)));
        mutation.addChangeListener(e -> {
            cells.mutation = mutation.getValue() / 1000f;
            ((TitledBorder) mutation.getBorder()).setTitle(String.format("Уровень мутации %2.1f%%", mutation.getValue()/10f));
            mutation.repaint();
        });

        JSlider peacefulness = new JSlider(0, 20);
        peacefulness.setValue(cells.peacefulness);
        peacefulness.setMajorTickSpacing(10);
        peacefulness.setMinorTickSpacing(1);
        peacefulness.setPaintTicks(true);
        peacefulness.setPaintLabels(true);
        peacefulness.setBorder(BorderFactory.createTitledBorder("Миролюбивость " + peacefulness.getValue()));
        peacefulness.addChangeListener(e -> {
            cells.peacefulness = peacefulness.getValue();
            ((TitledBorder) peacefulness.getBorder()).setTitle("Миролюбивость " + peacefulness.getValue());
            peacefulness.repaint();
        });

        String[] choices = new String[]{"Поколения", "Энергия", "Сложность"};

        JComboBox<String> comboBox = new JComboBox<>(choices);
        comboBox.setBorder(BorderFactory.createTitledBorder("Расцветка"));
        comboBox.addItemListener(e -> {
            if (choices[0].equals(e.getItem())) {
                cells.setColorType(Cells.GENERATIONS);
            } else if (choices[1].equals(e.getItem())) {
                cells.setColorType(Cells.ENERGY);
            }else if (choices[2].equals(e.getItem())) {
                cells.setColorType(Cells.COMPLEXITY);
            }
            if (!isRun) paintPan.repaint();
        });

        JSlider energyGap = new JSlider(20, 100);
        energyGap.setValue(cells.energySplitDeathGap);
        energyGap.setMajorTickSpacing(20);
        energyGap.setMinorTickSpacing(10);
        energyGap.setPaintTicks(true);
        energyGap.setPaintLabels(true);
        energyGap.setBorder(BorderFactory.createTitledBorder("Порог размножения " + energyGap.getValue() + "%"));
        energyGap.addChangeListener(e -> {
            cells.energySplitDeathGap = energyGap.getValue();
            ((TitledBorder) energyGap.getBorder()).setTitle("Порог размножения " + energyGap.getValue() + "%");
            energyGap.repaint();
        });

        JSlider energyStep = new JSlider(1, 100);
        energyStep.setMinimumSize(new Dimension(200,80));
        energyStep.setValue(cells.energyStep);
        energyStep.setMajorTickSpacing(10);
        energyStep.setMinorTickSpacing(2);
        energyStep.setPaintTicks(true);
        energyStep.setPaintLabels(true);
        energyStep.setBorder(BorderFactory.createTitledBorder("Расход энергии за действие " + energyStep.getValue()));
        energyStep.addChangeListener(e -> {
            cells.energyStep = energyStep.getValue();
            ((TitledBorder) energyStep.getBorder()).setTitle("Расход энергии за действие " + energyStep.getValue());
            energyStep.repaint();
        });

        info = new JLabel();
        info.setMaximumSize(new Dimension(180, 80));
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
        instruments.add(comboBox, c);
        c.gridy++;
        instruments.add(energyGap, c);
        c.gridy++;
        instruments.add(energyStep, c);
        c.gridy++;
        c.insets = new Insets(0,10,0,10);
        instruments.add(info, c);

        instruments.setPreferredSize(new Dimension(200, 500));

        return instruments;
    }

    private JPanel getLightPanel() {

        JSlider sliderLightPower = new JSlider(20, 500);
        sliderLightPower.setValue(cells.lightPower);
        sliderLightPower.setMajorTickSpacing(80);
        sliderLightPower.setMinorTickSpacing(10);
        sliderLightPower.setPaintTicks(true);
        sliderLightPower.setPaintLabels(true);
        sliderLightPower.setBorder(BorderFactory.createTitledBorder("Интенсивность " + sliderLightPower.getValue()));
        sliderLightPower.addChangeListener(e -> {
            cells.lightPower = sliderLightPower.getValue();

            if (!dynamicLight) {
                cells.calcLightMap();
            }

            ((TitledBorder) sliderLightPower.getBorder()).setTitle("Интенсивность " + sliderLightPower.getValue());
            sliderLightPower.repaint();
            if (!isRun) paintPan.repaint();

        });

        JSlider sliderRotationLight = new JSlider(1, 100);
        sliderRotationLight.setValue(cells.energyStep);
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


        JRadioButton staticLightBtn = new JRadioButton("Статика", !dynamicLight);
        JRadioButton dynamicLightBtn = new JRadioButton("Динамика", dynamicLight);

        ActionListener listener = e -> {
            if (dynamicLightBtn == e.getSource()) {
                dynamicLight = true;
                executor.execute(moveLightTask);
            } else {
                dynamicLight = false;
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
        reset.addActionListener(new  AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                lock.lock();
                try {
                    int x = Integer.valueOf(width.getText());
                    int y = Integer.valueOf(height.getText());
                    cells.init(x, y, choice);
                } catch (Exception ex) {
                    cells.init(cells.getWidth(), cells.getHeight(), choice);
                }

                Frame.this.repaint();
                count = 0;
                updateInfo(0);
                paintPan.recalcScale();
                lock.unlock();
            }
        });

        JButton start = new JButton("Старт") {
            @Override
            public void repaint() {
                super.repaint();
                if (isRun) {
                    setText("Пауза");
                } else {
                    setText("Старт");
                }
            }
        };
        start.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isRun) {
                    isRun = false;
                } else {
                    isRun = true;
                    executor.execute(tickTask);

                    if (dynamicLight) {
                        executor.execute(moveLightTask);
                    }

                }
            }
        });

        String[] choices = new String[]{"Gene Cells", "Neuron Cells"};

        JComboBox<String> comboBox = new JComboBox<>(choices);
        comboBox.addItemListener(e -> {
            if (choices[0].equals(e.getItem())) {
                choice = 0;
            } else if (choices[1].equals(e.getItem())) {
                choice = 1;
            }
        });

        JPanel sizeAndButtons = new JPanel(new GridBagLayout());
        sizeAndButtons.setBorder(BorderFactory.createTitledBorder("Соотношение сторон"));
        width = new JTextField(cells.getWidth());
        width.setText(String.valueOf(cells.getWidth()));
        height = new JTextField(cells.getHeight());
        height.setText(String.valueOf(cells.getHeight()));

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

        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy = 2;
        sizeAndButtons.add(comboBox, c);

        return sizeAndButtons;
    }

    private void updateInfo(long time) {
        info.setText(String.format("<html>Итерация: %d<br>Количество клеток: %d<br>Время тика (мс): %d<html>",
                count, cells.size(), time));
    }


    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        cells = CellsParallel.getInstance();
        cells.init(600, 600, 0);
        new Frame();
    }

    private class TickTask implements Runnable {

        private RepaintTask repaintTask = new RepaintTask();

        @Override
        public void run() {
            executor.execute(repaintTask);
            while (isRun) {
                try {
                    Thread.sleep(sleepSimulation);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                long start = System.currentTimeMillis();

                lock.lock();
                if (!cells.DoTick()) {
                    isRun = false;
                }
                lock.unlock();

                count++;
                updateInfo(System.currentTimeMillis() - start);
            }
        }
    }

    private class RepaintTask implements Runnable {

        @Override
        public void run() {
            while (isRun) {
                try {
                    Thread.sleep(sleepRepaint);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                paintPan.repaint();
            }
        }
    }

    private class MoveLightTask implements Runnable {

        @Override
        public void run() {
            while (isRun && dynamicLight) {
                try {
                    Thread.sleep(sleepLight + sleepSimulation);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                cells.calcLightMapDynamic();
            }
            if (isRun) {
                cells.calcLightMap();
            }
        }
    }
}
