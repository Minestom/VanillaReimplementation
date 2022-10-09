package net.minestom.vanilla.blocks;

import org.jetbrains.annotations.NotNull;

public class TrappedChestBlockHandler extends ChestLikeBlockHandler {
    // TODO: redstone signal

    public TrappedChestBlockHandler(@NotNull VanillaBlocks.BlockContext context) {
        super(context, 3 * 9);
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
