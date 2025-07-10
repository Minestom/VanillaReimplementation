package net.minestom.vanilla.blocks.placement;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;

public class SnowyUpdateRule extends BlockPlacementRule {

    public SnowyUpdateRule(Block block) {
        super(block);
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {
        String snowy = getSnowyState(updateState.instance(), updateState.blockPosition());
        return updateState.currentBlock().withProperty("snowy", snowy);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        String snowy = getSnowyState(placementState.instance(), placementState.placePosition());
        return placementState.block().withProperty("snowy", snowy);
    }

    private String getSnowyState(Block.Getter instance, Point position) {
        Point abovePosition = position.relative(BlockFace.TOP);
        Block aboveBlock = instance.getBlock(abovePosition);
        boolean isSnow = aboveBlock.compare(Block.SNOW) ||
                          aboveBlock.compare(Block.SNOW_BLOCK) ||
                          aboveBlock.compare(Block.POWDER_SNOW);
        return String.valueOf(isSnow);
    }
}
