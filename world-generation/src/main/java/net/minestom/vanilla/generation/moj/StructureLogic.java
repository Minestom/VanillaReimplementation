package net.minestom.vanilla.generation.moj;

import it.unimi.dsi.fastutil.longs.LongList;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.vanilla.datapack.worldgen.StructureSet;
import net.minestom.vanilla.datapack.worldgen.random.WorldgenRandom;

public class StructureLogic {
    public static boolean isStructureChunk(StructureSet.Placement placement, ChunkGeneratorStructureState state, int chunkX, int chunkZ) {
        if (!isPlacementChunk(placement, state, chunkX, chunkZ)) {
            return false;
        } else if (placement.frequency() < 1.0F && !shouldGenerate(placement.frequencyReductionMethod(), state.getSeed(), placement.salt(), chunkX, chunkZ, placement.frequency())) {
            return false;
        } else {
            return placement.exclusionZone() == null || !isPlacementForbidden(placement.exclusionZone(), state, chunkX, chunkZ);
        }
    }

    public static boolean isPlacementForbidden(StructureSet.ExclusionZone exclusionZone, ChunkGeneratorStructureState state, int chunkX, int chunkZ) {
        return state.hasStructureChunkInRange(exclusionZone.otherSet(), chunkX, chunkZ, exclusionZone.chunkCount());
    }

    public static boolean shouldGenerate(StructureSet.FrequencyReductionMethod frequencyReductionMethod, long seed, int x, int z, int salt, float frequency) {
        return switch (frequencyReductionMethod) {
            case default_ -> probabilityReducer(seed, x, z, salt, frequency);
            case legacy_type_1 -> legacyPillagerOutpostReducer(seed, x, z, salt, frequency);
            case legacy_type_2 -> legacyArbitrarySaltProbabilityReducer(seed, x, z, salt, frequency);
            case legacy_type_3 -> legacyProbabilityReducerWithDouble(seed, x, z, salt, frequency);
        };
    }

    public static boolean probabilityReducer(long seed, int x, int z, int salt, float frequency) {
        WorldgenRandom random = WorldgenRandom.legacy(0).withLargeFeatureWithSalt(seed, x, z, salt);
        return random.nextFloat() < frequency;
    }

    public static boolean legacyProbabilityReducerWithDouble(long seed, int x, int z, int salt, float frequency) {
        WorldgenRandom random = WorldgenRandom.legacy(0).withLargeFeatureSeed(seed, z, salt);
        return random.nextDouble() < (double) frequency;
    }

    public static boolean legacyArbitrarySaltProbabilityReducer(long seed, int x, int z, int salt, float frequency) {
        WorldgenRandom random = WorldgenRandom.legacy(0).withLargeFeatureWithSalt(seed, z, salt, 10387320);
        return random.nextFloat() < frequency;
    }

    public static boolean legacyPillagerOutpostReducer(long seed, int x, int z, int salt, float frequency) {
        int xCoord = x >> 4;
        int zCoord = z >> 4;
        WorldgenRandom random = WorldgenRandom.legacy((long) (xCoord ^ zCoord << 4) ^ seed);
        random.nextInt();
        return random.nextInt((int) (1.0F / frequency)) == 0;
    }

    static boolean isPlacementChunk(StructureSet.Placement placement, ChunkGeneratorStructureState state, int chunkX, int chunkZ) {
        return switch (placement) {
            case StructureSet.Placement.ConcentricRings rings -> isPlacementChunk(rings, state, chunkX, chunkZ);
            case StructureSet.Placement.RandomSpread spread -> isPlacementChunk(spread, state, chunkX, chunkZ);
        };
    }

    public static boolean isPlacementChunk(StructureSet.Placement.ConcentricRings rings, ChunkGeneratorStructureState state, int chunkX, int chunkZ) {
        LongList ringPositions = state.getRingPositionsFor(rings);
        return ringPositions != null && ringPositions.contains(ChunkUtils.getChunkIndex(chunkX, chunkZ));
    }

    public static long getPotentialStructureChunk(StructureSet.Placement.RandomSpread spread, long seed, int chunkX, int chunkZ) {
        int gridX = Math.floorDiv(chunkX, spread.spacing());
        int gridZ = Math.floorDiv(chunkZ, spread.spacing());

        WorldgenRandom random = WorldgenRandom.legacy(0L).withLargeFeatureWithSalt(seed, gridX, gridZ, spread.salt());

        int spacingMinusSeparation = spread.spacing() - spread.separation();
        int xOffset = evaluateSpreadType(spread.spreadType(), random, spacingMinusSeparation);
        int zOffset = evaluateSpreadType(spread.spreadType(), random, spacingMinusSeparation);

        return ChunkUtils.getChunkIndex(gridX * spread.spacing() + xOffset, gridZ * spread.spacing() + zOffset);
    }

    private static int evaluateSpreadType(StructureSet.Placement.RandomSpread.SpreadType spreadType, WorldgenRandom random, int max) {
        return switch (spreadType) {
            case linear -> random.nextInt(max);
            case triangular -> (random.nextInt(max) + random.nextInt(max)) / 2;
        };
    }

    public static boolean isPlacementChunk(StructureSet.Placement.RandomSpread spread, ChunkGeneratorStructureState state, int chunkX, int chunkZ) {
        long potentialChunk = getPotentialStructureChunk(spread, state.getSeed(), chunkX, chunkZ);
        int x = ChunkUtils.getChunkCoordX(potentialChunk);
        int z = ChunkUtils.getChunkCoordZ(potentialChunk);
        return x == chunkX && z == chunkZ;
    }
}
