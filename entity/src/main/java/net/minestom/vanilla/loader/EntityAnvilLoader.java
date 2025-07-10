package net.minestom.vanilla.loader;

import it.unimi.dsi.fastutil.ints.IntIntImmutablePair;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.BinaryTagTypes;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.DoubleBinaryTag;
import net.kyori.adventure.nbt.FloatBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.CoordConversion;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.anvil.AnvilLoader;
import net.minestom.server.utils.validate.Check;
import net.minestom.vanilla.Entities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class EntityAnvilLoader extends AnvilLoader {
    private final static Logger LOGGER = LoggerFactory.getLogger(EntityAnvilLoader.class);

    private final ReentrantLock fileCreationLock = new ReentrantLock();
    private final Map<String, RegionFile> alreadyLoaded = new ConcurrentHashMap<>();

    private static class RegionCache extends ConcurrentHashMap<IntIntImmutablePair, Set<IntIntImmutablePair>> {
    }

    /**
     * Represents the chunks currently loaded per region. Used to determine when a region file can be unloaded.
     */
    private final RegionCache perRegionLoadedChunks = new RegionCache();
    private final ReentrantLock perRegionLoadedChunksLock = new ReentrantLock();

    private final @NotNull Path entityPath;
    private final @NotNull Path path; // Some sort of getter in AnvilLoader would be nice


    public EntityAnvilLoader(@NotNull Path path) {
        super(path);
        this.path = path;
        this.entityPath = path.resolve("entities");
    }

    @Override
    public @Nullable Chunk loadChunk(@NotNull Instance instance, int chunkX, int chunkZ) {
        if (!Files.exists(path)) {
            // No world folder
            return null;
        }
        try {
            return loadEntityMCA(instance, chunkX, chunkZ);
        } catch (Exception e) {
            MinecraftServer.getExceptionManager().handleException(e);
            return null;
        }
    }

    private @Nullable Chunk loadEntityMCA(Instance instance, int chunkX, int chunkZ) throws IOException {
        Chunk chunk = super.loadChunk(instance, chunkX, chunkZ);
        if (chunk == null) {
            // No chunk data, nothing to load
            return null;
        }

        final RegionFile mcaFile = getEntityMCAFile(chunkX, chunkZ);
        if (mcaFile == null) {
            return chunk;
        }
        final CompoundBinaryTag chunkData = mcaFile.readChunkData(chunkX, chunkZ);
        if (chunkData == null) { // No entity data for this chunk means there are no entities in this chunk
            return chunk;
        }

        synchronized (chunk) { // todo: boo, synchronized
            loadEntities(chunk, chunkData);
        }

        // Cache the index of the loaded chunk
        perRegionLoadedChunksLock.lock();
        try {
            int regionX = CoordConversion.chunkToRegion(chunkX);
            int regionZ = CoordConversion.chunkToRegion(chunkZ);
            var chunks = perRegionLoadedChunks.computeIfAbsent(new IntIntImmutablePair(regionX, regionZ),
                    r -> new HashSet<>()); // region cache may have been removed on another thread due to unloadChunk
            chunks.add(new IntIntImmutablePair(chunkX, chunkZ));
        } finally {
            perRegionLoadedChunksLock.unlock();
        }
        return chunk;
    }

    private @Nullable RegionFile getEntityMCAFile(int chunkX, int chunkZ) {
        final int regionX = CoordConversion.chunkToRegion(chunkX);
        final int regionZ = CoordConversion.chunkToRegion(chunkZ);
        final String fileName = RegionFile.getFileName(regionX, regionZ);

        final RegionFile loadedFile = alreadyLoaded.get(fileName);

        if (loadedFile != null) return loadedFile;

        perRegionLoadedChunksLock.lock();
        try {
            return alreadyLoaded.computeIfAbsent(fileName, n -> {
                final Path regionPath = this.entityPath.resolve(n);
                if (!Files.exists(regionPath)) {
                    return null;
                }

                try {
                    Set<IntIntImmutablePair> previousVersion =
                            perRegionLoadedChunks.put(new IntIntImmutablePair(regionX, regionZ), new HashSet<>());
                    assert previousVersion == null : "The EntityAnvilLoader cache should not already have data for " +
                            "this region.";
                    return new RegionFile(regionPath);
                } catch (IOException e) {
                    MinecraftServer.getExceptionManager().handleException(e);
                    return null;
                }
            });
        } finally {
            perRegionLoadedChunksLock.unlock();
        }
    }

    private void loadEntities(@NotNull Chunk chunk, @NotNull CompoundBinaryTag chunkData) {
        for (BinaryTag entityTag : chunkData.getList("Entities", BinaryTagTypes.COMPOUND)) {
            final CompoundBinaryTag entityData = (CompoundBinaryTag) entityTag;

            String type = entityData.getString("id");
            EntityType entityType = EntityType.fromKey(type);
            Check.notNull(entityType, "Unknown entity type: " + type);

            ListBinaryTag posData = entityData.getList("Pos", BinaryTagTypes.DOUBLE);
            Check.stateCondition(posData.size() != 3,
                    "Entity position must have 3 coordinates, found: " + posData.size());
            double x = posData.getDouble(0);
            double y = posData.getDouble(1);
            double z = posData.getDouble(2);

            ListBinaryTag rotation = entityData.getList("Rotation", BinaryTagTypes.FLOAT);
            Check.stateCondition(rotation.size() != 2, "Entity rotation must have 2 values, found: " + rotation.size());
            float yaw = rotation.getFloat(0);
            float pitch = rotation.getFloat(1);

            Pos pos = new Pos(x, y, z, yaw, pitch);
            Entities.spawnEntity(entityType, chunk.getInstance(), pos, entityData);
        }
    }

    @Override
    public void saveChunk(@NotNull Chunk chunk) {
        super.saveChunk(chunk);
        final int chunkX = chunk.getChunkX();
        final int chunkZ = chunk.getChunkZ();

        // Find the region file or create an empty one if missing
        RegionFile mcaFile;
        fileCreationLock.lock();
        try {
            mcaFile = getEntityMCAFile(chunkX, chunkZ);
            if (mcaFile == null) {
                final int regionX = CoordConversion.chunkToRegion(chunkX);
                final int regionZ = CoordConversion.chunkToRegion(chunkZ);
                final String regionFileName = RegionFile.getFileName(regionX, regionZ);
                try {
                    Path regionFile = entityPath.resolve(regionFileName);
                    if (!Files.exists(regionFile)) {
                        Files.createDirectories(regionFile.getParent());
                        Files.createFile(regionFile);
                    }

                    mcaFile = new RegionFile(regionFile);
                    alreadyLoaded.put(regionFileName, mcaFile);
                } catch (IOException e) {
                    LOGGER.error("Failed to create region file for {}, {}", chunkX, chunkZ, e);
                    MinecraftServer.getExceptionManager().handleException(e);
                    return;
                }
            }
        } finally {
            fileCreationLock.unlock();
        }

        try {
            final CompoundBinaryTag.Builder chunkData = CompoundBinaryTag.builder();

            chunkData.putInt("DataVersion", MinecraftServer.DATA_VERSION);
            chunkData.putIntArray("Position", new int[]{chunkX, chunkZ});

            saveEntityData(chunk, chunkData);

            mcaFile.writeChunkData(chunkX, chunkZ, chunkData.build());
        } catch (IOException e) {
            LOGGER.error("Failed entitydata to save chunk {}, {}", chunkX, chunkZ, e);
            MinecraftServer.getExceptionManager().handleException(e);
        }
    }

    /**
     * Saves the entity data for a given chunk into the provided chunk data.
     *
     * @param chunk the chunk to save entity data for
     * @param chunkData the chunk data to save the entities into
     */
    private void saveEntityData(@NotNull Chunk chunk, @NotNull CompoundBinaryTag.Builder chunkData) {
        final ListBinaryTag.Builder<CompoundBinaryTag> entities = ListBinaryTag.builder(BinaryTagTypes.COMPOUND);

        for (Entity entity : chunk.getInstance().getChunkEntities(chunk)) {
            if (entity.isRemoved()) {
                // Skip removed entities
                continue;
            }

            if (entity instanceof Player) {
                // Skip players, saved elsewhere
                continue;
            }

            // Get the entity data and add it to the list
            CompoundBinaryTag entityData = getEntityData(entity);
            entities.add(entityData);
        }

        chunkData.put("Entities", entities.build());
    }

    //TODO: Proper way to save/load entity data
    private CompoundBinaryTag getEntityData(final Entity entity) {
        final CompoundBinaryTag.Builder tag = CompoundBinaryTag.builder();
        tag.putString("id", entity.getEntityType().key().asString());
        tag.putIntArray("uuid", new int[]{
                (int) (entity.getUuid().getMostSignificantBits() >> 32),
                (int) entity.getUuid().getMostSignificantBits(),
                (int) (entity.getUuid().getLeastSignificantBits() >> 32),
                (int) entity.getUuid().getLeastSignificantBits()
        });

        ListBinaryTag posList = ListBinaryTag.builder(BinaryTagTypes.DOUBLE)
                .add(DoubleBinaryTag.doubleBinaryTag(entity.getPosition().x()))
                .add(DoubleBinaryTag.doubleBinaryTag(entity.getPosition().y()))
                .add(DoubleBinaryTag.doubleBinaryTag(entity.getPosition().z()))
                .build();
        tag.put("Pos", posList);

        ListBinaryTag rotationList = ListBinaryTag.builder(BinaryTagTypes.FLOAT)
                .add(FloatBinaryTag.floatBinaryTag(entity.getPosition().yaw()))
                .add(FloatBinaryTag.floatBinaryTag(entity.getPosition().pitch()))
                .build();
        tag.put("Rotation", rotationList);
        return tag.build();
    }

    /**
     * Unload a given chunk. Also unloads a region when no chunk from that region is loaded.
     *
     * @param chunk the chunk to unload
     */
    @Override
    public void unloadChunk(Chunk chunk) {
        super.unloadChunk(chunk);
        final int regionX = CoordConversion.chunkToRegion(chunk.getChunkX());
        final int regionZ = CoordConversion.chunkToRegion(chunk.getChunkZ());
        final IntIntImmutablePair regionKey = new IntIntImmutablePair(regionX, regionZ);

        perRegionLoadedChunksLock.lock();
        try {
            Set<IntIntImmutablePair> chunks = perRegionLoadedChunks.get(regionKey);
            if (chunks != null) { // if null, trying to unload a chunk from a region that was not created by the
                // AnvilLoader
                // don't check return value, trying to unload a chunk not created by the AnvilLoader is valid
                chunks.remove(new IntIntImmutablePair(chunk.getChunkX(), chunk.getChunkZ()));

                if (chunks.isEmpty()) {
                    perRegionLoadedChunks.remove(regionKey);
                    RegionFile regionFile = alreadyLoaded.remove(RegionFile.getFileName(regionX, regionZ));
                    if (regionFile != null) {
                        try {
                            regionFile.close();
                        } catch (IOException e) {
                            MinecraftServer.getExceptionManager().handleException(e);
                        }
                    }
                }
            }
        } finally {
            perRegionLoadedChunksLock.unlock();
        }
    }
}
