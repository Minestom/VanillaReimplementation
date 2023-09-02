package net.minestom.vanilla.datapack.worldgen;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minestom.server.coordinate.Vec;
import net.minestom.vanilla.datapack.DatapackLoader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DensityFunctionUnitTests {

    @BeforeAll
    public static void prepare() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    public void testConstant() {
        assertExact("1.0");
    }

    @Test
    public void testEndIslands() {
        assertExact("""
                {
                  "type": "minecraft:end_islands"
                }
                """);
    }

//    @Test
//    public void testSpline() {
//        assertExact("""
//                {
//                    "type": "minecraft:clamp",
//                    "source": {
//                        "type": "minecraft:constant",
//                        "value": 1.0
//                    },
//                    "min": 0.0,
//                    "max": 2.0
//                }
//                """);
//    }

    private static final Set<Vec> testPositions;

    static {
        Set<Vec> tests = new HashSet<>();

        double dist = 0.0001;

        Random random = new Random(0);
        while (dist < 100000.0) {
            dist *= 2.0;

            double x = random.nextDouble(-dist, dist);
            double y = random.nextDouble(-dist, dist);
            double z = random.nextDouble(-dist, dist);
            tests.add(new Vec(x, y, z));
        }

        testPositions = Set.copyOf(tests);
    }

    private interface DF {
        double compute(int x, int y, int z);
    }

    private void assertExact(String source) {
        assertExact(vanilla(source), vri(source));
    }

    private void assertExact(DF vanilla, DF vri) {
        for (Vec pos : testPositions) {
            var result = compare(vanilla, vri, pos.blockX(), pos.blockY(), pos.blockZ());
            result.assertEqual(0.0);
        }
    }

    private record Result(double vanilla, double vri) {
        public void assertEqual(double delta) {
            assertEquals(vanilla, vri, delta);
        }
    }

    private Result compare(DF vanilla, DF vri, int x, int y, int z) {
        var vanillaRes = vanilla.compute(x, y, z);
        var vriRes = vri.compute(x, y, z);
        return new Result(vanillaRes, vriRes);
    }

    private DF vanilla(String source) {
        JsonElement element = new Gson().fromJson(source, JsonElement.class);
        var result = net.minecraft.world.level.levelgen.DensityFunction.HOLDER_HELPER_CODEC.parse(JsonOps.INSTANCE, element);
        var df = result.getOrThrow(false, error -> { throw new RuntimeException(error); });
        return (x, y, z) -> df.compute(createFunctionContext(x, y, z));
    }

    private DF vri(String source) {
        DensityFunction df = DatapackLoader.adaptor(DensityFunction.class).apply(source);
        return (x, y, z) -> df.compute(DensityFunction.context(x, y, z));
    }

    private net.minecraft.world.level.levelgen.DensityFunction.FunctionContext createFunctionContext(int x, int y, int z) {
        return new net.minecraft.world.level.levelgen.DensityFunction.FunctionContext() {
            @Override
            public int blockX() {
                return x;
            }

            @Override
            public int blockY() {
                return y;
            }

            @Override
            public int blockZ() {
                return z;
            }
        };
    }
}
