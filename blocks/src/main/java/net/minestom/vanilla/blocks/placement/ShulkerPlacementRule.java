package net.minestom.vanilla.blocks.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;

public class ShulkerPlacementRule extends BlockPlacementRule {

    public ShulkerPlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        BlockFace facing = determineFacing(placementState);
        return placementState.block().withProperty("facing", facing.toDirection().name().toLowerCase());
    }

    private BlockFace determineFacing(PlacementState placementState) {
        BlockFace blockFace = placementState.blockFace();
        if (blockFace != null) {
            return blockFace;
        }
        return BlockFace.NORTH;
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {
        return updateState.currentBlock();
    }
}
