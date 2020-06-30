package net.minestom.vanilla.anvil;

import net.minestom.server.MinecraftServer;
import net.minestom.server.data.Data;
import net.minestom.server.instance.Biome;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.IChunkLoader;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.batch.ChunkBatch;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockManager;
import net.minestom.server.instance.block.CustomBlock;
import net.minestom.server.registry.Registries;
import net.minestom.server.storage.StorageFolder;
import net.minestom.server.utils.BlockPosition;
import net.minestom.vanilla.blocks.VanillaBlock;
import net.querz.mca.MCAFile;
import net.querz.mca.MCAUtil;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class AnvilChunkLoader implements IChunkLoader {
    private final static Logger LOGGER = LoggerFactory.getLogger(AnvilChunkLoader.class);

    private StorageFolder regionFolder;
    private ConcurrentHashMap<String, MCAFile> alreadyLoaded = new ConcurrentHashMap<>();

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
        MCAFile mcaFile = alreadyLoaded.computeIfAbsent(MCAUtil.createNameFromChunkLocation(chunkX, chunkZ), n -> {
            try {
                return MCAUtil.read(new File(regionFolder.getFolderPath(), n));
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        });
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
                loadBlocks(instance, chunkX, chunkZ, batch, fileChunk);
                batch.flush(c -> {
                    callback.accept(c);
                    loadTileEntities(c, chunkX, chunkZ, instance, fileChunk);
                });

                return loadedChunk;
            }
        }
        return null;
    }

    private void loadTileEntities(Chunk loadedChunk, int chunkX, int chunkZ, Instance instance, net.querz.mca.Chunk fileChunk) {
        BlockPosition pos = new BlockPosition(0,0,0);
        for(CompoundTag te : fileChunk.getTileEntities()) {
            String tileEntityID = te.getString("id");
            int x = te.getInt("x")+chunkX*16;
            int y = te.getInt("y");
            int z = te.getInt("z")+chunkZ*16;
            CustomBlock block = loadedChunk.getCustomBlock(x, y, z);
            if(block != null && block instanceof VanillaBlock) {
                pos.setX(x);
                pos.setY(y);
                pos.setZ(z);
                Data data = loadedChunk.getData(x, y, z);
                data = ((VanillaBlock) block).readTileEntity(te, instance, pos, data);
                loadedChunk.setBlockData(x, y, z, data);
            } else if(tileEntityID.equals("minecraft:chest")) {
                System.err.println("ouch "+block);
            }
        }
    }

    private void loadBlocks(Instance instance, int chunkX, int chunkZ, ChunkBatch batch, net.querz.mca.Chunk fileChunk) {
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

                        short customBlockId = 0;
                        Data data = null;
                        CustomBlock customBlock = MinecraftServer.getBlockManager().getCustomBlock(rBlock.getBlockId());
                        if(customBlock != null && customBlock instanceof VanillaBlock) {
                            customBlockId = rBlock.getBlockId();

                            data = customBlock.createData(instance, new BlockPosition(x+chunkX*16, y, z+chunkZ*16), null);
                        }
                        if (blockState.getCompoundTag("Properties") != null) {
                            CompoundTag properties = blockState.getCompoundTag("Properties");
                            List<String> propertiesArray = new ArrayList<>();
                            properties.forEach((key, value2) -> {
                                Tag<String> value = (Tag<String>) value2;
                                propertiesArray.add(key + "=" + value.valueToString().replace("\"", ""));
                            });
                            Collections.sort(propertiesArray);
                            short block = rBlock.withProperties(propertiesArray.toArray(new String[0]));

                            if(customBlock != null) {
                                batch.setSeparateBlocks(x, y, z, block, customBlockId, data);
                            } else {
                                batch.setBlock(x, y, z, block);
                            }
                        } else {
                            if(customBlock != null) {
                                batch.setSeparateBlocks(x, y, z, rBlock.getBlockId(), customBlockId, data);
                            } else {
                                batch.setBlock(x, y, z, rBlock.getBlockId());
                            }
                        }
                    } catch (NullPointerException ignored) {

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    // TODO: find a way to unload MCAFiles when an entire region is unloaded

    @Override
    public void saveChunk(Chunk chunk, Runnable callback) {
        // TODO
        LOGGER.debug("Attempt saving at {} {}", chunk.getChunkX(), chunk.getChunkZ());
    }
}
