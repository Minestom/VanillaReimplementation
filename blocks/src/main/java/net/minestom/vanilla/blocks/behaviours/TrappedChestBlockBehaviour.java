package net.minestom.vanilla.blocks.behaviours;

import net.minestom.vanilla.blocks.VanillaBlocks;
import org.jetbrains.annotations.NotNull;

public class TrappedChestBlockBehaviour extends ChestLikeBlockBehaviour {
    // TODO: redstone signal

    public TrappedChestBlockBehaviour(@NotNull VanillaBlocks.BlockContext context) {
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
