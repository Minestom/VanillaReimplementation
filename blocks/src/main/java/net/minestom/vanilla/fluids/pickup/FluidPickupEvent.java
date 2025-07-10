package net.minestom.vanilla.fluids.pickup;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

/**
 * This file contains code ported from Kotlin to Java, adapted from the Blocks and Stuff project.
 * Original source: https://github.com/everbuild-org/blocks-and-stuff
 * <p>
 * Original authors: ChrisB, AEinNico, CreepyX
 * <p>
 * Ported from Kotlin to Java and adapted for use in this project with modifications.
 */
public class FluidPickupEvent implements CancellableEvent, PlayerInstanceEvent {
    private final Instance instance;
    private final Player player;
    private final Block sourceBlock;
    private final BlockVec sourceBlockPosition;
    private Block blockToPlace;
    private boolean isCancelled = false;

    public FluidPickupEvent(
        Instance instance,
        Player player,
        Block sourceBlock,
        BlockVec sourceBlockPosition,
        Block blockToPlace
    ) {
        this.instance = instance;
        this.player = player;
        this.sourceBlock = sourceBlock;
        this.sourceBlockPosition = sourceBlockPosition;
        this.blockToPlace = blockToPlace;
    }

    public Block getSourceBlock() {
        return sourceBlock;
    }

    public BlockVec getSourceBlockPosition() {
        return sourceBlockPosition;
    }

    public Block getBlockToPlace() {
        return blockToPlace;
    }

    public void setBlockToPlace(Block blockToPlace) {
        this.blockToPlace = blockToPlace;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.isCancelled = cancel;
    }

    @Override
    public @NotNull Instance getInstance() {
        return instance;
    }

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }
}
