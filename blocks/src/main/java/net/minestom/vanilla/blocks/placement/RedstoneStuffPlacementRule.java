package net.minestom.vanilla.blocks.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.utils.Direction;
import net.minestom.vanilla.common.item.DroppedItemFactory;
import net.minestom.vanilla.common.utils.DirectionUtils;

import java.util.Locale;

public class RedstoneStuffPlacementRule extends BlockPlacementRule {

    public RedstoneStuffPlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        var supportPosition = placementState.placePosition().relative(BlockFace.BOTTOM);
        var supportBlock = placementState.instance().getBlock(supportPosition);

        if (supportBlock.isAir() || supportBlock.compare(block)) {
            return null;
        }

        Direction facing = DirectionUtils.getNearestHorizontalLookingDirection(placementState);
        return block
            .withProperty("facing", facing.name().toLowerCase(Locale.ROOT))
            .withProperty("powered", "false");
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {
        var supportPosition = updateState.blockPosition().relative(BlockFace.BOTTOM);
        var supportBlock = updateState.instance().getBlock(supportPosition);

        if (supportBlock.isAir()) {
            DroppedItemFactory.maybeDrop(updateState);
            return Block.AIR;
        }
        return updateState.currentBlock();
    }
}
