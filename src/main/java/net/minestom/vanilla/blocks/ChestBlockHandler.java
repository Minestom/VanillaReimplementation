package net.minestom.vanilla.blocks;

import net.minestom.server.instance.block.Block;

public class ChestBlockHandler extends ChestLikeBlockHandler {
    public ChestBlockHandler() {
        super(Block.CHEST);
    }

//    @Override
//    protected BlockPropertyList createPropertyValues() {
//        return super.createPropertyValues().property("type", "single", "left", "right");
//    }

    @Override
    public boolean dropContentsOnDestroy() {
        return true;
    }

//    @Override
//    public Data createData(Instance instance, BlockPosition blockPosition, Data data) {
//        return new ChestBlockEntity(blockPosition);
//    }
}
