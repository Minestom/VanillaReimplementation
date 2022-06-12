package net.minestom.vanilla.blocks;

import net.minestom.server.instance.block.Block;

public class ChestBlockHandler extends ChestLikeBlockHandler {
    public ChestBlockHandler() {
        super(Block.CHEST);
    }

    @Override
    public boolean dropContentsOnDestroy() {
        return true;
    }
}
