package net.minestom.vanilla.blocks;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

public class ConcretePowderBlockHandler extends GravityBlockHandler {
    private final Block solidifiedBlock;

    public ConcretePowderBlockHandler(Block powderBlock, Block solidifiedBlock) {
        super(powderBlock);
        this.solidifiedBlock = solidifiedBlock;
    }

    @Override
    public void onPlace(Placement placement) {
        tryConvert(placement.getInstance(), placement.getBlockPosition());
    }

    @Override
    public void tick(Tick tick) {
        tryConvert(tick.getInstance(), tick.getBlockPosition());
    }

    private void tryConvert(Instance instance, Point blockPosition) {
        Block above = instance.getBlock(blockPosition.blockX(), blockPosition.blockY() + 1, blockPosition.blockZ());
        Block west = instance.getBlock(blockPosition.blockX() - 1, blockPosition.blockY(), blockPosition.blockZ());
        Block east = instance.getBlock(blockPosition.blockX() + 1, blockPosition.blockY(), blockPosition.blockZ());
        Block north = instance.getBlock(blockPosition.blockX(), blockPosition.blockY(), blockPosition.blockZ() - 1);
        Block south = instance.getBlock(blockPosition.blockX(), blockPosition.blockY(), blockPosition.blockZ() + 1);

        // TODO: support block tags
        if (above.compare(Block.WATER)
                || west.compare(Block.WATER)
                || east.compare(Block.WATER)
                || north.compare(Block.WATER)
                || south.compare(Block.WATER)
        ) {
            instance.setBlock(blockPosition, solidifiedBlock);
        }
    }
}
