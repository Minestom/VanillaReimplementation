package net.minestom.vanilla.datapack.worldgen;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minestom.server.coordinate.Vec;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DensityFunctionTests {

    private static final double DELTA = 0.0001;

    @BeforeAll
    public static void prepare() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    // End islands are used in many other tests. So test them first.
    private final String END_ISLANDS = "{ \"type\": \"minecraft:end_islands\" }";
    @Test
    public void testEndIslands() {
        assertExact(END_ISLANDS);
    }

    @Test
    public void testConstant() {
        assertExact("1.0");
        assertExact("0.0");
        assertExact("-1.0");
        assertExact("0.5");
        assertExact("1");
        assertExact("0");
    }

    @Test
    public void testClamp() {
        assertExact(String.format("""
                {
                  "type": "minecraft:clamp",
                  "input": %s,
                  "min": -0.5,
                  "max": 0.5
                }
                """, END_ISLANDS));
        assertExact(String.format("""
                                {
                  "type": "minecraft:clamp",
                  "input": %s,
                  "min": -0.23,
                  "max": 1.0
                }
                """, END_ISLANDS));
        assertExact(String.format("""
                                {
                  "type": "minecraft:clamp",
                  "input": %s,
                  "min": -1.0,
                  "max": 0.23
                }
                """, END_ISLANDS));
        assertExact(String.format("""
                                {
                  "type": "minecraft:clamp",
                  "input": %s,
                  "min": -0.23,
                  "max": 0.23
                }
                """, END_ISLANDS));
        assertExact(String.format("""
                                {
                  "type": "minecraft:clamp",
                  "input": %s,
                  "min": 0.23,
                  "max": 0.23
                }
                """, END_ISLANDS));
    }

    @Test
    public void testAbs() {
        assertExact(String.format("""
                {
                  "type": "minecraft:abs",
                  "argument": %s
                }
                """, END_ISLANDS));
    }

    @Test
    public void testSquare() {
        assertExact(String.format("""
                {
                  "type": "minecraft:square",
                  "argument": %s
                }
                """, END_ISLANDS));
    }

    private final String END_ISLANDS_CUBED = String.format("""
                {
                  "type": "minecraft:cube",
                  "argument": %s
                }
                """, END_ISLANDS);
    @Test
    public void testCube() {
        assertExact(END_ISLANDS_CUBED);
    }

    @Test
    public void testHalfNegative() {
        assertExact(String.format("""
                {
                  "type": "minecraft:half_negative",
                  "argument": %s
                }
                """, END_ISLANDS));
    }

    @Test
    public void testQuarterNegative() {
        assertExact(String.format("""
                {
                  "type": "minecraft:quarter_negative",
                  "argument": %s
                }
                """, END_ISLANDS));
    }

    @Test
    public void testSqueeze() {
        assertExact(String.format("""
                {
                  "type": "minecraft:squeeze",
                  "argument": %s
                }
                """, END_ISLANDS));
    }

    @Test
    public void testAdd() {
        assertExact(String.format("""
                {
                  "type": "minecraft:add",
                  "argument1": %s,
                  "argument2": %s
                }
                """, END_ISLANDS, END_ISLANDS_CUBED));
    }

    @Test
    public void testMul() {
        assertExact(String.format("""
                {
                  "type": "minecraft:mul",
                  "argument1": %s,
                  "argument2": %s
                }
                """, END_ISLANDS, END_ISLANDS_CUBED));
        assertExact(String.format("""
                {
                  "type": "minecraft:mul",
                  "argument1": %s,
                  "argument2": 2.0
                }
                """, END_ISLANDS));
        assertExact(String.format("""
                {
                  "type": "minecraft:mul",
                  "argument1": %s,
                  "argument2": 128
                }
                """, END_ISLANDS));
    }

    @Test
    public void testMin() {
        assertExact(String.format("""
                {
                  "type": "minecraft:min",
                  "argument1": %s,
                  "argument2": %s
                }
                """, END_ISLANDS, END_ISLANDS_CUBED));
    }

    @Test
    public void testMax() {
        assertExact(String.format("""
                {
                  "type": "minecraft:max",
                  "argument1": %s,
                  "argument2": %s
                }
                """, END_ISLANDS, END_ISLANDS_CUBED));
    }

    // Noise is the big boi, so test it thoroughly.
    @Test
    public void testNoise() {
        assertExact("""
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
                """);
    }

    private void testPositions(BiConsumer<Vec, Integer> consumer) {
        double dist = 0.0001;

        Random random = new Random(0);
        int i = 0;
        while (dist < 100000.0) {
            dist *= 1.1;

            double x = random.nextDouble(-dist, dist);
            double y = random.nextDouble(-dist, dist);
            double z = random.nextDouble(-dist, dist);
            consumer.accept(new Vec(x, y, z), i++);
        }
    }

    private void assertExact(String source) {
        assertExact(DF.vanilla(source), DF.vri(source), DELTA);
    }

    private void assertExact(DF vanilla, DF vri, double delta) {
        testPositions((pos, i) -> {
            var result = compare(vanilla, vri, pos.blockX(), pos.blockY(), pos.blockZ());
            int finalI = i;
            result.assertEqual(delta, () -> "Failed at " + pos + " (index " + finalI + ")");
        });
    }

    private record Result(double vanilla, double vri) {
        public void assertEqual(double delta, Supplier<String> message) {
            assertEquals(vanilla, vri, delta, message);
        }
    }

    private Result compare(DF vanilla, DF vri, int x, int y, int z) {
        var vanillaRes = vanilla.compute(x, y, z);
        var vriRes = vri.compute(x, y, z);
        return new Result(vanillaRes, vriRes);
    }
}
