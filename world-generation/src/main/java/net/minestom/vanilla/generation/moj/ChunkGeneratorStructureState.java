package net.minestom.vanilla.generation.moj;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongObjectPair;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.vanilla.datapack.Datapack;
import net.minestom.vanilla.datapack.DatapackUtils;
import net.minestom.vanilla.datapack.worldgen.StructureSet;
import net.minestom.vanilla.datapack.worldgen.biome.BiomeSource;
import net.minestom.vanilla.datapack.worldgen.Structure;
import net.minestom.vanilla.datapack.worldgen.random.LegacyRandom;
import net.minestom.vanilla.datapack.worldgen.random.WorldgenRandom;
import net.minestom.vanilla.generation.RandomState;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class ChunkGeneratorStructureState {

    private final Datapack datapack;
    private final RandomState randomState;
    private final BiomeSource biomeSource;
    private final long seed;
    private final long ringsSeed;
    private final Map<Structure, List<StructureSet.Placement>> placementsForStructure = new Object2ObjectOpenHashMap<>();
    private final Map<StructureSet.Placement.ConcentricRings, CompletableFuture<LongList>> ringPositions = new Object2ObjectArrayMap();
    private boolean hasGeneratedPositions;
    private final List<StructureSet> structureSets;

    private ChunkGeneratorStructureState(Datapack datapack, RandomState random, BiomeSource biomeSource, long seed, long ringsSeed, List<StructureSet> structureSets) {
        this.datapack = datapack;
        this.randomState = random;
        this.seed = seed;
        this.biomeSource = biomeSource;
        this.ringsSeed = ringsSeed;
        this.structureSets = structureSets;
    }

    public List<StructureSet> possibleStructureSets() {
        return this.structureSets;
    }

    private void generatePositions() {
        Collection<NamespaceID> availableBiomes = this.biomeSource.biomes();

        this.possibleStructureSets().forEach((structureSet) -> {
            boolean structureFound = false;

            for (StructureSet.StructureEntry structureEntry : structureSet.structures()) {
                Structure structure = structureEntry.structure();
                Stream<NamespaceID> structureBiomes = structure.biomes().stream();
                Objects.requireNonNull(availableBiomes);
                if (structureBiomes.anyMatch(availableBiomes::contains)) {
                    this.placementsForStructure
                            .computeIfAbsent(structure, (key) -> new ArrayList<>())
                            .add(structureSet.placement());
                    structureFound = true;
                }
            }

            if (structureFound) {
                StructureSet.Placement placement = structureSet.placement();
                if (placement instanceof StructureSet.Placement.ConcentricRings concentricRingsPlacement) {
                    this.ringPositions.put(concentricRingsPlacement, this.generateRingPositions(structureSet, concentricRingsPlacement));
                }
            }

        });
    }

    private CompletableFuture<LongList> generateRingPositions(StructureSet structureSet, StructureSet.Placement.ConcentricRings concentricRings) {
        if (concentricRings.count() == 0) {
            return CompletableFuture.completedFuture(LongList.of());
        }

        int distance = concentricRings.distance();
        int count = concentricRings.count();
        List<CompletableFuture<Long>> futures = new ArrayList<>(count);
        int spread = concentricRings.spread();
        Set<NamespaceID> preferredBiomes = Set.copyOf(concentricRings.preferredBiomes());
        WorldgenRandom random = new LegacyRandom(this.ringsSeed);
        double angle = random.nextDouble() * Math.PI * 2.0;
        int ring = 0;
        int positionsInCurrentRing = 0;

        for (int i = 0; i < count; ++i) {

            double radius = (double) (4 * distance + distance * ring * 6) + (random.nextDouble() - 0.5) * (double)distance * 2.5;

            int xOffset = (int) Math.round(Math.cos(angle) * radius);
            int yOffset = (int) Math.round(Math.sin(angle) * radius);

            WorldgenRandom forkedRandom = random.fork();

            futures.add(CompletableFuture.supplyAsync(() -> {

                int blockX = sectionToBlockCoord(xOffset) + 8;
                int blockY = sectionToBlockCoord(yOffset) + 8;

                Objects.requireNonNull(preferredBiomes);
                LongObjectPair<NamespaceID> biomeMatch = BiomeSourceLogic.findBiomeHorizontal(this.biomeSource, blockX, 0, blockY, 112, 1, biome -> preferredBiomes.contains(biome), forkedRandom, false, this.randomState.sampler());
                if (biomeMatch != null) {
                    return biomeMatch.firstLong();
                } else {
                    return ChunkUtils.getChunkIndex(xOffset, yOffset);
                }
            }));

            angle += 6.283185307179586 / (double) spread;
            ++positionsInCurrentRing;
            if (positionsInCurrentRing == spread) {
                ++ring;
                positionsInCurrentRing = 0;
                spread += 2 * spread / (ring + 1);
                spread = Math.min(spread, count - i);
                angle += random.nextDouble() * Math.PI * 2.0;
            }
        }

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).thenApply((__) -> {
            LongList result = new LongArrayList(futures.size());
            futures.forEach((future) -> result.add((long)future.join()));
            return result;
        });
    }

    public static int sectionToBlockCoord(int sectionCoord) {
        return sectionCoord << 4;
    }

    public static int blockToSectionCoord(int blockCoord) {
        return blockCoord >> 4;
    }

    public void ensureStructuresGenerated() {
        if (!this.hasGeneratedPositions) {
            this.generatePositions();
            this.hasGeneratedPositions = true;
        }
    }

    public @Nullable LongList getRingPositionsFor(StructureSet.Placement.ConcentricRings concentricRings) {
        this.ensureStructuresGenerated();
        CompletableFuture<LongList> ringPositionsFuture = this.ringPositions.get(concentricRings);
        return ringPositionsFuture != null ? ringPositionsFuture.join() : null;
    }

    public List<StructureSet.Placement> getPlacementsForStructure(Structure structure) {
        this.ensureStructuresGenerated();
        return this.placementsForStructure.getOrDefault(structure, List.of());
    }

    public RandomState randomState() {
        return this.randomState;
    }

    public boolean hasStructureChunkInRange(String structureName, int chunkX, int chunkZ, int range) {
        Optional<StructureSet> structure = DatapackUtils.findStructureSet(datapack, structureName);
        if (structure.isEmpty()) {
            throw new IllegalArgumentException("Structure set " + structureName + " not found");
        }
        StructureSet.Placement placement = structure.get().placement();

        for (int x = chunkX - range; x <= chunkX + range; ++x) {
            for (int z = chunkZ - range; z <= chunkZ + range; ++z) {
                if (StructureLogic.isStructureChunk(placement, this, x, z)) {
                    return true;
                }
            }
        }

        return false;
    }

    public long getSeed() {
        return this.seed;
    }
}
