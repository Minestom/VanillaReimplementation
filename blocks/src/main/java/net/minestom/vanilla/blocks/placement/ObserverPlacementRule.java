package net.minestom.vanilla.blocks.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;

public class ObserverPlacementRule extends BlockPlacementRule {

    public ObserverPlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        if (placementState.blockFace() == null) {
            return placementState.block();
        }

        return placementState.block()
            .withProperty("facing", placementState.blockFace().name().toLowerCase())
            .withProperty("powered", "false");
    }
}
