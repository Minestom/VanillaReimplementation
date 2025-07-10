package net.minestom.vanilla.blocks.placement;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;

public class FarmlandPlacementRule extends BlockPlacementRule {

    public FarmlandPlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {
        Point abovePosition = updateState.blockPosition().relative(BlockFace.TOP);
        Block aboveBlock = updateState.instance().getBlock(abovePosition);

        if (aboveBlock.isSolid()) {
            return Block.DIRT;
        }

        return updateState.currentBlock();
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        return placementState.block();
    }
}
