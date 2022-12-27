package net.minestom.vanilla.generation;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.ChunkGenerator;
import net.minestom.server.instance.ChunkPopulator;
import net.minestom.server.instance.batch.ChunkBatch;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.world.DimensionType;
import net.minestom.vanilla.generation.biome.BiomeSource;
import net.minestom.vanilla.generation.noise.NoiseChunk;
import net.minestom.vanilla.generation.noise.NoiseGeneratorSettings;
import net.minestom.vanilla.generation.noise.NoiseSettings;
import net.minestom.vanilla.generation.noise.VerticalAnchor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NoiseChunkGenerator implements ChunkGenerator {
    private final Map<Long, NoiseChunk> noiseChunkCache = new HashMap<>();
    private final Aquifer.FluidPicker globalFluidPicker;

//    constructor(
//                    private readonly biomeSource: BiomeSource,
//                    private readonly settings: NoiseGeneratorSettings,
//                    ) {
//        this.noiseChunkCache = new Map()
//
//    const lavaFluid = new FluidStatus(-54, BlockState.LAVA)
//    const defaultFluid = new FluidStatus(settings.seaLevel, settings.defaultFluid)
//        this.globalFluidPicker = (x, y, z) => {
//            if (y < Math.min(-54, settings.seaLevel)) {
//                return lavaFluid
//            }
//            return defaultFluid
//        }
//    }

    private final @NotNull BiomeSource biomeSource;
    private final @NotNull NoiseGeneratorSettings settings;


    // Minestom
    private final DimensionType dimensionType;

    public NoiseChunkGenerator(@NotNull BiomeSource biomeSource, @NotNull NoiseGeneratorSettings settings, DimensionType dimensionType) {
        this.biomeSource = biomeSource;
        this.settings = settings;
        this.dimensionType = dimensionType;

        Aquifer.FluidStatus lavaFluid = new Aquifer.FluidStatus(-54, Block.LAVA);
        Aquifer.FluidStatus defaultFluid = new Aquifer.FluidStatus(settings.seaLevel(), settings.defaultFluid());
        this.globalFluidPicker = (x, y, z) -> {
            if (y < Math.min(-54, settings.seaLevel())) {
                return lavaFluid;
            }
            return defaultFluid;
        };
    }

//    public fill(randomState: RandomState, chunk: Chunk, onlyFirstZ: boolean = false) {
//        const minY = Math.max(chunk.minY, this.settings.noise.minY)
//        const maxY = Math.min(chunk.maxY, this.settings.noise.minY + this.settings.noise.height)
//
//        const cellWidth = NoiseSettings.cellWidth(this.settings.noise)
//        const cellHeight = NoiseSettings.cellHeight(this.settings.noise)
//        const cellCountXZ = Math.floor(16 / cellWidth)
//
//        const minCellY = Math.floor(minY / cellHeight)
//        const cellCountY = Math.floor((maxY - minY) / cellHeight)
//
//        const minX = ChunkPos.minBlockX(chunk.pos)
//        const minZ = ChunkPos.minBlockZ(chunk.pos)
//
//        const noiseChunk = this.getOrCreateNoiseChunk(randomState, chunk)

    public void fill(RandomState randomState, TargetChunk chunk) {
        fill(randomState, chunk, false);
    }

    public void fill(RandomState randomState, TargetChunk chunk, boolean onlyFirstZ) {
        int minY = Math.max(chunk.minY(), this.settings.noise().minY());
        int maxY = Math.min(chunk.maxY(), this.settings.noise().minY() + this.settings.noise().height());

        int cellWidth = NoiseSettings.cellWidth(this.settings.noise());
        int cellHeight = NoiseSettings.cellHeight(this.settings.noise());
        int cellCountXZ = Math.floorDiv(16, cellWidth);

        int minCellY = Math.floorDiv(minY, cellHeight);
        int cellCountY = Math.floorDiv(maxY - minY, cellHeight);

        NoiseChunk noiseChunk = this.getOrCreateNoiseChunk(randomState, chunk);

//
//        for (let cellX = 0; cellX < cellCountXZ; cellX += 1) {
//            for (let cellZ = 0; cellZ < (onlyFirstZ ? 1 : cellCountXZ); cellZ += 1) {
//                let section = chunk.getOrCreateSection(chunk.sectionsCount - 1)
//                for (let cellY = cellCountY - 1; cellY >= 0; cellY -= 1) {
//                    for (let offY = cellHeight - 1; offY >= 0; offY -= 1) {
//                    const blockY = (minCellY + cellY) * cellHeight + offY
//                    const sectionY = blockY & 0xF
//                    const sectionIndex = chunk.getSectionIndex(blockY)
//                        if (chunk.getSectionIndex(section.minBlockY) !== sectionIndex) {
//                            section = chunk.getOrCreateSection(sectionIndex)
//                        }
//                        for (let offX = 0; offX < cellWidth; offX += 1) {
//                        const blockX = minX + cellX * cellWidth + offX
//                        const sectionX = blockX & 0xF
//                            for (let offZ = 0; offZ < (onlyFirstZ ? 1 : cellWidth); offZ += 1) {
//                            const blockZ = minZ + cellZ * cellWidth + offZ
//                            const sectionZ = blockZ & 0xF
//                            const state = noiseChunk.getFinalState(blockX, blockY, blockZ) ?? this.settings.defaultBlock
//                                section.setBlockState(sectionX, sectionY, sectionZ, state)
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }

        for (int cellX = 0; cellX < cellCountXZ; cellX += 1) {
            for (int cellZ = 0; cellZ < (onlyFirstZ ? 1 : cellCountXZ); cellZ += 1) {
                for (int cellY = cellCountY - 1; cellY >= 0; cellY -= 1) {
                    for (int offY = cellHeight - 1; offY >= 0; offY -= 1) {
                        int blockY = (minCellY + cellY) * cellHeight + offY;
                        int sectionY = blockY / Chunk.CHUNK_SECTION_SIZE;

                        for (int offX = 0; offX < cellWidth; offX += 1) {
                            int blockX = chunk.minX() + cellX * cellWidth + offX;
                            int sectionX = blockX & 0xF;

                            for (int offZ = 0; offZ < (onlyFirstZ ? 1 : cellWidth); offZ += 1) {
                                int blockZ = chunk.minZ() + cellZ * cellWidth + offZ;
                                int sectionZ = blockZ & 0xF;

                                Block state = noiseChunk.getFinalState(blockX, blockY, blockZ);
                                if (state == null) {
                                    state = this.settings.defaultBlock();
                                }
                                chunk.setBlock(blockX, blockY, blockZ, state);
                            }
                        }
                    }
                }
            }
        }
    }

    //    public buildSurface(randomState: RandomState, chunk: Chunk, /** @deprecated */ biome: string = 'minecraft:plains') {
//        const noiseChunk = this.getOrCreateNoiseChunk(randomState, chunk)
//        const context = WorldgenContext.create(this.settings.noise.minY, this.settings.noise.height)
//        randomState.surfaceSystem.buildSurface(chunk, noiseChunk, context, () => biome)
//    }
    public void buildSurface(RandomState randomState, TargetChunk chunk, String biome) {
        NoiseChunk noiseChunk = this.getOrCreateNoiseChunk(randomState, chunk);
        VerticalAnchor.WorldgenContext context = VerticalAnchor.WorldgenContext.create(this.settings.noise().minY(), this.settings.noise().height());
        randomState.surfaceSystem.buildSurface(chunk, noiseChunk, context, point -> biome);
    }

    //    public computeBiome(randomState: RandomState, quartX: number, quartY: number, quartZ: number) {
//        return this.biomeSource.getBiome(quartX, quartY, quartZ, randomState.sampler)
//    }
    public NamespaceID computeBiome(RandomState randomState, int quartX, int quartY, int quartZ) {
        return this.biomeSource.getBiome(quartX, quartY, quartZ, randomState.sampler);
    }

    private NoiseChunk getOrCreateNoiseChunk(RandomState randomState, TargetChunk chunk) {
        return this.noiseChunkCache.computeIfAbsent(chunk.index(), ignored -> {
//            const minY = Math.max(chunk.minY, this.settings.noise.minY)
//            const maxY = Math.min(chunk.maxY, this.settings.noise.minY + this.settings.noise.height)
//
//            const cellWidth = NoiseSettings.cellWidth(this.settings.noise)
//            const cellHeight = NoiseSettings.cellHeight(this.settings.noise)
//            const cellCountXZ = Math.floor(16 / cellWidth)
//
//            const minCellY = Math.floor(minY / cellHeight)
//            const cellCountY = Math.floor((maxY - minY) / cellHeight)
//            const minX = ChunkPos.minBlockX(chunk.pos)
//            const minZ = ChunkPos.minBlockZ(chunk.pos)
//
//            return new NoiseChunk(cellCountXZ, cellCountY, minCellY, randomState, minX, minZ, this.settings.noise, this.settings.aquifersEnabled, this.globalFluidPicker)
            int minY = Math.max(chunk.minY(), this.settings.noise().minY());
            int maxY = Math.min(chunk.maxY(), this.settings.noise().minY() + this.settings.noise().height());

            int cellWidth = NoiseSettings.cellWidth(this.settings.noise());
            int cellHeight = NoiseSettings.cellHeight(this.settings.noise());
            int cellCountXZ = Math.floorDiv(Chunk.CHUNK_SECTION_SIZE, cellWidth);

            int minCellY = Math.floorDiv(minY, cellHeight);
            int cellCountY = Math.floorDiv(maxY - minY, cellHeight);
            int minX = chunk.minX();
            int minZ = chunk.minZ();

            return new NoiseChunk(cellCountXZ, cellCountY, minCellY, randomState, minX, minZ, this.settings.noise(), this.settings.aquifersEnabled(), this.globalFluidPicker);
        });
    }

    @Override
    public synchronized void generateChunkData(@NotNull ChunkBatch batch, int chunkX, int chunkZ) {
        TargetChunkImpl chunk = new TargetChunkImpl(batch,
                chunkX, chunkZ,
                dimensionType.getMinY() / Chunk.CHUNK_SECTION_SIZE,
                dimensionType.getMaxY() / Chunk.CHUNK_SECTION_SIZE);
        RandomState randomState = new RandomState(settings, 125);
        fill(randomState, chunk);
    }

    private static class TargetChunkImpl implements TargetChunk {

        private final int chunkX;
        private final int chunkZ;

        private final int minSection;
        private final int maxSection;

        private final ChunkBatch batch;

        private final Int2ObjectMap<Block> blocks = new Int2ObjectOpenHashMap<>();

        public TargetChunkImpl(ChunkBatch batch, int chunkX, int chunkZ, int minSection, int maxSection) {
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
            this.minSection = minSection;
            this.maxSection = maxSection;
            this.batch = batch;
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
            batch.setBlock(x - minX(), y, z - minZ(), block);
        }
    }

    public interface TargetChunk extends Block.Getter, Block.Setter {
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
    }

    @Override
    public @Nullable List<ChunkPopulator> getPopulators() {
        return null;
    }
}
