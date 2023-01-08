package net.minestom.vanilla.generation.noise;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minestom.vanilla.generation.Util;
import net.minestom.vanilla.generation.random.WorldgenRandom;
import org.jetbrains.annotations.NotNull;

public record NormalNoise(@NotNull PerlinNoise first, @NotNull PerlinNoise second, double valueFactor, double maxValue) implements Noise {

    public record NoiseParameters(double firstOctave, DoubleList amplitudes) {

        public NoiseParameters(double firstOctave, double... amplitudes) {
            this(firstOctave, DoubleList.of(amplitudes));
        }

        public static NoiseParameters create(double firstOctave, double @NotNull... amplitudes) {
            return new NoiseParameters(firstOctave, DoubleList.of(amplitudes));
        }

        public static NoiseParameters fromJson(Object json) {
            JsonObject root = Util.jsonObject(json);
            double firstOctave = root.get("firstOctave").isJsonNull() ? 0 : root.get("firstOctave").getAsDouble();
            double[] amplitudes = root.get("amplitudes").isJsonNull() ? new double[0] : new Gson().fromJson(root.get("amplitudes"), double[].class);
            return new NoiseParameters(firstOctave, DoubleList.of(amplitudes));
        }
    }

    private static final double INPUT_FACTOR = 1.0181268882175227;

    public static @NotNull NormalNoise ofRandom(WorldgenRandom random, NoiseParameters parameters) {
        double firstOctave = parameters.firstOctave();
        DoubleList amplitudes = parameters.amplitudes();
        var first = new PerlinNoise(random, firstOctave, amplitudes);
        var second = new PerlinNoise(random, firstOctave, amplitudes);

        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        for (int i = 0; i < amplitudes.size(); i += 1) {
            if (amplitudes.getDouble(i) != 0) {
                min = Math.min(min, i);
                max = Math.max(max, i);
            }
        }

        double expectedDeviation = 0.1 * (1 + 1 / (max - min + 1));
        var valueFactor = (1.0 / 6.0) / expectedDeviation;
        var maxValue = (first.maxValue + second.maxValue) * valueFactor;

        return new NormalNoise(first, second, valueFactor, maxValue);
    }

    @Override
    public double sample(double x, double y, double z) {
        double x2 = x * NormalNoise.INPUT_FACTOR;
        double y2 = y * NormalNoise.INPUT_FACTOR;
        double z2 = z * NormalNoise.INPUT_FACTOR;
        return (this.first.sample(x, y, z) + this.second.sample(x2, y2, z2)) * this.valueFactor;
    }
}