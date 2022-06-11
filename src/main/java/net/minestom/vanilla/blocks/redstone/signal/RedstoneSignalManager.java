package net.minestom.vanilla.blocks.redstone.signal;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.tag.Tag;
import net.minestom.vanilla.blocks.VanillaBlockHandler;
import net.minestom.vanilla.blocks.redstone.RedstoneContainerBlockHandler;
import net.minestom.vanilla.blocks.redstone.signal.info.RedstoneSignal;
import net.minestom.vanilla.blocks.redstone.signal.info.RedstoneSignalTarget;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;

public class RedstoneSignalManager {
    // IDs
    public static final Tag<Long> ID = Tag.Long("minestom:redstonesignalmanager_id");
    private static long nextID = 0;
    private static final Map<Long, RedstoneSignalManager> redstoneSignalManagerById = new HashMap<>();

    // Fields
    private final Instance instance;
    private static final Map<Pos, PriorityQueue<RedstoneSignal>> redstoneSignalMap = new HashMap<>();

    private RedstoneSignalManager(@NotNull Instance instance) {
        this.instance = instance;
        long id = nextID++;
        instance.setTag(ID, id);
        redstoneSignalManagerById.put(id, this);
    }

    public static @NotNull RedstoneSignalManager of(Instance instance) {
        Long id = instance.getTag(ID);

        if (id == null) {
            return new RedstoneSignalManager(instance);
        }

        return redstoneSignalManagerById.get(id);
    }

    public static boolean remove(Instance instance) {
        Long id = instance.getTag(ID);

        if (id == null) {
            throw new IllegalArgumentException("Argument 'instance' passed to RedstoneSignalManager#remove did not have an associated redstone signal manager.");
        }

        instance.setTag(ID, null);

        return redstoneSignalManagerById.remove(id) != null;
    }

    /**
     * Gets the signal table of this instance.
     * @return the redstone signal table
     */
    public Map<Pos, PriorityQueue<RedstoneSignal>> getSignalMap() {
        return redstoneSignalMap;
    }

    /**
     * Handles this redstone signal's propagation
     *
     * @param redstoneSignal the signal information
     * @param targets the targets to propagate the signal to
     */
    public void handleRedstoneSignal(RedstoneSignal redstoneSignal, @NotNull Point... targets) {
        if (redstoneSignal.strength() == 0) {
            return;
        }

        for (Point target : targets) {
            Block block = instance.getBlock(target);

            if (!(block.handler() instanceof VanillaBlockHandler vanillaBlockHandler)) {
                continue;
            }

            addRedstoneSignal(
                    new RedstoneSignalTarget(
                            instance,
                            target,
                            block
                    ),
                    redstoneSignal
            );

            if (redstoneSignal.type() == RedstoneSignal.Type.HARD) {
                handleRedstoneSignal(
                        redstoneSignal.reduce(),
                        target.add(1, 0, 0),
                        target.add(-1, 0, 0),
                        target.add(0, 1, 0),
                        target.add(0, -1, 0),
                        target.add(0, 0, 1),
                        target.add(0, 0, -1)
                );
            }
        }
    }

    private void addRedstoneSignal(@NotNull RedstoneSignalTarget redstoneSignalTarget, @NotNull RedstoneSignal signalInfo) {
        Pos pos = Pos.fromPoint(redstoneSignalTarget.target());
        Instance instance = redstoneSignalTarget.instance();
        PriorityQueue<RedstoneSignal> cachedRedstoneSignals = redstoneSignalMap.get(pos);

        // Assert that the redstone signal set is not absent
        if (cachedRedstoneSignals == null) {
            cachedRedstoneSignals = new PriorityQueue<>();
            redstoneSignalMap.put(pos, cachedRedstoneSignals);
        }

        RedstoneSignal originalSignal = cachedRedstoneSignals.peek();

        cachedRedstoneSignals.add(signalInfo);

        Block block = instance.getBlock(pos);

        if (!(block.handler() instanceof RedstoneContainerBlockHandler redstoneContainerBlockHandler)) {
            return;
        }

        // Check if peek redstone signal info has changed
        RedstoneSignal newSignal = Objects.requireNonNull(cachedRedstoneSignals.peek());
        if (!newSignal.equals(originalSignal)) {
            // Trigger newRedstoneSignal if applicable
            redstoneContainerBlockHandler.newRedstoneSignal(redstoneSignalTarget, newSignal, originalSignal);
        }
    }

    public void removeRedstoneSignal(RedstoneSignalTarget redstoneSignalTarget, @NotNull RedstoneSignal signalInfo) {
        Pos pos = Pos.fromPoint(redstoneSignalTarget.target());
        Instance instance = redstoneSignalTarget.instance();
        PriorityQueue<RedstoneSignal> cachedRedstoneSignals = redstoneSignalMap.get(pos);

        if (cachedRedstoneSignals == null) {
            return;
        }

        int size = cachedRedstoneSignals.size();

        if (size == 0) {
            return;
        }

        RedstoneSignal originalSignal = cachedRedstoneSignals.peek();

        if (!cachedRedstoneSignals.remove(signalInfo)) {
            return;
        }

        Block block = instance.getBlock(pos);

        if (!(block.handler() instanceof RedstoneContainerBlockHandler redstoneContainerBlockHandler)) {
            return;
        }

        // If list is empty
        if (size == 1) {
            // Activate event
            redstoneContainerBlockHandler.noRedstoneSignal(redstoneSignalTarget, signalInfo);
            return;
        }

        // Check if peek redstone signal info has changed
        RedstoneSignal newSignal = cachedRedstoneSignals.peek();
        if (originalSignal.equals(newSignal)) {
            // Trigger newRedstoneSignal
            redstoneContainerBlockHandler.newRedstoneSignal(redstoneSignalTarget, originalSignal, newSignal);
        }
    }
}
