package net.minestom.vanilla.generation.moj;

import it.unimi.dsi.fastutil.longs.LongObjectPair;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.vanilla.datapack.worldgen.biome.BiomeSource;
import net.minestom.vanilla.datapack.worldgen.biome.Climate;
import net.minestom.vanilla.datapack.worldgen.random.WorldgenRandom;

import java.util.function.Predicate;

import static net.minestom.vanilla.generation.Util.blockFromQuart;
import static net.minestom.vanilla.generation.Util.quartFromBlock;

public class BiomeSourceLogic {
    public static LongObjectPair<NamespaceID> findBiomeHorizontal(BiomeSource source, int blockX, int blockY, int blockZ, int radius, int step, Predicate<NamespaceID> biomePredicate, WorldgenRandom random, boolean inclusive, Climate.Sampler sampler) {
        int quartX = quartFromBlock(blockX);
        int quartY = quartFromBlock(blockY);
        int quartZ = quartFromBlock(blockZ);
        int quartStart = quartFromBlock(blockY);
        LongObjectPair<NamespaceID> bestBiome = null;
        int count = 0;
        int start = inclusive ? 0 : quartZ;

        for (int z = start; z <= quartZ; z += step) {
            for (int x = -z; x <= z; x += step) {
                boolean onEdgeX = Math.abs(x) == z;

                for (int y = -z; y <= z; y += step) {
                    if (inclusive) {
                        boolean onEdgeY = Math.abs(y) == z;
                        if (!onEdgeY && !onEdgeX) {
                            continue;
                        }
                    }

                    int worldX = quartX + x;
                    int worldZ = quartY + y;
                    NamespaceID biome = source.getBiome(worldX, quartStart, worldZ, sampler);
                    if (biomePredicate.test(biome)) {
                        if (bestBiome == null || random.nextInt(count + 1) == 0) {
                            long chunkIndex = ChunkUtils.getChunkIndex(blockFromQuart(worldX), blockFromQuart(worldZ));
                            if (inclusive) {
                                return LongObjectPair.of(chunkIndex, biome);
                            }

                            bestBiome = LongObjectPair.of(chunkIndex, biome);
                        }

                        ++count;
                    }
                }
            }
        }

        return bestBiome;
    }
}
