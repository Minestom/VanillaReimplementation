package net.minestom.vanilla.blocks;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

// TODO: When placing concrete powder in water, it turns to the solid block correctly, however it falls like a regular concrete powder block
public class ConcretePowderBlockHandler extends GravityBlockHandler {
    private final Block solidifiedBlock;

    public ConcretePowderBlockHandler(@NotNull VanillaBlocks.BlockContext context, Block solidifiedBlock) {
        super(context);
        this.solidifiedBlock = solidifiedBlock;
    }

    @Override
    public void onPlace(@NotNull VanillaPlacement placement) {
        super.onPlace(placement);
        tryConvert(placement.instance(), placement.position());
    }

    @Override
    public void tick(Tick tick) {
        tryConvert(tick.getInstance(), tick.getBlockPosition());
    }

    private void tryConvert(Instance instance, Point blockPosition) {

        int x = blockPosition.blockX();
        int y = blockPosition.blockY();
        int z = blockPosition.blockZ();

        // TODO: support block tags

        if (
                instance.getBlock(x, y + 1, z).compare(Block.WATER) || // above
                        instance.getBlock(x - 1, y, z).compare(Block.WATER) || // west
                        instance.getBlock(x + 1, y, z).compare(Block.WATER) || // east
                        instance.getBlock(x, y, z - 1).compare(Block.WATER) || // north
                        instance.getBlock(x, y, z + 1).compare(Block.WATER)    // south
        ) {
            instance.setBlock(blockPosition, solidifiedBlock);
        }
    }
}
