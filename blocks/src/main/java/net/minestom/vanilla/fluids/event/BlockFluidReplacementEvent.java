package net.minestom.vanilla.fluids.event;

import net.minestom.server.coordinate.BlockVec;
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
public class BlockFluidReplacementEvent implements BlockEvent, InstanceEvent, CancellableEvent {
    private final Instance inst;
    private final Block blk;
    private final BlockVec pos;
    private boolean isCancelled = true;

    public BlockFluidReplacementEvent(Instance inst, Block blk, BlockVec pos) {
        this.inst = inst;
        this.blk = blk;
        this.pos = pos;
    }

    @Override
    public @NotNull Block getBlock() {
        return blk;
    }

    @Override
    public @NotNull BlockVec getBlockPosition() {
        return pos;
    }

    @Override
    public @NotNull Instance getInstance() {
        return inst;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.isCancelled = cancel;
    }
}
