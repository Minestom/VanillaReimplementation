package net.minestom.vanilla.generation.util;

import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

class DoubleStorageExactImpl implements DoubleStorage {

    private final DoubleStorage original;

    // The storage
    private final Long2ObjectMap<Int2DoubleMap> storage = new Long2ObjectOpenHashMap<>();

    public DoubleStorageExactImpl(DoubleStorage original) {
        this.original = original;
    }

    @Override
    public double obtain(int x, int y, int z) {
        long index = getIndex(x, z);
        return storage.computeIfAbsent(index, i -> new Int2DoubleOpenHashMap()).computeIfAbsent(y, i -> original.obtain(x, y, z));
    }

    private long getIndex(int x, int z) {
        return ((long) x << 32) | (z & 0xFFFFFFFFL);
    }
}
