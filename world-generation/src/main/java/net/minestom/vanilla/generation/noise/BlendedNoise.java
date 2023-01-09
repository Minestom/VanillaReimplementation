package net.minestom.vanilla.generation.noise;

import net.minestom.vanilla.generation.Util;
import net.minestom.vanilla.generation.random.WorldgenRandom;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

record BlendedNoise(@NotNull PerlinNoise minLimitNoise,
                    @NotNull PerlinNoise maxLimitNoise,
                    @NotNull PerlinNoise mainNoise,
                    double xzMultiplier,
                    double yMultiplier,
                    double maxValue,
                    double xzFactor,
                    double yFactor,
                    double smearScaleMultiplier) implements Noise.Bounded {

    static @NotNull BlendedNoise create(@NotNull WorldgenRandom random, double xzScale, double yScale, double xzFactor, double yFactor, double smearScaleMultiplier) {
        var minLimitNoise = new PerlinNoise(random, -15, new double[]{1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0});
        var maxLimitNoise = new PerlinNoise(random, -15, new double[]{1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0});
        return new BlendedNoise(
                minLimitNoise,
                maxLimitNoise,
                new PerlinNoise(random, -7, new double[]{1.0, 1.0, 1.0, 1.0, 1.0, 0.0, 0.0, 0.0}),
                684.412 * xzScale,
                684.412 * yScale,
                minLimitNoise.edgeValue(yScale + 2), // TODO
                xzFactor,
                yFactor,
                smearScaleMultiplier
        );
    }

    @Override
    public double sample(double x, double y, double z) {
        double scaledX = x * this.xzMultiplier;
        double scaledY = y * this.yMultiplier;
        double scaledZ = z * this.xzMultiplier;

        double factoredX = scaledX / this.xzFactor;
        double factoredY = scaledY / this.yFactor;
        double factoredZ = scaledZ / this.xzFactor;

        double smear = this.yMultiplier * this.smearScaleMultiplier;
        double factoredSmear = smear / this.yFactor;

        @Nullable Noise.Scaled noise;
        double value = 0;
        double factor = 1;
        for (int i = 0; i < 8; i += 1) {
            noise = this.mainNoise.getOctaveNoise(i);
            if (noise != null) {
                double xx = PerlinNoise.wrap(factoredX * factor);
                double yy = PerlinNoise.wrap(factoredY * factor);
                double zz = PerlinNoise.wrap(factoredZ * factor);
                value += noise.sample(xx, yy, zz, factoredSmear * factor, factoredY * factor) / factor;
            }
            factor /= 2;
        }

        value = (value / 10 + 1) / 2;
        factor = 1;
        double min = 0;
        double max = 0;
        for (int i = 0; i < 16; i += 1) {
            double xx = PerlinNoise.wrap(scaledX * factor);
            double yy = PerlinNoise.wrap(scaledY * factor);
            double zz = PerlinNoise.wrap(scaledZ * factor);
            double smearsmear = smear * factor;
            if (value < 1 && (noise = this.minLimitNoise.getOctaveNoise(i)) != null) {
                min += noise.sample(xx, yy, zz, smearsmear, scaledY * factor) / factor;
            }
            if (value > 0 && (noise = this.maxLimitNoise.getOctaveNoise(i)) != null) {
                max += noise.sample(xx, yy, zz, smearsmear, scaledY * factor) / factor;
            }
            factor /= 2;
        }

        return Util.clampedLerp(min / 512, max / 512, value) / 128;
    }
}
