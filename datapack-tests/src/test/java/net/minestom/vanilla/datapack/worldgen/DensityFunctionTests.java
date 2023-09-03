package net.minestom.vanilla.datapack.worldgen;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minestom.server.coordinate.Vec;
import net.minestom.vanilla.datapack.DatapackLoader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DensityFunctionTests {

    private static final double DELTA = 0.0000001;

    @BeforeAll
    public static void prepare() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }


    // End islands are used in many other tests. So test them first.
    private final String END_ISLANDS_STRING = """
            {
              "type": "minecraft:end_islands"
            }
            """;

    private final DF vanillaEndIslands = vanilla(END_ISLANDS_STRING);
    private final DF vriEndIslands = vri(END_ISLANDS_STRING);
    @Test
    public void testEndIslands() {
        assertExact(vanillaEndIslands, vriEndIslands, DELTA);
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


    private static final List<Vec> testPositions;

    static {
        List<Vec> tests = new ArrayList<>();

        double dist = 0.0001;

        Random random = new Random(0);
        while (dist < 100000.0) {
            dist *= 2.0;

            double x = random.nextDouble(-dist, dist);
            double y = random.nextDouble(-dist, dist);
            double z = random.nextDouble(-dist, dist);
            tests.add(new Vec(x, y, z));
        }

        testPositions = List.copyOf(tests);
    }

    private interface DF {
        double compute(int x, int y, int z);
    }

    private void assertExact(String source) {
        assertExact(vanilla(source), vri(source), DELTA);
    }

    private void assertExact(DF vanilla, DF vri, double delta) {
        for (int i = 0; i < testPositions.size(); i++) {
            Vec pos = testPositions.get(i);
            var result = compare(vanilla, vri, pos.blockX(), pos.blockY(), pos.blockZ());
            int finalI = i;
            result.assertEqual(delta, () -> "Failed at " + pos + " (index " + finalI + ")");
        }
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

    private record VanillaDF(net.minecraft.world.level.levelgen.DensityFunction df) implements DF {
        @Override
        public double compute(int x, int y, int z) {
            return df.compute(createFunctionContext(x, y, z));
        }
    }

    private static DF vanilla(String source) {
        JsonElement element = new Gson().fromJson(source, JsonElement.class);
        var result = net.minecraft.world.level.levelgen.DensityFunction.HOLDER_HELPER_CODEC.parse(JsonOps.INSTANCE, element);
        var df = result.getOrThrow(false, error -> { throw new RuntimeException(error); });
        return new VanillaDF(df);
    }

    private record VriDF(DensityFunction df) implements DF {
        @Override
        public double compute(int x, int y, int z) {
            return df.compute(DensityFunction.context(x, y, z));
        }
    }

    private static DF vri(String source) {
        DensityFunction df = DatapackLoader.adaptor(DensityFunction.class).apply(source);
        return new VriDF(df);
    }

    private static net.minecraft.world.level.levelgen.DensityFunction.FunctionContext createFunctionContext(int x, int y, int z) {
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
