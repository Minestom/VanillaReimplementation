package net.minestom.vanilla.datapack.worldgen.storage;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import net.minestom.server.utils.chunk.ChunkUtils;

class DoubleStorageCache2d implements DoubleStorage {

    private final DoubleStorage original;
    private final Long2DoubleMap storage = new Long2DoubleOpenHashMap();
    public DoubleStorageCache2d(DoubleStorage original) {
        this.original = original;
    }


    @Override
    public double obtain(int x, int y, int z) {
        return storage.computeIfAbsent(getIndex(x, z), i -> original.obtain(x, y, z));
    }

    private long getIndex(int x, int z) {
        return ChunkUtils.getChunkIndex(x, z);
    }
}
