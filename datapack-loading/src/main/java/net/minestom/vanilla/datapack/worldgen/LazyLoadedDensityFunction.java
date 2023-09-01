package net.minestom.vanilla.datapack.worldgen;

import net.minestom.vanilla.datapack.DatapackLoader;
import net.minestom.vanilla.datapack.DatapackUtils;
import org.jetbrains.annotations.Nullable;

class LazyLoadedDensityFunction implements DensityFunction {

    private @Nullable DensityFunction densityFunction = null;

    public LazyLoadedDensityFunction(String id, DatapackLoader.LoadingContext context) {
        context.whenFinished(finisher -> this.densityFunction = DatapackUtils.findDensityFunction(finisher.datapack(), id)
                .orElseThrow(() -> new IllegalStateException("Density function " + id + " not found")));
    }

    private DensityFunction densityFunction() {
        if (densityFunction == null) {
            throw new IllegalStateException("Density function not loaded yet");
        }
        return densityFunction;
    }

    @Override
    public double compute(Context context) {
        return densityFunction().compute(context);
    }

    @Override
    public double maxValue() {
        return densityFunction().maxValue();
    }

    @Override
    public double minValue() {
        return densityFunction().minValue();
    }
}
