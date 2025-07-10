package net.minestom.vanilla.blocks.placement;

import net.minestom.server.instance.block.Block;

public class GrindstonePlacementRule extends FacedFacingPlacementRule {

    public GrindstonePlacementRule(Block block) {
        super(block);
    }

    @Override
    public boolean needSupport() {
        return false;
    }
}
