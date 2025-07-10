package net.minestom.vanilla.blocks.placement;

import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.utils.Direction;
import net.minestom.vanilla.common.utils.DirectionUtils;

public class BedPlacementRule extends BlockPlacementRule {

    public BedPlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        Direction direction = DirectionUtils.getNearestHorizontalLookingDirection(placementState).opposite();
        var additionalReplacementBlock = placementState.placePosition().add(direction.vec());

        if (!placementState.instance().getBlock(additionalReplacementBlock).registry().isReplaceable()) {
            return null;
        }

        Instance instance = (Instance) placementState.instance();

        instance.setBlock(
            additionalReplacementBlock,
            placementState.block()
                .withProperty("facing", direction.name().toLowerCase())
                .withProperty("part", "head")
        );

        return placementState.block()
            .withProperty("facing", direction.name().toLowerCase())
            .withProperty("part", "foot");
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {
        Direction facing = Direction.valueOf(
            updateState.currentBlock()
                .getProperty("facing")
                .toUpperCase()
        );
        BlockFace neighbourFacing;
        String neighbourPart;

        if ("foot".equals(updateState.currentBlock().getProperty("part"))) {
            neighbourFacing = BlockFace.fromDirection(facing);
            neighbourPart = "head";
        } else {
            neighbourFacing = BlockFace.fromDirection(facing).getOppositeFace();
            neighbourPart = "foot";
        }

        Block neighbour = updateState.instance().getBlock(updateState.blockPosition().relative(neighbourFacing));
        if (!neighbour.compare(block, Block.Comparator.ID)) {
            return Block.AIR;
        }

        String realNeighbourPart = neighbour.getProperty("part");
        if (!neighbourPart.equals(realNeighbourPart)) {
            return Block.AIR;
        }

        return updateState.currentBlock();
    }
}
