package net.minestom.vanilla.generation;

import de.articdive.jnoise.JNoise;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.ChunkGenerator;
import net.minestom.server.instance.ChunkPopulator;
import net.minestom.server.instance.batch.ChunkBatch;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.generator.GenerationUnit;
import net.minestom.server.instance.generator.Generator;
import net.minestom.server.world.biomes.Biome;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class VanillaTestGenerator implements Generator {

    private final JNoise noise = JNoise.newBuilder().fastSimplex().setFrequency(1.0 / 16.0).build();
    private final JNoise treeNoise = JNoise.newBuilder().white().setFrequency(999999.0).build();

    private synchronized double noise(JNoise noise, double x, double z) {
        return noise.getNoise(x, 0, z);
    }

    @Override
    public void generate(@NotNull GenerationUnit unit) {
        var modifier = unit.modifier();
        modifier.fillBiome(Biome.PLAINS);

        Point start = unit.absoluteStart();
        Point end = unit.absoluteEnd();

        for (int x = start.blockX(); x < end.blockX(); x++) {
            for (int z = start.blockZ(); z < end.blockX(); z++) {

                double heightDelta = noise(noise, x, z);
                int height = (int) (64 - heightDelta*16);
                Point bottom = new Vec(x, 0, z);
                Point stoneBoundary = new Vec(x, height, z);
                Point dirtBoundary = stoneBoundary.withY(y -> y + 5);
                Point grassLayer = dirtBoundary.withY(y -> y + 1);

                modifier.fill(bottom, stoneBoundary, Block.STONE);
                modifier.fill(stoneBoundary, dirtBoundary, Block.DIRT);
                modifier.fill(dirtBoundary, grassLayer, Block.GRASS_BLOCK);

                if (height < 64) {
                    // Too low for a tree
                    continue;
                }

                if (noise(treeNoise, x, z) > 0.9) {
                    unit.fork(setter -> spawnTree(setter, grassLayer.withY(y -> y + 1)));
                }
            }
        }
    }

    private void spawnTree(Block.Setter setter, Point pos) {
        int trunkX = pos.blockX();
        int trunkBottomY = pos.blockY();
        int trunkZ = pos.blockZ();

        for (int i = 0; i < 2; i++) {
            setter.setBlock(trunkX+1, trunkBottomY+3+i, trunkZ, Block.OAK_LEAVES);
            setter.setBlock(trunkX-1, trunkBottomY+3+i, trunkZ, Block.OAK_LEAVES);
            setter.setBlock(trunkX, trunkBottomY+3+i, trunkZ+1, Block.OAK_LEAVES);
            setter.setBlock(trunkX, trunkBottomY+3+i, trunkZ-1, Block.OAK_LEAVES);

            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    setter.setBlock(trunkX+x, trunkBottomY+2+i, trunkZ-z, Block.OAK_LEAVES);
                }
            }
        }

        setter.setBlock(trunkX, trunkBottomY, trunkZ, Block.OAK_LOG);
        setter.setBlock(trunkX, trunkBottomY+1, trunkZ, Block.OAK_LOG);
        setter.setBlock(trunkX, trunkBottomY+2, trunkZ, Block.OAK_LOG);
        setter.setBlock(trunkX, trunkBottomY+3, trunkZ, Block.OAK_LOG);
        setter.setBlock(trunkX, trunkBottomY+4, trunkZ, Block.OAK_LEAVES);
    }
}
