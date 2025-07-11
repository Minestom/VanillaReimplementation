package net.minestom.vanilla.blocks.event;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.event.Event;
import net.minestom.server.event.trait.BlockEvent;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.InstanceEvent;
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
public class CopperOxidationEvent implements Event, CancellableEvent, BlockEvent, InstanceEvent {
    private final Block block;
    private Block blockAfterOxidation;
    private final BlockVec position;
    private final Instance instance;
    private boolean cancelled = false;

    public CopperOxidationEvent(
        Block block,
        Block blockAfterOxidation,
        BlockVec position,
        Instance instance
    ) {
        this.block = block;
        this.blockAfterOxidation = blockAfterOxidation;
        this.position = position;
        this.instance = instance;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    public Block getBlockAfterOxidation() {
        return blockAfterOxidation;
    }

    public void setBlockAfterOxidation(Block block) {
        this.blockAfterOxidation = block;
    }

    @Override
    public @NotNull Block getBlock() {
        return block;
    }

    @Override
    public @NotNull BlockVec getBlockPosition() {
        return position;
    }

    @Override
    public @NotNull Instance getInstance() {
        return instance;
    }
}
