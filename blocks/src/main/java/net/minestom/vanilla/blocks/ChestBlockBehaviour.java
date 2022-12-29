package net.minestom.vanilla.blocks;

import org.jetbrains.annotations.NotNull;

public class ChestBlockBehaviour extends ChestLikeBlockBehaviour {
    public ChestBlockBehaviour(@NotNull VanillaBlocks.BlockContext context) {
        super(context, 3 * 9);
    }

    @Override
    public boolean dropContentsOnDestroy() {
        return true;
    }
}
