package net.minestom.vanilla.generation;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.batch.ChunkBatch;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.world.biomes.Biome;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.function.ObjIntConsumer;

public interface TargetChunk extends Block.Getter, Block.Setter, Biome.Getter, Biome.Setter {

    static TargetChunk from(int chunkX, int chunkZ, int minSection, int maxSection) {
        return new TargetChunkImpl(chunkX, chunkZ, minSection, maxSection);
    }

    int chunkX();

    int chunkZ();

    default int minX() {
        return chunkX() * Chunk.CHUNK_SIZE_X;
    }

    default int maxX() {
        return minX() + Chunk.CHUNK_SIZE_X;
    }

    default int minZ() {
        return chunkZ() * Chunk.CHUNK_SIZE_Z;
    }

    default int maxZ() {
        return minZ() + Chunk.CHUNK_SIZE_Z;
    }

    default long index() {
        return ChunkUtils.getChunkIndex(chunkX(), chunkZ());
    }

    int minSection();

    int maxSection();

    default int minY() {
        return minSection() * Chunk.CHUNK_SECTION_SIZE;
    }

    default int maxY() {
        return (maxSection() + 1) * Chunk.CHUNK_SECTION_SIZE;
    }

    default long chunk() {
        return ChunkUtils.getChunkIndex(chunkX(), chunkZ());
    }

    void apply(ObjIntConsumer<Block> blocks, ObjIntConsumer<Biome> biomes);
}

class TargetChunkImpl implements TargetChunk {

    private final int chunkX;
    private final int chunkZ;

    private final int minSection;
    private final int maxSection;

    private final Int2ObjectMap<Block> blocks = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectMap<Biome> biomes = new Int2ObjectOpenHashMap<>();

    public TargetChunkImpl(int chunkX, int chunkZ, int minSection, int maxSection) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.minSection = minSection;
        this.maxSection = maxSection;
    }

    @Override
    public int chunkX() {
        return this.chunkX;
    }

    @Override
    public int chunkZ() {
        return this.chunkZ;
    }

    @Override
    public int minSection() {
        return this.minSection;
    }

    @Override
    public int maxSection() {
        return this.maxSection;
    }

    @Override
    public void apply(ObjIntConsumer<Block> blocks, ObjIntConsumer<Biome> biomes) {
        this.blocks.int2ObjectEntrySet().forEach(entry -> blocks.accept(entry.getValue(), entry.getIntKey()));
        this.biomes.int2ObjectEntrySet().forEach(entry -> biomes.accept(entry.getValue(), entry.getIntKey()));
    }

    @Override
    public @UnknownNullability Block getBlock(int x, int y, int z, @NotNull Condition condition) {
        int index = ChunkUtils.getBlockIndex(x, y, z);
        return this.blocks.getOrDefault(index, Block.STONE);
    }

    @Override
    public void setBlock(int x, int y, int z, @NotNull Block block) {
        if (x < minX() || x >= maxX() || y < minY() || y >= maxY() || z < minZ() || z >= maxZ()) {
            return;
        }
        int index = ChunkUtils.getBlockIndex(x, y, z);
        this.blocks.put(index, block);
    }

    @Override
    public @NotNull Biome getBiome(int x, int y, int z) {
        int index = ChunkUtils.getBlockIndex(x, y, z);
        return this.biomes.getOrDefault(index, Biome.PLAINS);
    }

    @Override
    public void setBiome(int x, int y, int z, @NotNull Biome biome) {
        if (x < minX() || x >= maxX() || y < minY() || y >= maxY() || z < minZ() || z >= maxZ()) {
            return;
        }
        int index = ChunkUtils.getBlockIndex(x, y, z);
        this.biomes.put(index, biome);
    }
}
