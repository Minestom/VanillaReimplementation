package net.minestom.vanilla.generation;

import de.articdive.jnoise.JNoise;
import net.minestom.server.instance.Biome;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.ChunkGenerator;
import net.minestom.server.instance.ChunkPopulator;
import net.minestom.server.instance.batch.ChunkBatch;
import net.minestom.server.instance.block.Block;


import java.util.Arrays;
import java.util.List;
import java.util.Random;


public class VanillaGenerator extends ChunkGenerator  {
	
	int seed = (int) (47758);
	private Random random = new Random(seed);
	double seedProduct = random.nextDouble();
    private JNoise noise = JNoise.newBuilder().openSimplex().build();
    @Override
    public void generateChunkData(ChunkBatch batch, int chunkX, int chunkZ) {
    	for (byte x = 0; x < Chunk.CHUNK_SIZE_X; x++) {
            for (byte z = 0; z < Chunk.CHUNK_SIZE_Z; z++) {

            	
                int posX = (chunkX*16+x);
                int posZ = (chunkZ*16+z);
                
            	
                int height = getHeight(posX, posZ);
                
                int crust = (int) (height - 4);
                
                
                
                for (int level = 0; level < crust; level++) {
                    batch.setBlock(posX, level, posZ, Block.STONE);
                }
                
                for (int level = crust; level <= height; level++) {
                    batch.setBlock(posX, level, posZ, Block.DIRT);
                }
                
                batch.setBlock(posX, height, posZ, Block.GRASS_BLOCK);
                
                if (bushCheck(batch, posX/2, posZ/2) == true) {
                	batch.setBlock(posX, height + 1, posZ, Block.GRASS);
                }
            }
        }
    }

    @Override
    public void fillBiomes(Biome[] biomes, int chunkX, int chunkZ) {
        Arrays.fill(biomes, Biome.PLAINS);
    }

    private boolean bushCheck(ChunkBatch batch, int posX, int posZ) {
    	if (2 * noise.getNoise(posX, posZ) + 1 * noise.getNoise(posX/2, posZ/2) > 0.83) {
    		return true;
    	} else {
    		return false;
    	}
    }
    
    private int getHeight(int posX, int posZ) {
        double heightDelta = getNoiseValue(posX, posZ, 16);
        double heightAlpha = getNoiseValue(posX, posZ, 256);
        int height = (int) (2 * Math.abs(heightDelta) + 16 * Math.abs(heightAlpha) + 80);
        return height;
    }
    
    private double getNoiseValue(int posX, int posZ, double NoiseID) {
    	return noise.getNoise((seed + posX) / NoiseID, (seed + posZ) / NoiseID);
    }
    
    @Override
    public List<ChunkPopulator> getPopulators() {
        return null;
    }
}
