package net.minestom.vanilla.blocks;

import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

public class TrappedChestBlockHandler extends ChestLikeBlockHandler {
    // TODO: redstone signal

    public TrappedChestBlockHandler(@NotNull VanillaBlocks.BlockContext context) {
        super(context);
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
