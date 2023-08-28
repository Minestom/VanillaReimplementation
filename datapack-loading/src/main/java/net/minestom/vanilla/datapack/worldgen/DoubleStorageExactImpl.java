package net.minestom.vanilla.datapack.worldgen;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;

class DoubleStorageExactImpl implements DoubleStorage {

    private final DoubleStorage original;

    // The storage
    private final Long2DoubleMap storage;
    public DoubleStorageExactImpl(DoubleStorage original) {
        this.original = original;
        this.storage = new Long2DoubleOpenHashMap();
    }

    @Override
    public double obtain(int x, int y, int z) {
        long index = getIndex(x, y, z);
        return storage.computeIfAbsent(index, i -> original.obtain(x, y, z));
    }

    private long getIndex(int x, int y, int z) {
        // 64 bits, separated into 3x 21 bits
        // 21 bits for x, 21 bits for y, 21 bits for z
        long index = 0;
        index |= (x & 0x1FFFFF);
        index |= ((long) (y & 0x1FFFFF) << 21);
        index |= ((long) (z & 0x1FFFFF) << 42);
        return index;
    }
}
