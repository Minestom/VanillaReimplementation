package net.minestom.vanilla.generation.densityfunctions;

import net.minestom.vanilla.generation.Holder;
import net.minestom.vanilla.generation.densityfunctions.DensityFunctionsUtil.ContextImpl;
import net.minestom.vanilla.generation.densityfunctions.NativeDensityFunctions.HolderHolder;
import net.minestom.vanilla.generation.math.NumberFunction;
import net.minestom.vanilla.generation.noise.NoiseSettings;
import net.minestom.vanilla.generation.noise.NormalNoise;
import net.minestom.vanilla.generation.random.WorldgenRandom;

import java.util.Map;
import java.util.function.Function;

public interface DensityFunction extends NumberFunction<DensityFunction.Context> {

    /**
     * Creates a new immutable context from the given coords
     *
     * @param x the x coord
     * @param y the y coord
     * @param z the z coord
     * @return a new immutable context
     */
    static Context context(double x, double y, double z) {
        return new ContextImpl(x, y, z);
    }

    /**
     * Creates a density function from the given density function holder.
     *
     * @param holder the holder to use
     * @return a density function
     */
    static DensityFunction holder(Holder<DensityFunction> holder) {
        return new HolderHolder(holder);
    }

    static Mapper createMapper(long seed, NoiseSettings noiseSettings,
                               Map<String, DensityFunction> mapped,
                               Function<Holder<NormalNoise.NoiseParameters>, NormalNoise> getNoise,
                               WorldgenRandom.Positional random) {
        return DensityFunctionsUtil.createMapper(seed, noiseSettings, mapped, getNoise, random);
    }

    /**
     * Computes the density function value at the given context
     *
     * @param context the context
     * @return the density function value
     */
    double compute(Context context);

    /**
     * Returns the minimum possible value of this density function
     *
     * @return the minimum possible value
     */
    default double minValue() {
        return -maxValue();
    }

    /**
     * Returns the maximum possible value of this density function
     *
     * @return the maximum possible value
     */
    double maxValue();

    /**
     * Maps this density function to a new density function using the given visitor
     *
     * @param mapper the mapper
     * @return the new density function
     */
    default DensityFunction mapAll(Mapper mapper) {
        return mapper.map(this);
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

    /**
     * A density function mapper
     */
    interface Mapper extends NumberFunction.Mapper<DensityFunction.Context> {
        /**
         * Maps the given density function to a new density function
         *
         * @param function the density function
         * @return the new density function
         */
        @Override
        DensityFunction map(NumberFunction<Context> function);
    }

    static DensityFunction fromJson(Object obj) {
        return DensityFunctionsUtil.fromJson(obj);
    }
}
