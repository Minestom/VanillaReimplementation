package net.minestom.vanilla.datapack.worldgen.storage;

import java.util.function.Supplier;

public class DoubleStorageThreadLocalImpl implements DoubleStorage {

    private final ThreadLocal<DoubleStorage> threadLocal;
    public DoubleStorageThreadLocalImpl(Supplier<DoubleStorage> supplier) {
        this.threadLocal = ThreadLocal.withInitial(supplier);
    }

    @Override
    public double obtain(int x, int y, int z) {
        return threadLocal.get().obtain(x, y, z);
    }
}
