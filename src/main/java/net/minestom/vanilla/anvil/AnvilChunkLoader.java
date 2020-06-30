package net.minestom.vanilla.anvil;

import net.minestom.server.instance.Biome;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.IChunkLoader;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.batch.ChunkBatch;
import net.minestom.server.instance.block.Block;
import net.minestom.server.registry.Registries;
import net.minestom.server.storage.StorageFolder;
import net.querz.mca.MCAFile;
import net.querz.mca.MCAUtil;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class AnvilChunkLoader implements IChunkLoader {
    private final static Logger LOGGER = LoggerFactory.getLogger(AnvilChunkLoader.class);

    private StorageFolder regionFolder;

    public AnvilChunkLoader(StorageFolder regionFolder) {
        this.regionFolder = regionFolder;
    }

    @Override
    public boolean loadChunk(Instance instance, int chunkX, int chunkZ, Consumer<Chunk> callback) {
        LOGGER.debug("Attempt loading at {} {}", chunkX, chunkZ);
        try {
            Chunk chunk = loadMCA(instance, chunkX, chunkZ, callback);
            return chunk != null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Chunk loadMCA(Instance instance, int chunkX, int chunkZ, Consumer<Chunk> callback) throws IOException {
        MCAFile mcaFile = MCAUtil.read(new File(regionFolder.getFolderPath(), MCAUtil.createNameFromChunkLocation(chunkX, chunkZ)));
        if (mcaFile != null) {
            net.querz.mca.Chunk fileChunk = mcaFile.getChunk(chunkX, chunkZ);
            if (fileChunk != null) {
                Biome[] biomes = new Biome[Chunk.BIOME_COUNT];
                int[] fileChunkBiomes = fileChunk.getBiomes();
                for (int i = 0; i < fileChunkBiomes.length; i++) {
                    int id = fileChunkBiomes[i];
                    biomes[i] = Biome.fromId(id);
                }
                Chunk loadedChunk = new Chunk(biomes, chunkX, chunkZ);
                ChunkBatch batch = instance.createChunkBatch(loadedChunk);
                batch.setBlock(0,0,0,Block.BEDROCK.getBlockId(), null);
                for (int x = 0; x < Chunk.CHUNK_SIZE_X; x++) {
                    for (int z = 0; z < Chunk.CHUNK_SIZE_Z; z++) {
                        for (int y = 0; y < Chunk.CHUNK_SIZE_Y; y++) {
                            try {
                                CompoundTag blockState = fileChunk.getBlockStateAt(x, y, z);
                                if (blockState == null) {
                                    continue;
                                }
                                String name = blockState.get("Name").valueToString();
                                name = name.substring(1, name.length()-1); // trim quotes
                                Block rBlock = Registries.getBlock(name);

                                if (blockState.getCompoundTag("Properties") != null) {
                                    CompoundTag properties = blockState.getCompoundTag("Properties");
                                    List<String> propertiesArray = new ArrayList<>();
                                    properties.forEach((key, value2) -> {
                                        Tag<String> value = (Tag<String>) value2;
                                        propertiesArray.add(key + "=" + value.valueToString().replace("\"", ""));
                                    });
                                    Collections.sort(propertiesArray);
                                    short block = rBlock.withProperties(propertiesArray.toArray(new String[0]));

                                    // TODO: custom blocks
                                    batch.setBlock(x, y, z, block);
                                } else {
                                    batch.setBlock(x, y, z, rBlock.getBlockId());
                                }
                            } catch (NullPointerException ignored) {

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                batch.flush(callback);
                return loadedChunk;
            }
        }
        return null;
    }

    @Override
    public void saveChunk(Chunk chunk, Runnable callback) {
        // TODO
        LOGGER.debug("Attempt saving at {} {}", chunk.getChunkX(), chunk.getChunkZ());
    }
}
