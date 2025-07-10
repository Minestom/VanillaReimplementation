package net.minestom.vanilla.blocks.placement;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.vanilla.common.item.DroppedItemFactory;

public class TallFlowerPlacementRule extends BlockPlacementRule {

    public TallFlowerPlacementRule(Block baseFlowerBlock) {
        super(baseFlowerBlock);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        Block.Getter instance = placementState.instance();
        Point placePos = placementState.placePosition();

        Point upperPos = placePos.add(0.0, 1.0, 0.0);
        if (!instance.getBlock(upperPos).registry().isReplaceable()) {
            return null;
        }

        Point lowerPos = placePos.sub(0.0, 1.0, 0.0);
        if (!instance.getBlock(lowerPos).registry().collisionShape().isFaceFull(BlockFace.TOP)) {
            return null;
        }

        Block lowerFlowerBlock = placementState.block()
            .withProperty("half", "lower");

        Block upperFlowerBlock = placementState.block()
            .withProperty("half", "upper");

        ((Instance) instance).setBlock(upperPos, upperFlowerBlock);

        return lowerFlowerBlock;
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {
        Block.Getter instance = updateState.instance();
        Block currentBlock = updateState.currentBlock();
        Point blockPosition = updateState.blockPosition();

        String half = currentBlock.getProperty("half");

        Point neighborPos;
        String expectedOtherHalf;

        if ("lower".equals(half)) {
            neighborPos = blockPosition.add(0.0, 1.0, 0.0);
            expectedOtherHalf = "upper";
        } else { // half == "upper"
            neighborPos = blockPosition.sub(0.0, 1.0, 0.0);
            expectedOtherHalf = "lower";
        }

        Block neighborBlock = instance.getBlock(neighborPos);

        if (!neighborBlock.compare(updateState.currentBlock()) ||
            !expectedOtherHalf.equals(neighborBlock.getProperty("half"))) {

            if (neighborBlock.compare(updateState.currentBlock())) {
                ((Instance) instance).setBlock(neighborPos, Block.AIR);
            }
            return Block.AIR;
        }

        Block blockBelow = instance.getBlock(blockPosition.relative(BlockFace.BOTTOM));
        if ("lower".equals(updateState.currentBlock().getProperty("half")) &&
            !blockBelow.registry().collisionShape().isFaceFull(BlockFace.TOP)) {

            DroppedItemFactory.maybeDrop(updateState);
            ((Instance) instance).setBlock(neighborPos, Block.AIR);
            ((Instance) instance).setBlock(updateState.blockPosition(), Block.AIR);
        }

        return updateState.currentBlock();
    }
}
