package net.minestom.vanilla.datapack.worldgen;

import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minestom.vanilla.datapack.worldgen.noise.SimplexNoise;
import net.minestom.vanilla.datapack.worldgen.random.WorldgenRandom;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NoiseTests {

    @Test
    public void testSimplex() {
        testSimplex(0);
        testSimplex(123);
        testSimplex(123456789);
        testSimplex(-123456789);
    }

    @Test
    public void testNormal() {
        testNormal(0, -2, 1.0);
        testNormal(123, -2, 1.0);
        testNormal(123456789, -2, 1.0);
        testNormal(-123456789, -2, 1.0);
    }

    private void testSimplex(long seed) {
        Noise vanilla = simplexVanilla(seed);
        Noise vri = simplexVri(seed);
        Random random = new Random(seed);

        // 3d
        for (int i = 0; i < 1000; i++) {
            double bound = Math.sqrt(i + 1);
            double x = random.nextDouble(-bound, bound);
            double y = random.nextDouble(-bound, bound);
            double z = random.nextDouble(-bound, bound);
            double vanillaRes = vanilla.sample(x, y, z);
            double vriRes = vri.sample(x, y, z);
            assertEquals(vanillaRes, vriRes, 0.000001, "x=" + x + ", y=" + y + ", z=" + z + " failed (i=" + i + ")");
        }

        // 2d
        for (int i = 0; i < 1000; i++) {
            double bound = Math.sqrt(i + 1);
            double x = random.nextDouble(-bound, bound);
            double y = random.nextDouble(-bound, bound);
            double vanillaRes = vanilla.sample2d(x, y);
            double vriRes = vri.sample2d(x, y);
            assertEquals(vanillaRes, vriRes, 0.000001, "x=" + x + ", y=" + y + " failed (i=" + i + ")");
        }
    }

    private void testNormal(long seed, int firstOctave, double... amplitudes) {
        Noise vanilla = normalVanilla(seed, firstOctave, DoubleList.of(amplitudes));
        Noise vri = normalVri(seed, firstOctave, DoubleList.of(amplitudes));
        Random random = new Random(seed);

        // 3d
        for (int i = 0; i < 1000; i++) {
            double bound = Math.sqrt(i + 1);
            double x = random.nextDouble(-bound, bound);
            double y = random.nextDouble(-bound, bound);
            double z = random.nextDouble(-bound, bound);
            double vanillaRes = vanilla.sample(x, y, z);
            double vriRes = vri.sample(x, y, z);
            assertEquals(vanillaRes, vriRes, 0.000001, "x=" + x + ", y=" + y + ", z=" + z + " failed (i=" + i + ")");
        }
    }


    private interface Noise {
        double sample(double x, double y, double z);
        double sample2d(double x, double y);
    }


    private static Noise simplexVanilla(long seed) {
        var noise = new net.minecraft.world.level.levelgen.synth.SimplexNoise(new LegacyRandomSource(seed));
        return new Noise() {
            @Override
            public double sample(double x, double y, double z) {
                return noise.getValue(x, y, z);
            }

            @Override
            public double sample2d(double x, double y) {
                return noise.getValue(x, y);
            }
        };
    }

    private static Noise simplexVri(long seed) {
        var noise = new SimplexNoise(WorldgenRandom.legacy(seed));
        return new Noise() {
            @Override
            public double sample(double x, double y, double z) {
                return noise.sample(x, y, z);
            }

            @Override
            public double sample2d(double x, double y) {
                return noise.sample2D(x, y);
            }
        };
    }


    private static Noise normalVanilla(long seed, int firstOctave, DoubleList amplitudes) {
        var noise = NormalNoise.create(
                new LegacyRandomSource(seed),
                new NormalNoise.NoiseParameters(firstOctave, amplitudes)
        );
        return new Noise() {
            @Override
            public double sample(double x, double y, double z) {
                return noise.getValue(x, y, z);
            }

            @Override
            public double sample2d(double x, double y) {
                return 0;
            }
        };
    }

    private static Noise normalVri(long seed, int firstOctave, DoubleList amplitudes) {
        var noise = new net.minestom.vanilla.datapack.worldgen.noise.NormalNoise(
                WorldgenRandom.legacy(seed),
                new net.minestom.vanilla.datapack.worldgen.noise.NormalNoise.Config(firstOctave, amplitudes)
        );
        return new Noise() {
            @Override
            public double sample(double x, double y, double z) {
                return noise.sample(x, y, z);
            }

            @Override
            public double sample2d(double x, double y) {
                return 0;
            }
        };
    }
}
