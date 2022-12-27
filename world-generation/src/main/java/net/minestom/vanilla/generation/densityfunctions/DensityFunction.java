package net.minestom.vanilla.generation.densityfunctions;

import net.minestom.vanilla.generation.math.NumberFunction;

public interface DensityFunction extends DensityFunctions, NumberFunction<DensityFunction.Context> {
    double compute(Context context);

    default double minValue() {
        return -maxValue();
    }
    double maxValue();

    default DensityFunction mapAll(DensityFunction.Visitor visitor)  {
        return visitor.map().apply(this);
    }

    interface Context {
        double x();
        default int blockX() {
            return (int) Math.floor(x());
        }
        double y();
        default int blockY() {
            return (int) Math.floor(y());
        }
        double z();
        default int blockZ() {
            return (int) Math.floor(z());
        }
    }
}
