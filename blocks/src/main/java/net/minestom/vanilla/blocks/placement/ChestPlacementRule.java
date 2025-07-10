package net.minestom.vanilla.blocks.placement;

import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.utils.Direction;
import net.minestom.vanilla.common.utils.DirectionUtils;
import net.minestom.vanilla.common.utils.FluidUtils;

import java.util.Arrays;
import java.util.List;

public class ChestPlacementRule extends BlockPlacementRule {

    public ChestPlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        Block clicked = placementState.instance().getBlock(
            placementState.placePosition().relative(placementState.blockFace().getOppositeFace())
        );

        Direction clickedFacing = getClickedChestFacing(placementState, clicked);
        Direction facing = clickedFacing != null ? clickedFacing :
                           DirectionUtils.getNearestHorizontalLookingDirection(placementState);

        boolean waterlogged = FluidUtils.isWater(placementState.instance().getBlock(placementState.placePosition()));
        Block facedBlock = placementState.block()
            .withProperty("facing", facing.toString().toLowerCase())
            .withProperty("waterlogged", String.valueOf(waterlogged));

        if (placementState.isPlayerShifting() && clickedFacing == null) {
            return facedBlock;
        }

        if (clickedFacing != null &&
            canConnect(placementState, facing, placementState.blockFace().getOppositeFace().toDirection())) {
            return connect(
                placementState,
                facing,
                placementState.blockFace().getOppositeFace().toDirection(),
                facedBlock
            );
        }

        if (canConnect(placementState, facing, DirectionUtils.rotateR(facing))) {
            return connect(
                placementState,
                facing,
                DirectionUtils.rotateR(facing),
                facedBlock
            );
        }

        if (canConnect(placementState, facing, DirectionUtils.rotateL(facing))) {
            return connect(
                placementState,
                facing,
                DirectionUtils.rotateL(facing),
                facedBlock
            );
        }

        return facedBlock;
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {
        String type = updateState.currentBlock().getProperty("type");
        if ("single".equals(type)) {
            return super.blockUpdate(updateState);
        }

        Direction facing = Direction.valueOf(updateState.currentBlock().getProperty("facing").toUpperCase());

        Direction neighbourPosition;
        String expectedNeighbourType;

        if ("left".equals(type)) {
            neighbourPosition = DirectionUtils.rotateR(facing);
            expectedNeighbourType = "right";
        } else {
            neighbourPosition = DirectionUtils.rotateL(facing);
            expectedNeighbourType = "left";
        }

        BlockFace neighbourBlockFace = BlockFace.fromDirection(neighbourPosition);
        Block neighbourBlock = updateState.instance().getBlock(updateState.blockPosition().relative(neighbourBlockFace));

        if (!neighbourBlock.compare(block, Block.Comparator.ID)
            || !neighbourBlock.getProperty("facing").equals(facing.toString().toLowerCase())
            || !neighbourBlock.getProperty("type").equals(expectedNeighbourType)
        ) {
            return updateState.currentBlock()
                .withProperty("type", "single");
        }

        return super.blockUpdate(updateState);
    }

    private Direction getClickedChestFacing(PlacementState placementState, Block clicked) {
        if (!clicked.compare(block, Block.Comparator.ID)) {
            return null;
        }

        Direction facing = Direction.valueOf(clicked.getProperty("facing").toUpperCase());
        Direction rotateLeft = DirectionUtils.rotateL(facing);
        Direction rotateRight = DirectionUtils.rotateR(facing);

        List<BlockFace> lrFaces = Arrays.asList(
            BlockFace.fromDirection(rotateLeft),
            BlockFace.fromDirection(rotateRight)
        );

        return lrFaces.contains(placementState.blockFace()) ? facing : null;
    }

    private boolean canConnect(PlacementState placementState, Direction facingSelf, Direction connecting) {
        Block currentBlock = placementState.instance().getBlock(
            placementState.placePosition().relative(BlockFace.fromDirection(connecting))
        );

        if (!currentBlock.compare(block, Block.Comparator.ID)) {
            return false;
        }

        Direction facing = Direction.valueOf(currentBlock.getProperty("facing").toUpperCase());
        if (facing != facingSelf) {
            return false;
        }

        String type = currentBlock.getProperty("type");
        return "single".equals(type);
    }

    private Block connect(PlacementState placementState, Direction facingSelf, Direction connecting, Block facedBlock) {
        String selfType;
        String otherType;

        if (connecting == DirectionUtils.rotateL(facingSelf)) {
            selfType = "right";
            otherType = "left";
        } else {
            selfType = "left";
            otherType = "right";
        }

        Block connectingBlock = placementState.instance().getBlock(
            placementState.placePosition().relative(BlockFace.fromDirection(connecting))
        );

        ((Instance) placementState.instance()).setBlock(
            placementState.placePosition().relative(BlockFace.fromDirection(connecting)),
            connectingBlock.withProperty("type", otherType)
        );

        return facedBlock.withProperty("type", selfType);
    }
}
