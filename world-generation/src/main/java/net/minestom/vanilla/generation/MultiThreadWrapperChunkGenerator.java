package net.minestom.vanilla.generation;

import net.minestom.server.instance.ChunkGenerator;
import net.minestom.server.instance.ChunkPopulator;
import net.minestom.server.instance.batch.ChunkBatch;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public class MultiThreadWrapperChunkGenerator implements ChunkGenerator {

    private final ThreadLocal<ChunkGenerator> threadLocalGenerator;


    <T extends ChunkGenerator> MultiThreadWrapperChunkGenerator(Supplier<T> supplier) {
        threadLocalGenerator = ThreadLocal.withInitial(supplier);
    }

    @Override
    public void generateChunkData(@NotNull ChunkBatch batch, int chunkX, int chunkZ) {
        threadLocalGenerator.get().generateChunkData(batch, chunkX, chunkZ);
    }

    @Override
    public @Nullable List<ChunkPopulator> getPopulators() {
        return threadLocalGenerator.get().getPopulators();
    }
}
