package net.minestom.vanilla.blocks.redstone;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.vanilla.blocks.redstone.signal.RedstoneSignalManager;
import net.minestom.vanilla.blocks.redstone.signal.info.RedstoneSignal;
import net.minestom.vanilla.blocks.redstone.signal.info.RedstoneSignalTarget;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;

public class RedstoneBlockBlockHandler extends RedstoneContainerBlockHandler {
    public RedstoneBlockBlockHandler() {
        super(Block.REDSTONE_BLOCK);
    }

    @Override
    public void onPlace(@NotNull Placement placement) {
        Instance instance = placement.getInstance();
        Point pos = placement.getBlockPosition();

        RedstoneSignalManager redstoneSignalManager = RedstoneSignalManager.of(instance);
        redstoneSignalManager.handleRedstoneSignal(
                new RedstoneSignal(
                        RedstoneSignal.Type.SOFT,
                        15
                ),
                pos.add(1, 0, 0),
                pos.add(-1, 0, 0),
                pos.add(0, 0, 1),
                pos.add(0, 0, -1),
                pos.add(0, 1, 0),
                pos.add(0, -1, 0)
        );
    }
}
