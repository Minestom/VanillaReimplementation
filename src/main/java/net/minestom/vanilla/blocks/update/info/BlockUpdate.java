package net.minestom.vanilla.blocks.update.info;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

public final class BlockUpdate {

    private final @NotNull Instance instance;
    private final @NotNull Point blockPosition;
    private final @NotNull Block block;
    private final @NotNull BlockUpdateInfo info;

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

    public Instance instance() {
        return instance;
    }

    public Point blockPosition() {
        return blockPosition;
    }

    public Block block() {
        return block;
    }

    public BlockUpdateInfo info() {
        return info;
    }
}
