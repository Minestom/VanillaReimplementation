package net.minestom.vanilla.blocks.update.info;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

public record BlockUpdate(@NotNull Instance instance,
                          @NotNull Point blockPosition,
                          @NotNull Block block,
                          @NotNull BlockUpdateInfo info) {

    public BlockUpdate(
            @NotNull Instance instance,
            @NotNull Point blockPosition,
            @NotNull Block block,
            @NotNull BlockUpdateInfo info
    ) {
        this.instance = instance;
        this.blockPosition = blockPosition;
        this.block = block;
        this.info = info;
    }

    @Override
    public BlockUpdateInfo info() {
        return info;
    }
}
