package net.minestom.vanilla.datapack.worldgen;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minestom.vanilla.datapack.DatapackLoader;

interface DF {
    static DF zero() {
        return new DF() {
            @Override
            public double compute(double x, double y, double z) {
                return 0;
            }

            @Override
            public double minValue() {
                return 0;
            }

            @Override
            public double maxValue() {
                return 0;
            }
        };
    }

    double compute(double x, double y, double z);

    double minValue();
    double maxValue();

    static DF vanilla(String source) {
        JsonElement element = new Gson().fromJson(source, JsonElement.class);
        var result = net.minecraft.world.level.levelgen.DensityFunction.HOLDER_HELPER_CODEC.decode(JsonOps.INSTANCE, element);
        var df = result.getOrThrow(false, error -> {}).getFirst();
        df.mapAll(densityFunction -> densityFunction);
        return new VanillaDF(df);
    }

    static DF vri(String source) {
        DensityFunction df = DatapackLoader.adaptor(DensityFunction.class).apply(source);
        return new VriDF(df);
    }

    record VanillaDF(net.minecraft.world.level.levelgen.DensityFunction df) implements DF {
        @Override
        public double compute(double x, double y, double z) {
            return df.compute(createFunctionContext((int) x, (int) y, (int) z));
        }

        @Override
        public double minValue() {
            return df.minValue();
        }

        @Override
        public double maxValue() {
            return df.maxValue();
        }
    }

    record VriDF(DensityFunction df) implements DF {
        @Override
        public double compute(double x, double y, double z) {
            return df.compute(DensityFunction.context(x, y, z));
        }

        @Override
        public double minValue() {
            return df.minValue();
        }

        @Override
        public double maxValue() {
            return df.maxValue();
        }
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
