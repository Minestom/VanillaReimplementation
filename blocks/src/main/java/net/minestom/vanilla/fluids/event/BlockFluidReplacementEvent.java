package net.minestom.vanilla.fluids.event;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.event.trait.BlockEvent;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

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
    public Block getBlock() {
        return blk;
    }

    @Override
    public BlockVec getBlockPosition() {
        return pos;
    }

    @Override
    public Instance getInstance() {
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
