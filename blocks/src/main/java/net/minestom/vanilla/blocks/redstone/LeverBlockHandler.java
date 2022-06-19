package net.minestom.vanilla.blocks.redstone;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.vanilla.blocks.VanillaBlocks;
import net.minestom.vanilla.blocks.redstone.signal.RedstoneSignalManager;
import net.minestom.vanilla.blocks.redstone.signal.info.RedstoneSignal;
import org.jetbrains.annotations.NotNull;

public class LeverBlockHandler extends RedstoneContainerBlockHandler {

    public LeverBlockHandler(@NotNull VanillaBlocks.BlockContext context) {
        super(context);
    }

    @Override
    public boolean onInteract(@NotNull Interaction interaction) {
        Instance instance = interaction.getInstance();
        Point blockPosition = interaction.getBlockPosition();
        Block block = interaction.getBlock();

        boolean isPowered = block.getProperty("powered").equals("true");

        Block newBlock = block.withProperty("powered", String.valueOf(!isPowered));

        instance.setBlock(blockPosition, newBlock);

        return true;
    }

    @Override
    public void onPlace(@NotNull Placement placement) {
        Instance instance = placement.getInstance();
        Point pos = placement.getBlockPosition();
        Block block = placement.getBlock();

        boolean isPowered = block.getProperty("powered").equals("true");

        if (!isPowered) {
            return;
        }

        RedstoneSignalManager redstoneSignalManager = RedstoneSignalManager.of(instance);
        redstoneSignalManager.handleRedstoneSignal(
                new RedstoneSignal(
                        RedstoneSignal.Type.SOFT,
                        15
                ),
                pos.add(1, 0, 0),
                pos.add(-1, 0, 0),
                pos.add(0, 0, 1),
                pos.add(0, 0, -1)
        );
    }
}
