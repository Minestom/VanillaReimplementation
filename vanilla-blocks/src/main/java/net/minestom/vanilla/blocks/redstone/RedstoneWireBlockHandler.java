package net.minestom.vanilla.blocks.redstone;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.vanilla.blocks.redstone.signal.RedstoneSignalManager;
import net.minestom.vanilla.blocks.redstone.signal.info.RedstoneSignal;
import net.minestom.vanilla.blocks.redstone.signal.info.RedstoneSignalTarget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RedstoneWireBlockHandler extends RedstoneContainerBlockHandler {
    public RedstoneWireBlockHandler() {
        super(Block.REDSTONE_WIRE);
    }

    @Override
    public void newRedstoneSignal(
            @NotNull RedstoneSignalTarget redstoneSignalTarget,
            @NotNull RedstoneSignal newRedstoneSignal,
            @Nullable RedstoneSignal oldRedstoneSignal
    ) {
        Instance instance = redstoneSignalTarget.instance();
        Point pos = redstoneSignalTarget.target();

        RedstoneSignalManager redstoneSignalManager = RedstoneSignalManager.of(instance);
        redstoneSignalManager.handleRedstoneSignal(
                newRedstoneSignal.reduce(),
                pos.add(1, 0, 0),
                pos.add(-1, 0, 0),
                pos.add(0, 0, 1),
                pos.add(0, 0, -1),
                pos.add(0, -1, 0)
        );
    }
}
