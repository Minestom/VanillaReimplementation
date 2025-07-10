package net.minestom.vanilla.blocks.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;

public class SlabPlacementRule extends BlockPlacementRule {

    public SlabPlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        Block replacingBlock = placementState.instance().getBlock(placementState.placePosition());
        if (replacingBlock.compare(block)) {
            return placementState.block().withProperty("type", "double").withProperty("waterlogged", "false");
        }

        String waterlogged = String.valueOf(replacingBlock.compare(Block.WATER));

        if (placementState.blockFace() == BlockFace.BOTTOM ||
            (placementState.blockFace() != BlockFace.TOP &&
             placementState.cursorPosition() != null &&
             placementState.cursorPosition().y() > 0.5)) {

            return placementState.block().withProperty("type", "top").withProperty("waterlogged", waterlogged);
        }

        return placementState.block().withProperty("type", "bottom").withProperty("waterlogged", waterlogged);
    }

    @Override
    public boolean isSelfReplaceable(Replacement replacement) {
        Block blockToPlace = replacement.material().block();
        Block placedBlock = replacement.block();

        if (!blockToPlace.compare(placedBlock)) {
            return false;
        }

        String type = placedBlock.getProperty("type");
        if (type == null || "double".equals(type)) {
            return false;
        }

        if (replacement.isOffset()) {
            return true;
        }

        if ("top".equals(type) && replacement.blockFace() == BlockFace.BOTTOM) {
            return true;
        }

        if ("bottom".equals(type) && replacement.blockFace() == BlockFace.TOP) {
            return true;
        }

        if ("top".equals(type) && replacement.cursorPosition().y() < 0.5) {
            return true;
        }

        if ("bottom".equals(type) && replacement.cursorPosition().y() > 0.5) {
            return true;
        }

        return false;
    }
}
