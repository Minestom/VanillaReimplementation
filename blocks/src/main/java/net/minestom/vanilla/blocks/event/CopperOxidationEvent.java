package net.minestom.vanilla.blocks.event;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.event.Event;
import net.minestom.server.event.trait.BlockEvent;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

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
    public Block getBlock() {
        return block;
    }

    @Override
    public BlockVec getBlockPosition() {
        return position;
    }

    @Override
    public Instance getInstance() {
        return instance;
    }
}
