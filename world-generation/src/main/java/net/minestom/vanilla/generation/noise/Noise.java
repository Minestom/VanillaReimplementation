package net.minestom.vanilla.generation.noise;

import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minestom.vanilla.generation.Util;
import net.minestom.vanilla.generation.random.WorldgenRandom;
import org.jetbrains.annotations.NotNull;

public interface Noise {

    record NoiseParameters(double firstOctave, DoubleList amplitudes) {

        public NoiseParameters(double firstOctave, double... amplitudes) {
            this(firstOctave, DoubleList.of(amplitudes));
        }

        public static NoiseParameters create(double firstOctave, double @NotNull... amplitudes) {
            return new NoiseParameters(firstOctave, DoubleList.of(amplitudes));
        }

        public static NoiseParameters fromJson(Object json) {

            JsonObject root = Util.jsonObject(json);
            double firstOctave = root.get("firstOctave").isJsonNull() ? 0 : root.get("firstOctave").getAsDouble();
            double[] amplitudes = root.get("amplitudes").isJsonNull() ? new double[0] : Util.GSON.fromJson(root.get("amplitudes"), double[].class);
            return new NoiseParameters(firstOctave, DoubleList.of(amplitudes));
        }
    }

    /**
     * Represents noise that is bounded. Blended noise is typically used here.
     */
    interface Bounded extends Noise {

        /**
         * @return the maximum value of this noise
         */
        double maxValue();

    }

    interface TwoDimensional extends Noise {

        double sample2D(double d, double d2);

    }

    interface Scaled extends Noise {

        double yOffset();

        double sample(double x, double y, double z, double yScale, double yLimit);

    }

    double sample(double x, double y, double z);

    static @NotNull Noise.Bounded blended(@NotNull WorldgenRandom random, double xzScale, double yScale, double xzFactor, double yFactor, double smearScaleMultiplier) {
        return BlendedNoise.create(random, xzScale, yScale, xzFactor, yFactor, smearScaleMultiplier);
    }

    static @NotNull Noise.Scaled improved(@NotNull WorldgenRandom random) {
        return ImprovedNoise.create(random);
    }

    static @NotNull Noise.Bounded normal(@NotNull WorldgenRandom random, @NotNull NormalNoise.NoiseParameters parameters) {
        return NormalNoise.create(random, parameters);
    }

    static @NotNull Noise.TwoDimensional simplex(@NotNull WorldgenRandom random) {
        return SimplexNoise.create(random);
    }

    static @NotNull Noise perlin(@NotNull WorldgenRandom random, double firstOctave, double @NotNull [] amplitudes) {
        return new PerlinNoise(random, firstOctave, amplitudes);
    }

    static @NotNull Noise perlin(@NotNull WorldgenRandom random, double firstOctave, @NotNull DoubleList amplitudes) {
        return new PerlinNoise(random, firstOctave, amplitudes);
    }
}
