package net.minestom.vanilla.datapack.worldgen.noise;

import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minestom.vanilla.datapack.worldgen.random.WorldgenRandom;

public class NormalNoise implements Noise {

    public record NoiseParameters(double firstOctave, DoubleList amplitudes) {
    }

    private static final double INPUT_FACTOR = 1.0181268882175227;

    public final double valueFactor;
    public final PerlinNoise first;
    public final PerlinNoise second;
    public final double maxValue;

    public NormalNoise(WorldgenRandom random, NoiseParameters parameters) {
        double firstOctave = parameters.firstOctave();
        DoubleList amplitudes = parameters.amplitudes();
        this.first = new PerlinNoise(random, firstOctave, amplitudes);
        this.second = new PerlinNoise(random, firstOctave, amplitudes);

        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        for (int i = 0; i < amplitudes.size(); i += 1) {
            if (amplitudes.getDouble(i) != 0) {
                min = Math.min(min, i);
                max = Math.max(max, i);
            }
        }

        double expectedDeviation = 0.1 * (1 + 1 / (max - min + 1));
        this.valueFactor = (1.0 / 6.0) / expectedDeviation;
        this.maxValue = (this.first.maxValue + this.second.maxValue) * this.valueFactor;
    }

    @Override
    public double sample(double x, double y, double z) {
        double x2 = x * NormalNoise.INPUT_FACTOR;
        double y2 = y * NormalNoise.INPUT_FACTOR;
        double z2 = z * NormalNoise.INPUT_FACTOR;
        return (this.first.sample(x, y, z) + this.second.sample(x2, y2, z2)) * this.valueFactor;
    }

    @Override
    public double minValue() {
        return 0;
    }

    @Override
    public double maxValue() {
        return this.maxValue;
    }
}