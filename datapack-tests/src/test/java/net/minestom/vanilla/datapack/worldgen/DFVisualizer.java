package net.minestom.vanilla.datapack.worldgen;

import com.google.common.util.concurrent.AtomicDouble;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class DFVisualizer {

    public enum Axis {
        X, Y, Z
    }

    static void visualize2d(String source, double scale, Axis axisToIgnore) {
        visualize2d(DF.vanilla(source), DF.vri(source), scale, axisToIgnore);
    }

    static void visualize2d(DF vanilla, DF vri, double scale, Axis axisToSpecify) {
        Dimension dimension = new Dimension(512, 512);
        BufferedImage vanillaImage = generateImage(vanilla, scale, dimension, axisToSpecify, 0);
        BufferedImage vriImage = generateImage(vri, scale, dimension, axisToSpecify, 0);
        JLabel vanillaLabel = new JLabel(new ImageIcon(vanillaImage));
        JLabel vriLabel = new JLabel(new ImageIcon(vriImage));

        // create window, add images, and block this thread until the window is closed
        JFrame frame = new JFrame("Density Function Visualizer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new FlowLayout());
        frame.getContentPane().add(vanillaLabel);
        frame.getContentPane().add(vriLabel);

        // Add a slider for the axis value
        JSlider slider = new JSlider(JSlider.HORIZONTAL, -100, 100, 0);
        slider.setMajorTickSpacing(20);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        // register a listener to update the image when the slider is moved
        AtomicDouble axisValue = new AtomicDouble(0);
        AtomicDouble target = new AtomicDouble(0);
        slider.addChangeListener(e -> target.set(slider.getValue()));
        Thread daemon = new Thread(() -> {
            while (true) {
                System.out.println("target: " + target.get() + ", axisValue: " + axisValue.get());
                if (target.get() == axisValue.get()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    continue;
                }
                double currentTarget = target.get();
                System.out.println("Updating image");
                BufferedImage vanillaImage1 = generateImage(vanilla, scale, dimension, axisToSpecify, currentTarget);
                BufferedImage vriImage1 = generateImage(vri, scale, dimension, axisToSpecify, currentTarget);
                vanillaLabel.setIcon(new ImageIcon(vanillaImage1));
                vriLabel.setIcon(new ImageIcon(vriImage1));
                frame.pack();
                frame.repaint();
                axisValue.set(currentTarget);
            }
        });
        daemon.setDaemon(true);
        daemon.start();
        frame.getContentPane().add(slider);
        frame.pack();
        frame.setVisible(true);
        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static BufferedImage generateImage(DF df, double scale, Dimension imageDimensions, Axis axisToSpecify, double axisValue) {
        BufferedImage image = new BufferedImage(imageDimensions.width, imageDimensions.height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, imageDimensions.width, imageDimensions.height);

        double xStep = scale / imageDimensions.width;
        double zStep = scale / imageDimensions.height;

        for (int x = 0; x < imageDimensions.width; x++) {
            for (int z = 0; z < imageDimensions.height; z++) {
                double xCoord = ((double) x - (double) imageDimensions.width / 2.0) * xStep;
                double zCoord = ((double) z - (double) imageDimensions.height / 2.0) * zStep;
                double yCoord = switch (axisToSpecify) {
                    case X -> df.compute(axisValue, xCoord, zCoord);
                    case Y -> df.compute(xCoord, axisValue, zCoord);
                    case Z -> df.compute(xCoord, zCoord, axisValue);
                };
                double alpha = Math.min(1.0, Math.max(0.0, yCoord));
                graphics.setColor(new Color(1.0f, 1.0f, 1.0f, (float) alpha));
                graphics.fillRect(x, z, 1, 1);
            }
        }

        image.flush();
        return image;
    }
}
