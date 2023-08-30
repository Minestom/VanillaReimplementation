package net.minestom.vanilla.datapack.worldgen;

import org.jetbrains.annotations.NotNull;

public interface DoubleStorage {

    double obtain(int x, int y, int z);

    static DoubleStorage from(DensityFunction densityFunction) {
        return (x, y, z) -> densityFunction.compute(DensityFunction.context(x, y, z));
    }

    /**
     * A storage that caches an exact, unique value for each 3d coordinate once.
     * @param original the original storage to cache
     * @return a new storage that caches the original
     */
    static DoubleStorage exactCache(DoubleStorage original) {
        return new DoubleStorageExactImpl(original);
    }
}
