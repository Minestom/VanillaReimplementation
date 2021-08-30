package net.minestom.vanilla.blocks.update;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.instance.InstanceTickEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.tag.Tag;
import net.minestom.vanilla.blocks.VanillaBlockHandler;
import net.minestom.vanilla.blocks.update.info.BlockUpdate;
import net.minestom.vanilla.blocks.update.info.BlockUpdateInfo;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * A utility class used to facilitate block updates
 */
public class BlockUpdateManager {
    // IDs
    public static final Tag<Long> ID = Tag.Long("minestom:blockupdatemanager_id");
    private static long nextID = 0;
    private static final Map<Long, BlockUpdateManager> blockUpdateManagerById = new HashMap<>();


    private final Instance instance;
    private final Map<Point, BlockUpdateInfo> updateNeighbors = new LinkedHashMap<>();

    public static void init(EventNode<Event> eventNode) {
        eventNode.addListener(
                EventListener.of(
                        InstanceTickEvent.class, BlockUpdateManager::instanceTick
                )
        );
    }

    private static void instanceTick(InstanceTickEvent event) {
        Instance instance = event.getInstance();
        long time = event.getDuration();
        BlockUpdateManager.of(instance).tick(time);
    }

    private BlockUpdateManager(@NotNull Instance instance) {
        this.instance = instance;
        long id = nextID++;
        instance.setTag(ID, id);
        blockUpdateManagerById.put(id, this);
    }

    public static @NotNull BlockUpdateManager of(Instance instance) {
        Long id = instance.getTag(ID);

        if (id == null) {
            return new BlockUpdateManager(instance);
        }

        return blockUpdateManagerById.get(id);
    }

    public static boolean remove(Instance instance) {
        Long id = instance.getTag(ID);

        if (id == null) {
            throw new IllegalArgumentException("Argument 'instance' passed to BlockUpdateManager#remove did not have an associated block update manager.");
        }

        instance.setTag(ID, null);

        return blockUpdateManagerById.remove(id) != null;
    }

    // Public api methods

    /**
     * Schedules this position's neighbors to be updated next tick.
     */
    public void scheduleNeighborsUpdate(Point pos, BlockUpdateInfo info) {
        updateNeighbors.put(pos, info);
    }

    // Public api methods end

    private void tick(long time) {
        updateNeighbors(time);
    }

    private void updateNeighbors(long time) {
        if (updateNeighbors.size() == 0) {
            return;
        }

        // Update all the neighbors
        for (Map.Entry<Point, BlockUpdateInfo> entry : updateNeighbors.entrySet()) {
            Point pos = entry.getKey();
            BlockUpdateInfo info = entry.getValue();

            int x = pos.blockX();
            int y = pos.blockY();
            int z = pos.blockZ();

            // For each surrounding block
            for (int offsetX = -1; offsetX < 2; offsetX++)
                for (int offsetY = -1; offsetY < 2; offsetY++)
                    for (int offsetZ = -1; offsetZ < 2; offsetZ++) {

                        // If block is not the original block
                        if (offsetX == 0 && offsetY == 0 && offsetZ == 0) {
                            continue;
                        }

                        // Get the block handler at the position
                        Point blockPos = new Pos(x + offsetX, y + offsetY, z + offsetZ);
                        Block block = instance.getBlock(blockPos);
                        BlockHandler handler = block.handler();

                        // Update block handler if applicable
                        if (handler instanceof VanillaBlockHandler) {
                            BlockUpdate blockUpdate = new BlockUpdate(
                                    instance,
                                    blockPos,
                                    block,
                                    info
                            );

                            ((VanillaBlockHandler) handler).onBlockUpdate(blockUpdate);
                        }
                    }
        }

        updateNeighbors.clear();
    }
}
