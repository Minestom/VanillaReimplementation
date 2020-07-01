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
import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.registry.Registries;
import net.minestom.server.storage.StorageFolder;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.SerializerUtils;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.utils.nbt.NbtWriter;
import net.minestom.vanilla.blocks.BlockState;
import net.minestom.vanilla.blocks.BlockStates;
import net.minestom.vanilla.blocks.VanillaBlock;
import net.querz.mca.MCAFile;
import net.querz.mca.MCAUtil;
import net.querz.mca.Section;
import net.querz.nbt.io.NBTDeserializer;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.ListTag;
import net.querz.nbt.tag.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class AnvilChunkLoader implements IChunkLoader {
    private final static Logger LOGGER = LoggerFactory.getLogger(AnvilChunkLoader.class);

    private StorageFolder regionFolder;
    private ConcurrentHashMap<String, MCAFile> alreadyLoaded = new ConcurrentHashMap<>();
    private NBTDeserializer nbtDeserializer = new NBTDeserializer();

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
        MCAFile mcaFile = getMCAFile(chunkX, chunkZ);
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
                    // TODO: Other elements to load
                });

                return loadedChunk;
            }
        }
        return null;
    }

    private MCAFile getMCAFile(int chunkX, int chunkZ) {
        return alreadyLoaded.computeIfAbsent(MCAUtil.createNameFromChunkLocation(chunkX, chunkZ), n -> {
            try {
                return MCAUtil.read(new File(regionFolder.getFolderPath(), n));
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        });
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
        int chunkX = chunk.getChunkX();
        int chunkZ = chunk.getChunkZ();
        MCAFile mcaFile;
        synchronized (alreadyLoaded) {
            mcaFile = getMCAFile(chunkX, chunkZ);
            if(mcaFile == null) {
                mcaFile = new MCAFile(MCAUtil.chunkToRegion(chunkX), MCAUtil.chunkToRegion(chunkZ));
                alreadyLoaded.put(MCAUtil.createNameFromChunkLocation(chunkX, chunkZ), mcaFile);
            }
        }
        int[] biomes = new int[Chunk.BIOME_COUNT];
        for (int i = 0; i < biomes.length; i++) {
            Biome biome = chunk.getBiomes()[i];
            if(biome == null) {
                biome = Biome.THE_VOID;
            }
            biomes[i] = biome.getId();
        }
        save(chunk, mcaFile);

        // TODO: other elements to save
        try {
            mcaFile.cleanupPalettesAndBlockStates();
          // FIXME readd  MCAUtil.write(mcaFile, new File(regionFolder.getFolderPath(), MCAUtil.createNameFromChunkLocation(chunkX, chunkZ)));
            LOGGER.debug("Attempt saving at {} {}", chunk.getChunkX(), chunk.getChunkZ());
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("AAAA", e);
        }

        if(callback != null)
            callback.run();
    }

    private void saveTileEntities(Chunk chunk, net.querz.mca.Chunk fileChunk) {
        ListTag<CompoundTag> tileEntities = new ListTag<>(CompoundTag.class);
        BlockPosition position = new BlockPosition(0, 0, 0);

        for(var index : chunk.getBlockEntities()) {
            int[] pos = ChunkUtils.indexToChunkPosition(index);
            int x = pos[0];
            int y = pos[1];
            int z = pos[2];
            position.setX(x);
            position.setY(y);
            position.setZ(z);
            CustomBlock customBlock = chunk.getCustomBlock(x, y, z);
            if(customBlock instanceof VanillaBlock) {
                NbtWriter writer = new NbtWriter(new PacketWriter());
                Data data = chunk.getData(x, y, z);
                customBlock.writeBlockEntity(position, data, writer);

                tileEntities.add(convertToTag(writer));
            }
        }
        fileChunk.setTileEntities(tileEntities);
    }

    private CompoundTag convertToTag(NbtWriter writer) {
        try {
            return (CompoundTag) nbtDeserializer.fromStream(new ByteArrayInputStream(writer.getPacketWriter().toByteArray())).getTag();
        } catch (IOException e) {
            e.printStackTrace();
            return new CompoundTag();
        }
    }

    private void save(Chunk chunk, MCAFile mcaFile) {
        int chunkX = chunk.getChunkX();
        int chunkZ = chunk.getChunkZ();
        for (int x = 0; x < Chunk.CHUNK_SIZE_X; x++) {
            for (int z = 0; z < Chunk.CHUNK_SIZE_Z; z++) {
                for (int y = 0; y < Chunk.CHUNK_SIZE_Y; y++) {
                    CompoundTag state = new CompoundTag();
                    short id = chunk.getBlockId(x, y, z);
                    CustomBlock customBlock = chunk.getCustomBlock(x, y, z);
                    Block block = Block.fromId(id);
                    state.putString("Name", block.getName());

                    if(customBlock instanceof VanillaBlock) {
                        VanillaBlock vanillaBlock = (VanillaBlock) customBlock;
                        BlockStates states = vanillaBlock.getBlockStates();
                        BlockState blockstate = states.fromStateID(id);

                        CompoundTag properties = new CompoundTag();
                        for(var entry : blockstate.getProperties().entrySet()) {
                            properties.putString(entry.getKey(), entry.getValue());
                        }

                        if(properties.size() != 0) {
                            state.put("Properties", properties);
                        }
                    }

                    net.querz.mca.Chunk fileChunk = mcaFile.getChunk(chunkX, chunkZ);
                    if(fileChunk != null) {
                        Section existingSection = fileChunk.getSection(MCAUtil.blockToChunk(y));
                        if(existingSection != null && (existingSection.getPalette() == null || existingSection.getBlockStates() == null)) {
                            Section section = Section.newSection();
                            fileChunk.setSection(MCAUtil.blockToChunk(y), section);
                        }
                    }

                    int blockX = x+chunkX*16;
                    int blockY = y;
                    int blockZ = z+chunkZ*16;
                    mcaFile.setBlockStateAt(blockX, blockY, blockZ, state, false);

                    int index = ((y >> 2) & 63) << 4 | ((z >> 2) & 3) << 2 | ((x >> 2) & 3); // https://wiki.vg/Chunk_Format#Biomes
                    Biome biome = chunk.getBiomes()[index];
                    mcaFile.setBiomeAt(blockX, 0, blockZ, biome.getId());
                }
            }
        }
    }
}
