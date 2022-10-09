package net.minestom.vanilla.blocks;

import org.jetbrains.annotations.NotNull;

public class ChestBlockHandler extends ChestLikeBlockHandler {
    public ChestBlockHandler(@NotNull VanillaBlocks.BlockContext context) {
        super(context, 3 * 9);
    }

    @Override
    public boolean dropContentsOnDestroy() {
        return true;
    }
}
