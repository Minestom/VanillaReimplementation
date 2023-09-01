package net.minestom.vanilla.datapack.worldgen.storage;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minestom.server.utils.chunk.ChunkUtils;
import space.vectrix.flare.fastutil.Long2ObjectSyncMap;

class DoubleStorageExact2dImpl implements DoubleStorage {

    private final DoubleStorage original;
    private final Long2DoubleMap storage = new Long2DoubleOpenHashMap();
    public DoubleStorageExact2dImpl(DoubleStorage original) {
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
