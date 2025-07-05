package net.minestom.vanilla.datapack.worldgen;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minestom.vanilla.datapack.DatapackLoader;

interface DF {
    double compute(double x, double y, double z);

    record VanillaDF(net.minecraft.world.level.levelgen.DensityFunction df) implements DF {
        @Override
        public double compute(double x, double y, double z) {
            return df.compute(createFunctionContext((int) x, (int) y, (int) z));
        }
    }

    static DF vanilla(String source) {
        JsonElement element = new Gson().fromJson(source, JsonElement.class);
        var result = net.minecraft.world.level.levelgen.DensityFunction.HOLDER_HELPER_CODEC.parse(JsonOps.INSTANCE, element);
        var df = result.getOrThrow(error -> { throw new RuntimeException(error); });
        return new VanillaDF(df);
    }

    record VriDF(DensityFunction df) implements DF {
        @Override
        public double compute(double x, double y, double z) {
            return df.compute(DensityFunction.context(x, y, z));
        }
    }

    static DF vri(String source) {
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