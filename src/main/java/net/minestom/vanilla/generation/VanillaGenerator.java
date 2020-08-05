package net.minestom.vanilla.generation;

import de.articdive.jnoise.JNoise;
import net.minestom.server.instance.Biome;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.ChunkGenerator;
import net.minestom.server.instance.ChunkPopulator;
import net.minestom.server.instance.batch.ChunkBatch;
import net.minestom.server.instance.block.Block;

import java.lang.System.Logger.Level;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;


public class VanillaGenerator extends ChunkGenerator  {
	
	int seed = (int) (12345);
	private Random random = new Random(seed);
	double seedProduct = random.nextDouble();
    private JNoise TerrainNoise = JNoise.newBuilder().combined().setSeed((int) (seed / (random.nextDouble() + 0.01)))
    		.addNoise(JNoise.newBuilder().openSimplex().setSeed((int) (seed / (random.nextDouble() + 0.01))).build())
    		.build();
    private JNoise VegetationNoise = JNoise.newBuilder().combined().setSeed((int) (seed / (random.nextDouble() + 0.01)))
    		.addNoise(JNoise.newBuilder().openSimplex().setSeed((int) (seed / (random.nextDouble() + 0.01))).build())
    		.build();
    private JNoise AridNoise = JNoise.newBuilder().combined().setSeed((int) (seed / (random.nextDouble() + 0.01)))
    		.addNoise(JNoise.newBuilder().openSimplex().setSeed((int) (seed / (random.nextDouble() + 0.01))).build())
    		.build();
    
    
    
    private Biome[] biomes = {
			Biome.DEEP_FROZEN_OCEAN,
			Biome.FROZEN_OCEAN,
			Biome.DEEP_COLD_OCEAN,
			Biome.COLD_OCEAN,
			Biome.DEEP_OCEAN,
			Biome.LUKEWARM_OCEAN,
			Biome.DEEP_LUKEWARM_OCEAN,
			Biome.WARM_OCEAN,
			Biome.DEEP_WARM_OCEAN,
			Biome.FROZEN_OCEAN,
			Biome.SNOWY_BEACH,
			Biome.STONE_SHORE,
			Biome.MUSHROOM_FIELD_SHORE,
			Biome.OCEAN,
			Biome.BEACH,
			Biome.SWAMP,
			Biome.MODIFIED_JUNGLE_EDGE,
			Biome.JUNGLE_EDGE,
			Biome.ICE_SPIKES,
			Biome.SNOWY_TAIGA,
			Biome.TAIGA,
			Biome.MUSHROOM_FIELDS,
			Biome.BEACH,
			Biome.RIVER,
			Biome.MODIFIED_JUNGLE_EDGE,
			Biome.BAMBOO_JUNGLE,
			Biome.JUNGLE,
			Biome.FROZEN_RIVER,
			Biome.GIANT_SPRUCE_TAIGA,
			Biome.SNOWY_TAIGA,
			Biome.PLAINS,
			Biome.RIVER,
			Biome.PLAINS,
			Biome.SUNFLOWER_PLAINS,
			Biome.SAVANNA,
			Biome.MODIFIED_JUNGLE,
			Biome.FROZEN_RIVER,
			Biome.GIANT_TREE_TAIGA,
			Biome.DARK_FOREST,
			Biome.FOREST,
			Biome.PLAINS,
			Biome.FOREST,
			Biome.DESERT_LAKES,
			Biome.BAMBOO_JUNGLE_HILLS,
			Biome.DESERT,
			Biome.TAIGA_HILLS,
			Biome.FLOWER_FOREST,
			Biome.DARK_FOREST_HILLS,
			Biome.DARK_FOREST,
			Biome.FOREST,
			Biome.BIRCH_FOREST,
			Biome.SHATTERED_SAVANNA,
			Biome.JUNGLE_HILLS,
			Biome.DESERT_HILLS,
			Biome.SNOWY_TAIGA_HILLS,
			Biome.TAIGA_HILLS,
			Biome.GIANT_SPRUCE_TAIGA_HILLS,
			Biome.FLOWER_FOREST,
			Biome.FOREST,
			Biome.TALL_BIRCH_FOREST,
			Biome.WOODED_HILLS,
			Biome.SWAMP_HILLS,
			Biome.SHATTERED_SAVANNA_PLATEAU,
			Biome.TAIGA_MOUNTAINS,
			Biome.GIANT_TREE_TAIGA_HILLS,
			Biome.TALL_BIRCH_HILLS,
			Biome.MODIFIED_GRAVELLY_MOUNTAINS,
			Biome.MOUNTAIN_EDGE,
			Biome.BIRCH_FOREST_HILLS,
			Biome.ERODED_BADLANDS,
			Biome.BADLANDS,
			Biome.SAVANNA_PLATEAU,
			Biome.SNOWY_TAIGA_MOUNTAINS,
			Biome.SNOWY_MOUNTAINS,
			Biome.GRAVELLY_MOUNTAINS,
			Biome.MOUNTAINS,
			Biome.WOODED_MOUNTAINS,
			Biome.MODIFIED_BADLANDS_PLATEAU,
			Biome.WOODED_BADLANDS_PLATEAU,
			Biome.MODIFIED_WOODED_BADLANDS_PLATEAU,
			Biome.BADLANDS_PLATEAU
			};
    private Block[] biomeblocks = {
			Block.BLUE_ICE,// Biome.DEEP_FROZEN_OCEAN,
			Block.FROSTED_ICE,// Biome.FROZEN_OCEAN,
			Block.WATER,// Biome.DEEP_COLD_OCEAN,
			Block.WATER,// Biome.COLD_OCEAN,
			Block.WATER,// Biome.DEEP_OCEAN,
			Block.WATER,// Biome.LUKEWARM_OCEAN,
			Block.WATER,// Biome.DEEP_LUKEWARM_OCEAN,
			Block.WATER,// Biome.WARM_OCEAN,
			Block.WATER,// Biome.DEEP_WARM_OCEAN,
			Block.FROSTED_ICE,// Biome.FROZEN_OCEAN,
			Block.SNOW_BLOCK,// Biome.SNOWY_BEACH,
			Block.GRAVEL,// Biome.STONE_SHORE,
			Block.GRASS_BLOCK,// Biome.MUSHROOM_FIELD_SHORE,
			Block.WATER,// Biome.OCEAN,
			Block.SAND,// Biome.BEACH,
			Block.GRASS_BLOCK,// Biome.SWAMP,
			Block.GRASS_BLOCK,// Biome.MODIFIED_JUNGLE_EDGE,
			Block.GRASS_BLOCK,// Biome.JUNGLE_EDGE,
			Block.FROSTED_ICE,// Biome.ICE_SPIKES,
			Block.GRASS_BLOCK,// Biome.SNOWY_TAIGA,
			Block.GRASS_BLOCK,// Biome.TAIGA,
			Block.GRASS_BLOCK,// Biome.MUSHROOM_FIELDS,
			Block.SAND,// Biome.BEACH,
			Block.WATER,// Biome.RIVER,
			Block.GRASS_BLOCK,// Biome.MODIFIED_JUNGLE_EDGE,
			Block.GRASS_BLOCK,// Biome.BAMBOO_JUNGLE,
			Block.GRASS_BLOCK,// Biome.JUNGLE,
			Block.BLUE_ICE,// Biome.FROZEN_RIVER,
			Block.GRASS_BLOCK,// Biome.GIANT_SPRUCE_TAIGA,
			Block.GRASS_BLOCK,// Biome.SNOWY_TAIGA,
			Block.GRASS_BLOCK,// Biome.PLAINS,
			Block.WATER,// Biome.RIVER,
			Block.GRASS_BLOCK,// Biome.PLAINS,
			Block.GRASS_BLOCK,// Biome.SUNFLOWER_PLAINS,
			Block.SAND,// Biome.SAVANNA,
			Block.GRASS_BLOCK,// Biome.MODIFIED_JUNGLE,
			Block.ICE,// Biome.FROZEN_RIVER,
			Block.GRASS_BLOCK,// Biome.GIANT_TREE_TAIGA,
			Block.GRASS_BLOCK,// Biome.DARK_FOREST,
			Block.GRASS_BLOCK,// Biome.FOREST,
			Block.GRASS_BLOCK,// Biome.PLAINS,
			Block.GRASS_BLOCK,// Biome.FOREST,
			Block.WATER,// Biome.DESERT_LAKES,
			Block.GRASS_BLOCK,// Biome.BAMBOO_JUNGLE_HILLS,
			Block.SAND,// Biome.DESERT,
			Block.GRASS_BLOCK,// Biome.TAIGA_HILLS,
			Block.GRASS_BLOCK,// Biome.FLOWER_FOREST,
			Block.GRASS_BLOCK,// Biome.DARK_FOREST_HILLS,
			Block.GRASS_BLOCK,// Biome.DARK_FOREST,
			Block.GRASS_BLOCK,// Biome.FOREST,
			Block.GRASS_BLOCK,// Biome.BIRCH_FOREST,
			Block.GRASS_BLOCK,// Biome.SHATTERED_SAVANNA,
			Block.GRASS_BLOCK,// Biome.JUNGLE_HILLS,
			Block.SAND,// Biome.DESERT_HILLS,
			Block.GRASS_BLOCK,// Biome.SNOWY_TAIGA_HILLS,
			Block.GRASS_BLOCK,// Biome.TAIGA_HILLS,
			Block.GRASS_BLOCK,// Biome.GIANT_SPRUCE_TAIGA_HILLS,
			Block.GRASS_BLOCK,// Biome.FLOWER_FOREST,
			Block.GRASS_BLOCK,// Biome.FOREST,
			Block.GRASS_BLOCK,// Biome.TALL_BIRCH_FOREST,
			Block.GRASS_BLOCK,// Biome.WOODED_HILLS,
			Block.GRASS_BLOCK,// Biome.SWAMP_HILLS,
			Block.SAND,// Biome.SHATTERED_SAVANNA_PLATEAU,
			Block.SNOW_BLOCK,// Biome.TAIGA_MOUNTAINS,
			Block.SNOW_BLOCK,// Biome.GIANT_TREE_TAIGA_HILLS,
			Block.BIRCH_PLANKS,// Biome.TALL_BIRCH_HILLS,
			Block.STONE,// Biome.MODIFIED_GRAVELLY_MOUNTAINS,
			Block.STONE,// Biome.MOUNTAIN_EDGE,
			Block.GRASS_BLOCK,// Biome.BIRCH_FOREST_HILLS,
			Block.SANDSTONE,// Biome.ERODED_BADLANDS,
			Block.SANDSTONE_SLAB,// Biome.BADLANDS,
			Block.SAND,// Biome.SAVANNA_PLATEAU,
			Block.SNOW_BLOCK,// Biome.SNOWY_TAIGA_MOUNTAINS,
			Block.SNOW_BLOCK,// Biome.SNOWY_MOUNTAINS,
			Block.STONE,// Biome.GRAVELLY_MOUNTAINS,
			Block.STONE,// Biome.MOUNTAINS,
			Block.STONE,// Biome.WOODED_MOUNTAINS,
			Block.CHISELED_RED_SANDSTONE,// Biome.MODIFIED_BADLANDS_PLATEAU,
			Block.CUT_RED_SANDSTONE,// Biome.WOODED_BADLANDS_PLATEAU,
			Block.RED_SANDSTONE,// Biome.MODIFIED_WOODED_BADLANDS_PLATEAU,
			Block.SMOOTH_RED_SANDSTONE,// Biome.BADLANDS_PLATEAU
			};
    
    
    @Override
    public void generateChunkData(ChunkBatch batch, int chunkX, int chunkZ) {
       	int[][] HeightMap = {
    				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
    				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
    				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
    				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
    				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
    				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
    				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
    				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
    				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
    				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
    				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
    				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
    				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
    				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
    				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
    				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}
    				};
       	int[][] RiverMap = HeightMap;
       	// int[][] RiverMap = HeightMap;
       	double TotalHeight = 0;
    	for (byte x = 0; x < Chunk.CHUNK_SIZE_X; x++) {
            for (byte z = 0; z < Chunk.CHUNK_SIZE_Z; z++) {
            	double posX = (chunkX*16+x);
                double posZ = (chunkZ*16+z);
                double longheightdelta = Math.abs(90 * Math.pow(TerrainNoise.getNoise(posX/128, posZ/128), 2));
                longheightdelta *= (1 - Math.pow(Math.abs(Math.abs(AridNoise.getNoise(posX/64, posZ/64)) - 1), 5));
            	HeightMap[x][z] = (int) longheightdelta;
            	TotalHeight += longheightdelta;
            }
        }
    	double AverageHeight = (double) TotalHeight/256;
    	double terrainID = (double) AverageHeight / 10;
    	double[] biomespec = getBiome(chunkX, chunkZ, terrainID);
    	int biomeselector = (int) Math.floor(biomespec[0]);
    	System.out.println(chunkX + " " + chunkZ + " | " + AverageHeight + " | " + biomes[biomeselector].name() + ": Climate: " + biomespec[3] + " Terrain: " + biomespec[4]);
    	
    	for (byte x = 0; x < Chunk.CHUNK_SIZE_X; x++) {
            for (byte z = 0; z < Chunk.CHUNK_SIZE_Z; z++) {
            	
            	
            	int posX = (chunkX*16+x);
                int posZ = (chunkZ*16+z);
            	
            	
            	int height = 0;
                
            	
        		height = (int) (50 + (HeightMap[x][z]));
            	
                
                int crust = (int) (height - 4);
                
                for (int level = 0; level < crust; level++) {
                    batch.setBlock(posX, level, posZ, Block.STONE);
                }
                
                for (int level = crust; level <= height; level++) {
                    batch.setBlock(posX, level, posZ, Block.STONE);
                }
                
                batch.setBlock(posX, height, posZ, Block.GRASS_BLOCK);
                if (random.nextDouble() < (Math.pow(Math.abs(VegetationNoise.getNoise((double) posX/92,(double)  posZ/92)), 8)) && x != 0 && x != 15 && z != 0 && z != 15) {
                	SpawnTree(batch, posX, height, posZ, biomes[biomeselector].name());
                }
            }
        }
    }

    public void SpawnTree(ChunkBatch batch, int posX, int posY, int posZ, String BiomeName) {
    	Block LogBlock = Block.OAK_LOG;
    	Block LeavesBlock = Block.OAK_LEAVES;
    	int Size = 5;
    	if (BiomeName.contains("OAK")) {
    		LogBlock = Block.OAK_LOG;
    		LeavesBlock = Block.OAK_LEAVES;
    	} else if (BiomeName.contains("BIRCH")) {
    		LogBlock = Block.BIRCH_LOG;
    		LeavesBlock = Block.BIRCH_LEAVES;
    	}
    	for (int y = 0; y < Size; y++) {
    		batch.setBlock(posX, posY + y, posZ, LogBlock);
    	}
    	batch.setBlock(posX - 1, posY + Size, posZ - 1, LeavesBlock);
    	batch.setBlock(posX - 1, posY + Size, posZ, LeavesBlock);
    	batch.setBlock(posX - 1, posY + Size, posZ + 1, LeavesBlock);
    	batch.setBlock(posX, posY + Size, posZ - 1, LeavesBlock);
    	batch.setBlock(posX, posY + Size, posZ, LeavesBlock);
    	batch.setBlock(posX, posY + Size, posZ + 1, LeavesBlock);
    	batch.setBlock(posX + 1, posY + Size, posZ - 1, LeavesBlock);
    	batch.setBlock(posX + 1, posY + Size, posZ, LeavesBlock);
    	batch.setBlock(posX + 1, posY + Size, posZ + 1, LeavesBlock);
    	
    }
    
    
    
    @Override
    public void fillBiomes(Biome[] biomes, int chunkX, int chunkZ) {

    	Arrays.fill(biomes, Biome.BEACH);
    }
    
    public double[] getBiome(int chunkX, int chunkZ, double terrainID) {
    	double climateID = 4.5 + 4.5 * (AridNoise.getNoise(chunkX/4, chunkZ/4) * VegetationNoise.getNoise(chunkX/4, chunkZ/4));
    	double[][] biomematches = {
				{0.0, 0.0}, {0.0, 0.0}, {0.0, 0.0}, {0.0, 0.0}, {0.0, 0.0}, 
				{0.0, 0.0}, {0.0, 0.0}, {0.0, 0.0}, {0.0, 0.0}, {0.0, 0.0}, 
				{0.0, 0.0}, {0.0, 0.0}, {0.0, 0.0}, {0.0, 0.0}, {0.0, 0.0}, 
				{0.0, 0.0}, {0.0, 0.0}, {0.0, 0.0}, {0.0, 0.0}, {0.0, 0.0}, 
				{0.0, 0.0}, {0.0, 0.0}, {0.0, 0.0}, {0.0, 0.0}, {0.0, 0.0}, 
				{0.0, 0.0}, {0.0, 0.0}, {0.0, 0.0}, {0.0, 0.0}, {0.0, 0.0}, 
				{0.0, 0.0}, {0.0, 0.0}, {0.0, 0.0}, {0.0, 0.0}, {0.0, 0.0}, 
				{0.0, 0.0}, {0.0, 0.0}, {0.0, 0.0}, {0.0, 0.0}, {0.0, 0.0}, 
				{0.0, 0.0}, {0.0, 0.0}, {0.0, 0.0}, {0.0, 0.0}, {0.0, 0.0}, 
				{0.0, 0.0}, {0.0, 0.0}, {0.0, 0.0}, {0.0, 0.0}, {0.0, 0.0}, 
				{0.0, 0.0}, {0.0, 0.0}, {0.0, 0.0}, {0.0, 0.0}, {0.0, 0.0}, 
				{0.0, 0.0}, {0.0, 0.0}, {0.0, 0.0}, {0.0, 0.0}, {0.0, 0.0}, 
				{0.0, 0.0}, {0.0, 0.0}, {0.0, 0.0}, {0.0, 0.0}, {0.0, 0.0}, 
				{0.0, 0.0}, {0.0, 0.0}, {0.0, 0.0}, {0.0, 0.0}, {0.0, 0.0}, 
				{0.0, 0.0}, {0.0, 0.0}, {0.0, 0.0}, {0.0, 0.0}, {0.0, 0.0}, 
				{0.0, 0.0}, {0.0, 0.0}, {0.0, 0.0}, {0.0, 0.0}, {0.0, 0.0}, 
				{0.0, 0.0}, {0.0, 0.0}
    			};
	
		biomematches[0][0] = Math.abs(climateID - 1.0); // DEEP_FROZEN_OCEAN
		biomematches[0][1] = Math.abs(terrainID - 1.0); // DEEP_FROZEN_OCEAN
		biomematches[1][0] = Math.abs(climateID - 2.0); // FROZEN_OCEAN
		biomematches[1][1] = Math.abs(terrainID - 1.0); // FROZEN_OCEAN
	  	biomematches[2][0] = Math.abs(climateID - 3.0); // DEEP_COLD_OCEAN
		biomematches[2][1] = Math.abs(terrainID - 1.0); // DEEP_COLD_OCEAN
	  	biomematches[3][0] = Math.abs(climateID - 4.0); // COLD_OCEAN
		biomematches[3][1] = Math.abs(terrainID - 1.0); // COLD_OCEAN
	  	biomematches[4][0] = Math.abs(climateID - 5.0); // DEEP_OCEAN
		biomematches[4][1] = Math.abs(terrainID - 1.0); // DEEP_OCEAN
	  	biomematches[5][0] = Math.abs(climateID - 6.0); // LUKEWARM_OCEAN
		biomematches[5][1] = Math.abs(terrainID - 1.0); // LUKEWARM_OCEAN
	  	biomematches[6][0] = Math.abs(climateID - 7.0); // DEEP_LUKEWARM_OCEAN
		biomematches[6][1] = Math.abs(terrainID - 1.0); // DEEP_LUKEWARM_OCEAN
	  	biomematches[7][0] = Math.abs(climateID - 8.0); // WARM_OCEAN
		biomematches[7][1] = Math.abs(terrainID - 1.0); // WARM_OCEAN
	  	biomematches[8][0] = Math.abs(climateID - 9.0); // DEEP_WARM_OCEAN
		biomematches[8][1] = Math.abs(terrainID - 1.0); // DEEP_WARM_OCEAN
	  	biomematches[9][0] = Math.abs(climateID - 1.0); // FROZEN_OCEAN
		biomematches[9][1] = Math.abs(terrainID - 2.0); // FROZEN_OCEAN
	  	biomematches[10][0] = Math.abs(climateID - 2.0); // SNOWY_BEACH
		biomematches[10][1] = Math.abs(terrainID - 2.0); // SNOWY_BEACH
	  	biomematches[11][0] = Math.abs(climateID - 3.0); // STONE_SHORE
		biomematches[11][1] = Math.abs(terrainID - 2.0); // STONE_SHORE
	  	biomematches[12][0] = Math.abs(climateID - 4.0); // MUSHROOM_FIELDS_SHORE
		biomematches[12][1] = Math.abs(terrainID - 2.0); // MUSHROOM_FIELDS_SHORE
	  	biomematches[13][0] = Math.abs(climateID - 5.0); // OCEAN
		biomematches[13][1] = Math.abs(terrainID - 2.0); // OCEAN
	  	biomematches[14][0] = Math.abs(climateID - 6.0); // BEACH
		biomematches[14][1] = Math.abs(terrainID - 2.0); // BEACH
	  	biomematches[15][0] = Math.abs(climateID - 7.0); // SWAMP
		biomematches[15][1] = Math.abs(terrainID - 2.0); // SWAMP
	  	biomematches[16][0] = Math.abs(climateID - 8.0); // MODIFIED_JUNGLE_EDGE
		biomematches[16][1] = Math.abs(terrainID - 2.0); // MODIFIED_JUNGLE_EDGE
	  	biomematches[17][0] = Math.abs(climateID - 9.0); // JUNGLE_EDGE
		biomematches[17][1] = Math.abs(terrainID - 2.0); // JUNGLE_EDGE
	  	biomematches[18][0] = Math.abs(climateID - 1.0); // ICE_SPIKES
		biomematches[18][1] = Math.abs(terrainID - 3.0); // ICE_SPIKES
	  	biomematches[19][0] = Math.abs(climateID - 2.0); // SNOWY_TAIGA
		biomematches[19][1] = Math.abs(terrainID - 3.0); // SNOWY_TAIGA
	  	biomematches[20][0] = Math.abs(climateID - 3.0); // TAIGA
		biomematches[20][1] = Math.abs(terrainID - 3.0); // TAIGA
	  	biomematches[21][0] = Math.abs(climateID - 4.0); // MUSHROOM_FIELDS
		biomematches[21][1] = Math.abs(terrainID - 3.0); // MUSHROOM_FIELDS
		biomematches[22][0] = Math.abs(climateID - 5.0); // BEACH
		biomematches[22][1] = Math.abs(terrainID - 3.0); // BEACH
	  	biomematches[23][0] = Math.abs(climateID - 6.0); // RIVER
		biomematches[23][1] = Math.abs(terrainID - 3.0); // RIVER
		biomematches[24][0] = Math.abs(climateID - 7.0); // MODIFIED_JUNGLE_EDGE
		biomematches[24][1] = Math.abs(terrainID - 3.0); // MODIFIED_JUNGLE_EDGE
	  	biomematches[25][0] = Math.abs(climateID - 8.0); // BAMBOO_JUNGLE
		biomematches[25][1] = Math.abs(terrainID - 3.0); // BAMBOO_JUNGLE
	  	biomematches[26][0] = Math.abs(climateID - 9.0); // JUNGLE
		biomematches[26][1] = Math.abs(terrainID - 3.0); // JUNGLE
	  	biomematches[27][0] = Math.abs(climateID - 1.0); // FROZEN_RIVER
		biomematches[27][1] = Math.abs(terrainID - 4.0); // FROZEN_RIVER
	  	biomematches[28][0] = Math.abs(climateID - 2.0); // GIANT_SPRUCE_TAIGA
		biomematches[28][1] = Math.abs(terrainID - 4.0); // GIANT_SPRUCE_TAIGA
		biomematches[29][0] = Math.abs(climateID - 3.0); // SNOWY_TAIGA
		biomematches[29][1] = Math.abs(terrainID - 4.0); // SNOWY_TAIGA
	  	biomematches[30][0] = Math.abs(climateID - 4.0); // PLAINS
		biomematches[30][1] = Math.abs(terrainID - 4.0); // PLAINS
		biomematches[31][0] = Math.abs(climateID - 5.0); // RIVER
		biomematches[31][1] = Math.abs(terrainID - 4.0); // RIVER
		biomematches[32][0] = Math.abs(climateID - 6.0); // PLAINS
		biomematches[32][1] = Math.abs(terrainID - 4.0); // PLAINS
	  	biomematches[33][0] = Math.abs(climateID - 7.0); // SUNFLOWER_PLAINS
		biomematches[33][1] = Math.abs(terrainID - 4.0); // SUNFLOWER_PLAINS
	  	biomematches[34][0] = Math.abs(climateID - 8.0); // SAVANA
		biomematches[34][1] = Math.abs(terrainID - 4.0); // SAVANA
	  	biomematches[35][0] = Math.abs(climateID - 9.0); // MODIFIED_JUNGLE
		biomematches[35][1] = Math.abs(terrainID - 4.0); // MODIFIED_JUNGLE
		biomematches[36][0] = Math.abs(climateID - 1.0); // FROZEN_RIVER
		biomematches[36][1] = Math.abs(terrainID - 5.0); // FROZEN_RIVER
	  	biomematches[37][0] = Math.abs(climateID - 2.0); // GIANT_TREE_TAIGA
		biomematches[37][1] = Math.abs(terrainID - 5.0); // GIANT_TREE_TAIGA
		biomematches[38][0] = Math.abs(climateID - 3.0); // DARK_FOREST
		biomematches[38][1] = Math.abs(terrainID - 5.0); // DARK_FOREST
	  	biomematches[39][0] = Math.abs(climateID - 4.0); // FOREST
		biomematches[39][1] = Math.abs(terrainID - 5.0); // FOREST
		biomematches[40][0] = Math.abs(climateID - 5.0); // PLAINS
		biomematches[40][1] = Math.abs(terrainID - 5.0); // PLAINS
		biomematches[41][0] = Math.abs(climateID - 6.0); // FOREST
		biomematches[41][1] = Math.abs(terrainID - 5.0); // FOREST
	  	biomematches[42][0] = Math.abs(climateID - 7.0); // DESERT_LAKES
		biomematches[42][1] = Math.abs(terrainID - 5.0); // DESERT_LAKES
	  	biomematches[43][0] = Math.abs(climateID - 8.0); // BAMBOO_JUNGLE_HILLS
		biomematches[43][1] = Math.abs(terrainID - 5.0); // BAMBOO_JUNGLE_HILLS
	  	biomematches[44][0] = Math.abs(climateID - 9.0); // DESERT
		biomematches[44][1] = Math.abs(terrainID - 5.0); // DESERT
	  	biomematches[45][0] = Math.abs(climateID - 1.0); // TAIGA_HILLS
		biomematches[45][1] = Math.abs(terrainID - 6.0); // TAIGA_HILLS
	  	biomematches[46][0] = Math.abs(climateID - 2.0); // FLOWER_FOREST
		biomematches[46][1] = Math.abs(terrainID - 6.0); // FLOWER_FOREST
	  	biomematches[47][0] = Math.abs(climateID - 3.0); // DARK_FOREST_HILLS
		biomematches[47][1] = Math.abs(terrainID - 6.0); // DARK_FOREST_HILLS
	  	biomematches[48][0] = Math.abs(climateID - 4.0); // DARK_FOREST
		biomematches[48][1] = Math.abs(terrainID - 6.0); // DARK_FOREST
	  	biomematches[49][0] = Math.abs(climateID - 5.0); // FOREST
		biomematches[49][1] = Math.abs(terrainID - 6.0); // FOREST
	  	biomematches[50][0] = Math.abs(climateID - 6.0); // BIRCH_FOREST
		biomematches[50][1] = Math.abs(terrainID - 6.0); // BIRCH_FOREST
	  	biomematches[51][0] = Math.abs(climateID - 7.0); // SHATTERED_SAVANA
		biomematches[51][1] = Math.abs(terrainID - 6.0); // SHATTERED_SAVANA
	  	biomematches[52][0] = Math.abs(climateID - 8.0); // JUNGLE_HILLS
		biomematches[52][1] = Math.abs(terrainID - 6.0); // JUNGLE_HILLS
	  	biomematches[53][0] = Math.abs(climateID - 9.0); // DESERT_HILLS
		biomematches[53][1] = Math.abs(terrainID - 6.0); // DESERT_HILLS
	  	biomematches[54][0] = Math.abs(climateID - 1.0); // SNOWY_TAIGA_HILLS
		biomematches[54][1] = Math.abs(terrainID - 7.0); // SNOWY_TAIGA_HILLS
		biomematches[55][0] = Math.abs(climateID - 2.0); // TAIGA_HILLS
		biomematches[55][1] = Math.abs(terrainID - 7.0); // TAIGA_HILLS
	  	biomematches[56][0] = Math.abs(climateID - 3.0); // GIANT_SPRUCE_TAIGA_HILLS
		biomematches[56][1] = Math.abs(terrainID - 7.0); // GIANT_SPRUCE_TAIGA_HILLS
		biomematches[57][0] = Math.abs(climateID - 4.0); // FLOWER_FOREST
		biomematches[57][1] = Math.abs(terrainID - 7.0); // FLOWER_FOREST
		biomematches[58][0] = Math.abs(climateID - 5.0); // FOREST
		biomematches[58][1] = Math.abs(terrainID - 7.0); // FOREST
	  	biomematches[59][0] = Math.abs(climateID - 6.0); // TALL_BIRCH_FOREST
		biomematches[59][1] = Math.abs(terrainID - 7.0); // TALL_BIRCH_FOREST
	  	biomematches[60][0] = Math.abs(climateID - 7.0); // WOODED_HILLS
		biomematches[60][1] = Math.abs(terrainID - 7.0); // WOODED_HILLS
	  	biomematches[61][0] = Math.abs(climateID - 8.0); // SWAMP_HILLS
		biomematches[61][1] = Math.abs(terrainID - 7.0); // SWAMP_HILLS
	  	biomematches[62][0] = Math.abs(climateID - 9.0); // SHATTERED_SAVANA_PLATEAU
		biomematches[62][1] = Math.abs(terrainID - 7.0); // SHATTERED_SAVANA_PLATEAU
	  	biomematches[63][0] = Math.abs(climateID - 1.0); // TAIGA_MOUNTAINS
		biomematches[63][1] = Math.abs(terrainID - 8.0); // TAIGA_MOUNTAINS
	  	biomematches[64][0] = Math.abs(climateID - 2.0); // GIANT_TREE_TAIGA_HILLS
		biomematches[64][1] = Math.abs(terrainID - 8.0); // GIANT_TREE_TAIGA_HILLS
	  	biomematches[65][0] = Math.abs(climateID - 3.0); // TALL_BIRCH_HILLS
		biomematches[65][1] = Math.abs(terrainID - 8.0); // TALL_BIRCH_HILLS
	  	biomematches[66][0] = Math.abs(climateID - 4.0); // MODIFIED_GRAVELLY_MOUNTAINS
		biomematches[66][1] = Math.abs(terrainID - 8.0); // MODIFIED_GRAVELLY_MOUNTAINS
	  	biomematches[67][0] = Math.abs(climateID - 5.0); // MOUNTAIN_EDGE
		biomematches[67][1] = Math.abs(terrainID - 8.0); // MOUNTAIN_EDGE
	  	biomematches[68][0] = Math.abs(climateID - 6.0); // BIRCH_FOREST_HILLS
		biomematches[68][1] = Math.abs(terrainID - 8.0); // BIRCH_FOREST_HILLS
	  	biomematches[69][0] = Math.abs(climateID - 7.0); // ERODED_BADLANDS
		biomematches[69][1] = Math.abs(terrainID - 8.0); // ERODED_BADLANDS
	  	biomematches[70][0] = Math.abs(climateID - 8.0); // BADLANDS
		biomematches[70][1] = Math.abs(terrainID - 8.0); // BADLANDS
	  	biomematches[71][0] = Math.abs(climateID - 9.0); // SAVANA_PLATEAU
		biomematches[71][1] = Math.abs(terrainID - 8.0); // SAVANA_PLATEAU
	  	biomematches[72][0] = Math.abs(climateID - 1.0); // SNOWY_TAIGA_MOUNTAINS
		biomematches[72][1] = Math.abs(terrainID - 9.0); // SNOWY_TAIGA_MOUNTAINS
	  	biomematches[73][0] = Math.abs(climateID - 2.0); // SNOWY_MOUNTAINS
		biomematches[73][1] = Math.abs(terrainID - 9.0); // SNOWY_MOUNTAINS
	  	biomematches[74][0] = Math.abs(climateID - 3.0); // GRAVELLY_MOUNTAINS
		biomematches[74][1] = Math.abs(terrainID - 9.0); // GRAVELLY_MOUNTAINS
	  	biomematches[75][0] = Math.abs(climateID - 4.0); // MOUNTAINS
		biomematches[75][1] = Math.abs(terrainID - 9.0); // MOUNTAINS
	  	biomematches[76][0] = Math.abs(climateID - 5.0); // WOODED_MOUTAINS
		biomematches[76][1] = Math.abs(terrainID - 9.0); // WOODED_MOUTAINS
	  	biomematches[77][0] = Math.abs(climateID - 6.0); // MODIFIED_BADLANDS_PLATEAU
		biomematches[77][1] = Math.abs(terrainID - 9.0); // MODIFIED_BADLANDS_PLATEAU
	  	biomematches[78][0] = Math.abs(climateID - 7.0); // WOODED_BADLANDS_PLATEAU
		biomematches[78][1] = Math.abs(terrainID - 9.0); // WOODED_BADLANDS_PLATEAU
	  	biomematches[79][0] = Math.abs(climateID - 8.0); // MODIFIED_WOODED_BADLANDS_PLATEAU
		biomematches[79][1] = Math.abs(terrainID - 9.0); // MODIFIED_WOODED_BADLANDS_PLATEAU
	  	biomematches[80][0] = Math.abs(climateID - 9.0); // BADLANDS_PLATEAU
		biomematches[80][1] = Math.abs(terrainID - 9.0); // BADLANDS_PLATEAU
		
    		
    		
    		
    		double[] bestmatch = {10.0, 10.0};
    		double bestmatchindex = 5.0;
    		for (int j = 0; j < (biomematches.length/2); j++) {
    			if (biomematches[j][0] + biomematches[j][1] < bestmatch[0] + bestmatch[1]) {
    				bestmatch[0] = biomematches[j][0];
    				bestmatch[1] = biomematches[j][1];
    				bestmatchindex = j;
    				
    			}
    		}
    		double[] returnvalue = {bestmatchindex, bestmatch[0], bestmatch[1], climateID, terrainID};
    		return returnvalue;
    }
    
    @Override
    public List<ChunkPopulator> getPopulators() {
        return null;
    }
}
