package net.minestom.vanilla.blocks;

import net.minestom.server.instance.block.Block;

public class TrappedChestBlockHandler extends ChestLikeBlockHandler {
    // TODO: redstone signal

    public TrappedChestBlockHandler() {
        super(Block.TRAPPED_CHEST);
    }

//    @Override
//    protected BlockPropertyList createPropertyValues() {
//        return super.createPropertyValues().property("type", "single", "left", "right");
//    }

    @Override
    public boolean dropContentsOnDestroy() {
        return true;
    }


}
