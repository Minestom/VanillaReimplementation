package net.minestom.vanilla.generation;


import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.ChunkGenerator;
import net.minestom.server.instance.ChunkPopulator;
import net.minestom.server.instance.batch.ChunkBatch;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.world.DimensionType;
import net.minestom.vanilla.datapack.worldgen.DensityFunction;
import net.minestom.vanilla.datapack.worldgen.NoiseSettings;
import net.minestom.vanilla.datapack.worldgen.biome.BiomeSource;
import net.minestom.vanilla.datapack.worldgen.biome.Climate;
import net.minestom.vanilla.dimensions.VanillaDimensionTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
@SuppressWarnings("UnstableApiUsage")
public final class NoiseChunkGenerator implements ChunkGenerator {
    private final NoiseSettings settings;
    private final Aquifer.FluidPicker fluidPicker;
    private final BiomeSource biomeSource;

    public NoiseChunkGenerator(BiomeSource biomeSource, NoiseSettings settings) {
        this.biomeSource = biomeSource;
        this.settings = settings;
        this.fluidPicker = createFluidPicker(settings);
    }

    private static Aquifer.FluidPicker createFluidPicker(NoiseSettings settings) {
        Aquifer.FluidStatus lavaStatus = new Aquifer.FluidStatus(-54, Block.LAVA);
        int seaLevel = settings.sea_level();
        Aquifer.FluidStatus waterStatus = new Aquifer.FluidStatus(seaLevel, settings.default_fluid().toMinestom());
        // TODO: Get dimension type from datapack
        DimensionType dimensionType = VanillaDimensionTypes.OVERWORLD;
        Aquifer.FluidStatus belowMinYStatus = new Aquifer.FluidStatus(dimensionType.getMinY() * 2, Block.AIR);
        return (pos, fluidLevel, noise) -> fluidLevel < Math.min(-54, seaLevel) ? lavaStatus : waterStatus;
    }

    public CompletableFuture<TargetChunk> createBiomes(Executor executor, RandomState randomState, StructureManager structureManager, TargetChunk targetChunk) {
        return CompletableFuture.supplyAsync(() -> {
            NoiseChunk chunk = this.getOrCreateNoiseChunk(targetChunk, structureManager, randomState);
            this.doCreateBiomes(randomState, structureManager, targetChunk);
            return targetChunk;
        }, executor);
    }

    private void doCreateBiomes(RandomState randomState, StructureManager structureManager, TargetChunk targetChunk) {
        NoiseChunk noiseChunk = getOrCreateNoiseChunk(targetChunk, structureManager, randomState);
        Climate.Sampler sampler = Climate.Sampler.fromRouter(randomState.router());
        fillBiomesFromNoise(targetChunk, this.biomeSource, sampler);
    }

    public void fillBiomesFromNoise(TargetChunk chunk, BiomeSource biomeSource, Climate.Sampler sampler) {
        int quartX = Util.quartFromBlock(chunk.minX());
        int quartZ = Util.quartFromBlock(chunk.minZ());

        for (int x = chunk.minX(); x < chunk.maxX(); x += 4) {
            for (int y = chunk.minY(); y < chunk.maxY(); y += 4) {
                for (int z = chunk.minZ(); z < chunk.maxZ(); z += 4) {
                    // TODO: Get biome from datapack
                    // chunk.setBiome(x, y, z, biomeSource.getBiome(x, y, z, sampler));
                }
            }
        }
    }

    private final Map<Long, NoiseChunk> noiseChunks = new ConcurrentHashMap<>();

    private NoiseChunk createNoiseChunk(TargetChunk targetChunk, StructureManager structureManager, RandomState randomState) {
        return NoiseChunk.forChunk(targetChunk, randomState, new DensityFunction.Beardifier(), this.settings, this.fluidPicker);
    }

    private NoiseChunk getOrCreateNoiseChunk(TargetChunk targetChunk, StructureManager structureManager, RandomState randomState) {
        return this.noiseChunks.computeIfAbsent(targetChunk.chunk(), key -> this.createNoiseChunk(targetChunk, structureManager, randomState));
    }

    public NoiseSettings generatorSettings() {
        return this.settings;
    }

    public CompletableFuture<Void> fillFromNoise(Executor executor, RandomState randomState, StructureManager structureManager, TargetChunk targetChunk) {
        NoiseSettings.Noise noise = NoiseSettingsLogic.clampToHeight(this.settings.noise(), targetChunk.minY(), targetChunk.maxY());
        int cellHeight = NoiseSettingsLogic.getCellHeight(noise);
        int minY = noise.min_y();
        int height = noise.height();
        int startY = Math.floorDiv(minY, cellHeight);
        int endY = Math.floorDiv(height, cellHeight);

        if (endY <= 0) {
            return CompletableFuture.completedFuture(null);
        }

        int yPos = endY * cellHeight - 1 + minY;
        int sectionStart = ChunkUtils.getChunkCoordinate(yPos) - targetChunk.minSection();
        int sectionMin = ChunkUtils.getChunkCoordinate(minY) - targetChunk.minSection();

        return CompletableFuture.runAsync(() -> this.doFill(structureManager, randomState, targetChunk, startY, endY), executor);
    }

    private void doFill(StructureManager structureManager, RandomState randomState, TargetChunk targetChunk, int startY, int endY) {
        NoiseChunk noiseChunk = getOrCreateNoiseChunk(targetChunk, structureManager, randomState);
        long chunkId = targetChunk.chunk();
        int chunkMinX = targetChunk.minX();
        int chunkMinZ = targetChunk.minZ();
        Aquifer aquifer = noiseChunk.aquifer();
        int cellWidth = noiseChunk.cellWidth();
        int cellHeight = noiseChunk.cellHeight();
        int xMax = 16 / cellWidth;
        int yMax = 16 / cellWidth;

        for (int x = 0; x < xMax; x++) {
            for (int y = 0; y < yMax; y++) {
                int currentSection = targetChunk.maxSection() - targetChunk.minSection() - 1;

                for (int z = endY - 1; z >= 0; z--) {
                    for (int sectionY = cellHeight - 1; sectionY >= 0; sectionY--) {
                        int blockY = (startY + z) * cellHeight + sectionY;
                        int yCoord = blockY & 15;
                        int sectionCoord = ChunkUtils.getChunkCoordinate(blockY) - targetChunk.minSection();

                        if (currentSection != sectionCoord) {
                            currentSection = sectionCoord;
                        }

                        double yRatio = (double) sectionY / (double) cellHeight;

                        for (int xOffset = 0; xOffset < cellWidth; xOffset++) {
                            int blockX = chunkMinX + x * cellWidth + xOffset;
                            int xCoord = blockX & 15;
                            double xRatio = (double) xOffset / (double) cellWidth;

                            for (int zOffset = 0; zOffset < cellWidth; zOffset++) {
                                int blockZ = chunkMinZ + y * cellWidth + zOffset;
                                int zCoord = blockZ & 15;
                                double zRatio = (double) zOffset / (double) cellWidth;
                                Block block = noiseChunk.materialRule().compute(DensityFunction.context(blockX, blockY, blockZ));

                                if (block == null) {
                                    block = this.settings.default_block().toMinestom();
                                }

                                if (!Block.AIR.compare(block)) {
                                    targetChunk.setBlock(xCoord, yCoord, zCoord, block);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void generateChunkData(@NotNull ChunkBatch chunkBatch, int chunkX, int chunkZ) {
        // TODO: Get dimension type from datapack
        DimensionType dimensionType = VanillaDimensionTypes.OVERWORLD;
        TargetChunk chunk = TargetChunk.from(
                chunkX, chunkZ,
                dimensionType.getMinY() / Chunk.CHUNK_SECTION_SIZE,
                dimensionType.getMaxY() / Chunk.CHUNK_SECTION_SIZE);
        RandomState randomState = new RandomState(settings, 125);
        fillFromNoise(ForkJoinPool.commonPool(), randomState, null, chunk).join();
    }

    @Override
    public @Nullable List<ChunkPopulator> getPopulators() {
        return List.of();
    }
}
