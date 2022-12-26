package net.minestom.vanilla.blockupdatesystem;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2BooleanMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.instance.InstanceChunkLoadEvent;
import net.minestom.server.event.instance.InstanceTickEvent;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.randomticksystem.RandomTickManager;
import net.minestom.vanilla.randomticksystem.RandomTickable;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * A utility class used to facilitate block updates
 */
public class BlockUpdateManager {
    // Block update manager by instance
    private static final Map<Instance, BlockUpdateManager> instance2BlockUpdateManager =
            Collections.synchronizedMap(new WeakHashMap<>());

    // Block updatables
    private static final Map<NamespaceID, BlockUpdatable> blockUpdatables = new ConcurrentHashMap<>();

    public static void registerUpdatable(Block block, @NotNull BlockUpdatable updatable) {
        synchronized (blockUpdatables) {
            blockUpdatables.put(block.namespace(), updatable);
        }
    }

    public static void init(EventNode<Event> eventNode) {
        eventNode.addListener(InstanceTickEvent.class, BlockUpdateManager::instanceTick);
        eventNode.addListener(PlayerBlockBreakEvent.class, event ->
                BlockUpdateManager.from(event.getPlayer().getInstance())
                        .scheduleNeighborsUpdate(event.getBlockPosition(),
                                BlockUpdateInfo.DESTROY_BLOCK(), 1)
        );
        eventNode.addListener(PlayerBlockPlaceEvent.class, event ->
                BlockUpdateManager.from(event.getPlayer().getInstance())
                        .scheduleNeighborsUpdate(event.getBlockPosition(),
                                BlockUpdateInfo.PLACE_BLOCK(), 1)
        );
        eventNode.addListener(InstanceChunkLoadEvent.class, event -> {
            Chunk chunk = event.getChunk();
            int minY = chunk.getMinSection() * Chunk.CHUNK_SECTION_SIZE;
            int maxY = chunk.getMaxSection() * Chunk.CHUNK_SECTION_SIZE;
            int minX = chunk.getChunkX() * Chunk.CHUNK_SIZE_X;
            int minZ = chunk.getChunkZ() * Chunk.CHUNK_SIZE_Z;

            Instance instance = event.getInstance();
            BlockUpdateManager.from(instance);

            synchronized (blockUpdatables) {
                synchronized (chunk) {
                    for (int x = minX; x < minX + Chunk.CHUNK_SIZE_X; x++) {
                        for (int z = minZ; z < minZ + Chunk.CHUNK_SIZE_Z; z++) {
                            for (int y = minY; y < maxY; y++) {
                                Block block = chunk.getBlock(x, y, z);
                                BlockUpdatable updatable = blockUpdatables.get(block.namespace());
                                if (updatable == null) continue;
                                updatable.blockUpdate(instance, new Vec(x, y, z), BlockUpdateInfo.CHUNK_LOAD());
                            }
                        }
                    }
                }
            }
        });
    }

    private static void instanceTick(InstanceTickEvent event) {
        Instance instance = event.getInstance();
        from(instance).tick(event.getDuration());
    }

    public static @NotNull BlockUpdateManager from(@NotNull Instance instance) {
        return instance2BlockUpdateManager.computeIfAbsent(instance, BlockUpdateManager::new);
    }

    private final Long2ObjectMap<Updates> updates = Long2ObjectMaps.synchronize(new Long2ObjectOpenHashMap<>());
    private final BlockUpdateManager.UpdateHandler updateHandler;
    private final Instance instance;

    public BlockUpdateManager(@NotNull Instance instance, @NotNull BlockUpdateManager.UpdateHandler updateHandler) {
        this.updateHandler = updateHandler;
        this.instance = instance;
    }

    private BlockUpdateManager(@NotNull Instance instance) {
        this(instance, (pos, info) -> {
            BlockUpdatable updateable = blockUpdatables.get(instance.getBlock(pos).namespace());
            if (updateable == null) return;
            updateable.blockUpdate(instance, pos, info);
        });
    }

    public static void registerRandomTickable(short stateId, RandomTickable randomTickable) {
        RandomTickManager.registerRandomTickable(stateId, randomTickable);
    }

    public interface UpdateHandler {
        void update(@NotNull Point pos, @NotNull BlockUpdateInfo info);
    }

    // Public api methods

    /**
     * Schedules this position's neighbors to be updated next tick.
     */
    public void scheduleNeighborsUpdate(Point pos, BlockUpdateInfo info, int delayTicks) {
        long tickTime = instance.getWorldAge() + delayTicks;
        updates.computeIfAbsent(tickTime, ignored -> new Updates()).addNeighbors(pos, info);
    }

    public void scheduleUpdate(Point pos, BlockUpdateInfo info, int delayTicks) {
        long tickTime = instance.getWorldAge() + delayTicks;
        updates.computeIfAbsent(tickTime, ignored -> new Updates()).add(pos, info);
    }

    // Public api methods end

    private void tick(int duration) {
        if (updates.size() == 0) return;
        long worldAge = instance.getWorldAge();
        updates.forEach((tickTime, updates) -> {
            if (tickTime > worldAge) return;
            updates.forEach(updateHandler::update);
            updates.clear();
        });
        updates.keySet().removeIf(tickTime -> tickTime <= worldAge);
    }

    private static class Updates {
        private final List<Map.Entry<Point, BlockUpdateInfo>> updates = new ArrayList<>();

        public synchronized void forEach(BiConsumer<Point, BlockUpdateInfo> consumer) {
            updates.forEach(entry -> consumer.accept(entry.getKey(), entry.getValue()));
        }

        public synchronized void add(Point pos, BlockUpdateInfo info) {
            updates.add(Map.entry(pos, info));
        }

        public synchronized void addNeighbors(Point pos, BlockUpdateInfo info) {
            int x = pos.blockX();
            int y = pos.blockY();
            int z = pos.blockZ();

            // For each surrounding block
            for (int offsetX = -1; offsetX < 2; offsetX++) {
                for (int offsetY = -1; offsetY < 2; offsetY++) {
                    for (int offsetZ = -1; offsetZ < 2; offsetZ++) {

                        // If block is not the original block
                        if (offsetX == 0 && offsetY == 0 && offsetZ == 0) {
                            continue;
                        }

                        // Get the block handler at the position
                        Point blockPos = new Pos(x + offsetX, y + offsetY, z + offsetZ);
                        add(blockPos, info);
                    }
                }
            }
        }

        public synchronized void clear() {
            updates.clear();
        }

        public synchronized boolean isEmpty() {
            return updates.isEmpty();
        }
    }
}
