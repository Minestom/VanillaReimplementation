package net.minestom.vanilla.blocks.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.utils.Direction;

public class WallCoralPlacementRule extends BlockPlacementRule {

    public WallCoralPlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        return placementState.block();
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {
        Block currentBlock = updateState.currentBlock();
        String facingStr = currentBlock.getProperty("facing");
        if (facingStr == null) {
            return Block.AIR;
        }

        BlockFace facing = BlockFace.fromDirection(
            Direction.valueOf(
                facingStr.toUpperCase()
            )
        );

        Block supportingBlock = updateState.instance().getBlock(updateState.blockPosition().relative(facing.getOppositeFace()));

        if (!supportingBlock.registry().collisionShape().isFaceFull(facing)) {
            return "true".equals(currentBlock.getProperty("waterlogged")) ? Block.WATER : Block.AIR;
        }

        return super.blockUpdate(updateState);
    }
}

