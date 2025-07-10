package net.minestom.vanilla.blocks.placement;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.utils.Direction;
import net.minestom.vanilla.common.item.DroppedItemFactory;
import net.minestom.vanilla.common.utils.DirectionUtils;

import static java.lang.Math.abs;

public class DoorPlacementRule extends BlockPlacementRule {

    public DoorPlacementRule(Block baseDoorBlock) {
        super(baseDoorBlock);
    }

    private int countSolidFaces(Instance instance, Point centerPos, BlockFace horizontalDirection) {
        int solidFaces = 0;

        if (instance.getBlock(centerPos.relative(horizontalDirection)).isSolid()) {
            solidFaces++;
        }

        Direction directionVector = horizontalDirection.toDirection();

        BlockFace diagClockwiseDir = BlockFace.fromDirection(DirectionUtils.rotateR(directionVector));
        if (instance.getBlock(centerPos.relative(diagClockwiseDir)).isSolid()) {
            solidFaces++;
        }

        BlockFace diagCounterClockwiseDir = BlockFace.fromDirection(DirectionUtils.rotateL(directionVector));
        if (instance.getBlock(centerPos.relative(diagCounterClockwiseDir)).isSolid()) {
            solidFaces++;
        }

        return solidFaces;
    }

    private String getHingeSide(Instance instance, Point placePos, Pos playerPos, BlockFace playerFacing) {
        BlockFace doorFrontDirection = playerFacing.getOppositeFace();

        BlockFace leftOfDoor = BlockFace.fromDirection(DirectionUtils.rotateL(doorFrontDirection.toDirection()));
        BlockFace rightOfDoor = BlockFace.fromDirection(DirectionUtils.rotateR(doorFrontDirection.toDirection()));

        Point leftBlockPos = placePos.relative(leftOfDoor);
        Point rightBlockPos = placePos.relative(rightOfDoor);

        Block leftNeighborBlock = instance.getBlock(leftBlockPos);
        Block rightNeighborBlock = instance.getBlock(rightBlockPos);

        if (leftNeighborBlock.key().equals(block.key())) {
            String existingDoorHalf = leftNeighborBlock.getProperty("half");
            String existingDoorHinge = leftNeighborBlock.getProperty("hinge");

            if ("lower".equals(existingDoorHalf)) {
                if ("right".equals(existingDoorHinge)) {
                    return "left";
                }
            }
        }

        if (rightNeighborBlock.key().equals(block.key())) {
            String existingDoorHalf = rightNeighborBlock.getProperty("half");
            String existingDoorHinge = rightNeighborBlock.getProperty("hinge");

            if ("lower".equals(existingDoorHalf)) {
                if ("left".equals(existingDoorHinge)) {
                    return "right";
                }
            }
        }

        BlockFace playerRightSideBlockFace = BlockFace.fromDirection(DirectionUtils.rotateR(playerFacing.toDirection()));
        BlockFace playerLeftSideBlockFace = BlockFace.fromDirection(DirectionUtils.rotateL(playerFacing.toDirection()));

        int leftSupportScore = countSolidFaces(instance, placePos, playerLeftSideBlockFace);
        int rightSupportScore = countSolidFaces(instance, placePos, playerRightSideBlockFace);

        if (leftSupportScore > rightSupportScore) {
            return "left";
        }
        if (rightSupportScore > leftSupportScore) {
            return "right";
        }

        float playerYaw = playerPos.yaw();

        float yawToRightHandleSide = DirectionUtils.getYaw(rightOfDoor.toDirection());
        float yawToLeftHandleSide = DirectionUtils.getYaw(leftOfDoor.toDirection());

        float normalizedPlayerYaw = normalizeYaw(playerYaw);
        float normalizedYawToRightHandle = normalizeYaw(yawToRightHandleSide);
        float normalizedYawToLeftHandle = normalizeYaw(yawToLeftHandleSide);

        float diffToRightHandle = abs(normalizedPlayerYaw - normalizedYawToRightHandle);
        float diffToLeftHandle = abs(normalizedPlayerYaw - normalizedYawToLeftHandle);

        float finalDiffToRightHandle = Math.min(diffToRightHandle, abs(diffToRightHandle - 360));
        float finalDiffToLeftHandle = Math.min(diffToLeftHandle, abs(diffToLeftHandle - 360));

        return finalDiffToRightHandle < finalDiffToLeftHandle ? "left" : "right";
    }

    private float normalizeYaw(float yaw) {
        float normalized = yaw % 360;
        if (normalized > 180) normalized -= 360;
        if (normalized < -180) normalized += 360;
        return normalized;
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        Block.Getter instance = placementState.instance();
        Point placePos = placementState.placePosition();

        Point upperPos = placePos.add(0.0, 1.0, 0.0);
        if (!instance.getBlock(upperPos).registry().isReplaceable()) {
            return null;
        }

        Point lowerPos = placePos.sub(0.0, 1.0, 0.0);
        if (!instance.getBlock(lowerPos).registry().collisionShape().isFaceFull(BlockFace.TOP)) {
            return null;
        }

        Direction facing = DirectionUtils.getNearestHorizontalLookingDirection(placementState).opposite();

        String hinge = getHingeSide(
            (Instance) instance,
            placePos,
            (Pos) placementState.playerPosition(),
            BlockFace.fromDirection(facing)
        );

        String open = "false";
        String powered = "false";

        Block lowerDoorBlock = placementState.block()
            .withProperty("facing", facing.name().toLowerCase())
            .withProperty("half", "lower")
            .withProperty("hinge", hinge)
            .withProperty("open", open)
            .withProperty("powered", powered);

        Block upperDoorBlock = placementState.block()
            .withProperty("facing", facing.name().toLowerCase())
            .withProperty("half", "upper")
            .withProperty("hinge", hinge)
            .withProperty("open", open)
            .withProperty("powered", powered);

        ((Instance) instance).setBlock(upperPos, upperDoorBlock);

        return lowerDoorBlock;
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {
        Block.Getter instance = updateState.instance();
        Block currentBlock = updateState.currentBlock();
        Point blockPosition = updateState.blockPosition();

        String half = currentBlock.getProperty("half");

        Point neighborPos;
        String expectedOtherHalf;

        if ("lower".equals(half)) {
            neighborPos = blockPosition.add(0.0, 1.0, 0.0);
            expectedOtherHalf = "upper";
        } else { // half == "upper"
            neighborPos = blockPosition.sub(0.0, 1.0, 0.0);
            expectedOtherHalf = "lower";
        }

        Block neighborBlock = instance.getBlock(neighborPos);

        if (!neighborBlock.compare(updateState.currentBlock()) ||
            !expectedOtherHalf.equals(neighborBlock.getProperty("half"))) {

            if (neighborBlock.compare(updateState.currentBlock())) {
                ((Instance) instance).setBlock(neighborPos, Block.AIR);
            }
            return Block.AIR;
        }

        Block blockBelow = instance.getBlock(blockPosition.relative(BlockFace.BOTTOM));
        if ("lower".equals(updateState.currentBlock().getProperty("half")) &&
            !blockBelow.registry().collisionShape().isFaceFull(BlockFace.TOP)) {

            DroppedItemFactory.maybeDrop(updateState);
            Instance realInstance = (Instance) instance;
            realInstance.setBlock(neighborPos, Block.AIR);
            realInstance.setBlock(updateState.blockPosition(), Block.AIR);
        }

        return updateState.currentBlock();
    }
}
