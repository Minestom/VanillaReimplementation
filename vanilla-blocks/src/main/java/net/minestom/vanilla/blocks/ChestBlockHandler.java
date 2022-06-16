package net.minestom.vanilla.blocks;

import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

public class ChestBlockHandler extends ChestLikeBlockHandler {
    public ChestBlockHandler(@NotNull VanillaBlocks.BlockContext context) {
        super(context);
    }

    @Override
    public boolean dropContentsOnDestroy() {
        return true;
    }
}
