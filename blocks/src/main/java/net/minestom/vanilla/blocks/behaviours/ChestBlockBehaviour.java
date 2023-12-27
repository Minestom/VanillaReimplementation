package net.minestom.vanilla.blocks.behaviours;

import net.minestom.vanilla.blocks.VanillaBlocks;
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
