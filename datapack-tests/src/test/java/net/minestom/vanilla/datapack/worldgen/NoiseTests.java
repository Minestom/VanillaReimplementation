package net.minestom.vanilla.datapack.worldgen;

import net.minecraft.world.level.levelgen.LegacyRandomSource;
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
}
