package net.minestom.vanilla.datapack.worldgen.storage;

import net.minestom.vanilla.datapack.worldgen.DensityFunction;

import java.util.function.Supplier;

public interface DoubleStorage {

    double obtain(int x, int y, int z);

    static DoubleStorage from(DensityFunction densityFunction) {
        return (x, y, z) -> densityFunction.compute(DensityFunction.context(x, y, z));
    }

    /**
     * A storage that caches an exact, unique value for each 3d coordinate once.
     * @return a new storage that caches the original
     */
    default DoubleStorage exactCache() {
        return new DoubleStorageExactImpl(this);
    }

    /**
     * A storage that caches an exact, unique value for the 2d coordinate (x, z) once.
     * @return a new storage that caches the original
     */
    default DoubleStorage exactCache2d() {
        return new DoubleStorageExact2dImpl(this);
    }

    static DoubleStorage threadLocal(Supplier<DoubleStorage> supplier) {
        return new DoubleStorageThreadLocalImpl(supplier);
    }
}
