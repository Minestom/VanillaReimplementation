package net.minestom.vanilla.datapack.worldgen;

import com.google.common.util.concurrent.AtomicDouble;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minestom.server.MinecraftServer;
import net.minestom.vanilla.VanillaReimplementation;
import net.minestom.vanilla.datapack.Datapack;
import net.minestom.vanilla.datapack.DatapackLoader;
import net.minestom.vanilla.datapack.DatapackLoadingFeature;
import net.minestom.vanilla.datapack.worldgen.noise.NormalNoise;
import net.minestom.vanilla.datapack.worldgen.storage.DoubleStorage;
import net.minestom.vanilla.files.FileSystem;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public class DFVisualizer {

    public static void main(String[] args) {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();

        String df = """
{
  "type": "minecraft:noise",
  "noise": {
    "firstOctave": -3,
    "amplitudes": [
      1
    ]
  },
  "xz_scale": 1.0,
  "y_scale": 1.0
}
                """;

//        DF vri = new DF.VriDF(
//                new DensityFunctions.NoiseRoot(
//                        1.0,
//                        1.0,
//                        new NormalNoise(
//                                DatapackLoader.loading().random(),
//                                new NormalNoise.Config(1, DoubleList.of(1))
//                        )
//                )
//        );

        visualize2d(df, Axis.Y);
    }

    public enum Axis {
        X, Y, Z
    }

    static void visualize2d(String source, Axis axisToIgnore) {
        visualize2d(DF.vanilla(source), DF.vri(source), axisToIgnore);
    }

    static void visualize2d(DF vanilla, DF vri, Axis axisToSpecify) {
        Dimension dimension = new Dimension(512, 512);

        double min = Math.min(vanilla.minValue(), vri.minValue());
        double max = Math.max(vanilla.maxValue(), vri.maxValue());

        BufferedImage vanillaImage = generateImage(vanilla, min, max, 1.0, dimension, axisToSpecify, 0);
        BufferedImage vriImage = generateImage(vri, min, max, 1.0, dimension, axisToSpecify, 0);

        JLabel vanillaLabel = new JLabel(new ImageIcon(vanillaImage));
        JLabel vriLabel = new JLabel(new ImageIcon(vriImage));

        // create window, add images, and block this thread until the window is closed
        JFrame frame = new JFrame("Density Function Visualizer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new FlowLayout());
        frame.getContentPane().add(vanillaLabel);
        frame.getContentPane().add(vriLabel);

        AtomicDouble axisValue = new AtomicDouble(0);
        AtomicDouble axisTarget = new AtomicDouble(0);


        // Add a slider for the axis value
        JSlider axisSlider;
        {
            axisSlider = new JSlider(JSlider.HORIZONTAL, -8000, 8000, 4000);
            axisSlider.setMajorTickSpacing(20);
            axisSlider.setPaintTicks(true);
            axisSlider.setPaintLabels(true);
            axisSlider.addChangeListener(e -> axisTarget.set(axisSlider.getValue() / 1000.0));
        }

        AtomicDouble scaleValue = new AtomicDouble(1.0);
        AtomicDouble scaleTarget = new AtomicDouble(1.0);

        // Add a slider for the scale value
        JSlider scaleSlider;
        {
            scaleSlider = new JSlider(JSlider.HORIZONTAL, 1, 200, (int) (100.0 / 16.0));
            scaleSlider.setMajorTickSpacing(20);
            scaleSlider.setPaintTicks(true);
            scaleSlider.setPaintLabels(true);
            scaleSlider.addChangeListener(e -> scaleTarget.set((scaleSlider.getValue() / 100.0) * 16.0));
        }

        // register a listener to update the image when the slider is moved
        Thread daemon = new Thread(() -> {
            while (true) {
                System.out.println("target: " + axisTarget.get() + ", axisValue: " + axisValue.get());

                if (axisTarget.get() == axisValue.get() && scaleTarget.get() == scaleValue.get()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    continue;
                }

                double currentTarget = axisTarget.get();
                double currentScale = scaleTarget.get();
                System.out.println("Updating image");

                BufferedImage vanillaImage1 = generateImage(vanilla, min, max, currentScale, dimension, axisToSpecify, currentTarget);
                BufferedImage vriImage1 = generateImage(vri, min, max, currentScale, dimension, axisToSpecify, currentTarget);

                vanillaLabel.setIcon(new ImageIcon(vanillaImage1));
                vriLabel.setIcon(new ImageIcon(vriImage1));

                frame.pack();
                frame.repaint();

                axisValue.set(currentTarget);
                scaleValue.set(currentScale);
            }
        });

        daemon.setDaemon(true);
        daemon.start();

        frame.getContentPane().add(axisSlider);
        frame.getContentPane().add(scaleSlider);
        frame.pack();
        frame.setVisible(true);

        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static final ExecutorService executorService = Executors.newFixedThreadPool(8);

    private record Pixel(int x, int z, double alpha) {
    }

    private static BufferedImage generateImage(DF df, double min, double max, double scale, Dimension imageDimensions, Axis axisToSpecify, double axisValue) {
        BufferedImage image = new BufferedImage(imageDimensions.width, imageDimensions.height, BufferedImage.TYPE_INT_RGB);
        {
            Graphics2D graphics = image.createGraphics();

            graphics.setColor(Color.BLACK);
            graphics.fillRect(0, 0, imageDimensions.width, imageDimensions.height);

            image.flush();
        }

        double xStep = scale / imageDimensions.width;
        double zStep = scale / imageDimensions.height;

        List<Supplier<List<Pixel>>> tasks = new ArrayList<>();

        int pixelsPerTask = 1024;

        for (int i = 0; i < imageDimensions.width * imageDimensions.height; i += pixelsPerTask) {
            int from = i;
            int to = Math.min(i + pixelsPerTask, imageDimensions.width * imageDimensions.height);
            tasks.add(() -> {
                List<Pixel> pixels = new ArrayList<>();
                for (int i1 = from; i1 < to; i1++) {
                    int finalX = i1 % imageDimensions.width;
                    int finalZ = i1 / imageDimensions.width;

                    double xCoord = ((double) finalX - (double) imageDimensions.width / 2.0) * xStep;
                    double zCoord = ((double) finalZ - (double) imageDimensions.height / 2.0) * zStep;
                    double yCoord = switch (axisToSpecify) {
                        case X -> df.compute(axisValue, xCoord, zCoord);
                        case Y -> df.compute(xCoord, axisValue, zCoord);
                        case Z -> df.compute(xCoord, zCoord, axisValue);
                    };
                    double range = max - min;
                    double alpha = (yCoord - min) / range;
                    alpha = Math.clamp(alpha, 0, 1);
                    pixels.add(new Pixel(finalX, finalZ, alpha));
                }
                return List.copyOf(pixels);
            });
        }

        var futures = tasks.stream()
            .map(task -> CompletableFuture.supplyAsync(task, executorService))
            .toList();

        for (CompletableFuture<List<Pixel>> future : futures) {
            List<Pixel> pixels = future.join();
            for (Pixel pixel : pixels) {
                Color color = new Color((float) pixel.alpha(), (float) pixel.alpha(), (float) pixel.alpha());
                image.setRGB(pixel.x(), pixel.z(), color.getRGB());
            }
        }

        return image;
    }
}
