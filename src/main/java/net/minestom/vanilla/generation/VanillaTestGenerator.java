package net.minestom.vanilla.generation;

import de.articdive.jnoise.JNoise;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.ChunkGenerator;
import net.minestom.server.instance.ChunkPopulator;
import net.minestom.server.instance.batch.ChunkBatch;
import net.minestom.server.instance.block.Block;
import net.minestom.server.world.biomes.Biome;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class VanillaTestGenerator extends ChunkGenerator  {

    private Random random = new Random();
    private JNoise noise = JNoise.newBuilder().openSimplex().build();

    @Override
    public void generateChunkData(ChunkBatch batch, int chunkX, int chunkZ) {
        for (byte x = 0; x < Chunk.CHUNK_SIZE_X; x++) {
            for (byte z = 0; z < Chunk.CHUNK_SIZE_Z; z++) {
                int posX = chunkX*16+x;
                int posZ = chunkZ*16+z;
                double heightDelta = noise.getNoise(posX/16.0, posZ/16.0);
                int height = (int) (64 - heightDelta*16);

                batch.setBlock(posX, 0, posZ, Block.BEDROCK);
                for (int level = 1; level < height; level++) {
                    batch.setBlock(posX, level, posZ, Block.STONE);
                }
                for (int level = height; level < 64; level++) {
                    batch.setBlock(posX, level, posZ, Block.WATER);
                }
                for (int level = 64; level < height; level++) {
                    batch.setBlock(posX, level, posZ, Block.DIRT);
                }

                if(height >= 64) {
                    batch.setBlock(posX, height, posZ, Block.GRASS_BLOCK);

                    if(x >= 2 && z >= 2 && x < Chunk.CHUNK_SIZE_X-2 && z < Chunk.CHUNK_SIZE_X-2) { // avoid chunk borders
                        if(random.nextDouble() < 0.02) {
                            spawnTree(batch, posX, height+1, posZ);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void fillBiomes(Biome[] biomes, int chunkX, int chunkZ) {
        Arrays.fill(biomes, Biome.PLAINS);
    }

    private void spawnTree(ChunkBatch batch, int trunkX, int trunkBottomY, int trunkZ) {
        for (int i = 0; i < 2; i++) {
            batch.setBlock(trunkX+1, trunkBottomY+3+i, trunkZ, Block.OAK_LEAVES);
            batch.setBlock(trunkX-1, trunkBottomY+3+i, trunkZ, Block.OAK_LEAVES);
            batch.setBlock(trunkX, trunkBottomY+3+i, trunkZ+1, Block.OAK_LEAVES);
            batch.setBlock(trunkX, trunkBottomY+3+i, trunkZ-1, Block.OAK_LEAVES);

            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    batch.setBlock(trunkX+x, trunkBottomY+2+i, trunkZ-z, Block.OAK_LEAVES);
                }
            }
        }

        batch.setBlock(trunkX, trunkBottomY, trunkZ, Block.OAK_LOG);
        batch.setBlock(trunkX, trunkBottomY+1, trunkZ, Block.OAK_LOG);
        batch.setBlock(trunkX, trunkBottomY+2, trunkZ, Block.OAK_LOG);
        batch.setBlock(trunkX, trunkBottomY+3, trunkZ, Block.OAK_LOG);
        batch.setBlock(trunkX, trunkBottomY+4, trunkZ, Block.OAK_LEAVES);
    }

    @Override
    public List<ChunkPopulator> getPopulators() {
        return null;
    }
}
