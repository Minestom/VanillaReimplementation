package net.minestom.vanilla.blocks.placement;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.utils.Direction;
import net.minestom.vanilla.blocks.placement.util.States;
import net.minestom.vanilla.common.utils.FluidUtils;
import net.minestom.vanilla.fluids.common.FluidState;
import net.minestom.vanilla.fluids.common.WaterlogHandler;
import net.minestom.vanilla.fluids.placement.FluidPlacementRule;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Map;

/**
 * This file contains code ported from Kotlin to Java, adapted from the Blocks and Stuff project.
 * Original source: https://github.com/everbuild-org/blocks-and-stuff
 * <p>
 * Original authors: ChrisB, AEinNico, CreepyX
 * <p>
 * Ported from Kotlin to Java and adapted for use in this project with modifications.
 */
public class StairsPlacementRule extends BlockPlacementRule {

    public StairsPlacementRule(Block block) {
        super(block);
    }

    @Override
    public @NotNull Block blockUpdate(UpdateState updateState) {
        return updateState.currentBlock().withProperty(
            States.SHAPE,
            getShape(updateState.instance(), updateState.currentBlock(), updateState.blockPosition())
        );
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        BlockFace placementFace = placementState.blockFace();
        Point placementPos = placementState.placePosition();
        Vec cursorPos = placementState.cursorPosition() != null ? (Vec) placementState.cursorPosition() : Vec.ZERO;
        Pos playerPos = placementState.playerPosition() != null ? placementState.playerPosition() : Pos.ZERO;

        BlockFace half;
        if (placementFace == BlockFace.BOTTOM ||
            (placementFace != BlockFace.TOP && cursorPos.y() > 0.5)) {
            half = BlockFace.TOP;
        } else {
            half = BlockFace.BOTTOM;
        }

        BlockFace facing = BlockFace.fromYaw(playerPos.yaw());

        Block resultBlock = this.block.withProperties(
            Map.of(
                States.HALF, half.name().toLowerCase(Locale.getDefault()),
                States.FACING, facing.name().toLowerCase(Locale.getDefault())
            )
        );

        resultBlock = resultBlock.withProperty(
            States.SHAPE,
            getShape(placementState.instance(), resultBlock, placementPos)
        );

        boolean isInWater = FluidUtils.isWater(placementState.instance().getBlock(placementPos));
        return resultBlock.withProperty("waterlogged", String.valueOf(isInWater));
    }

    private String getShape(Block.Getter instance, Block block, Point blockPos) {
        Direction direction = States.getFacing(block).toDirection();
        Block offsetBlock = instance.getBlock(
            blockPos.add(
                direction.normalX(),
                direction.normalY(),
                direction.normalZ()
            )
        );

        Direction offsetDirection = States.getFacing(offsetBlock).toDirection();
        Block oppositeOffsetBlock = instance.getBlock(
            blockPos.add(
                direction.opposite().normalX(),
                direction.opposite().normalY(),
                direction.opposite().normalZ()
            )
        );

        Direction oppositeOffsetDirection = States.getFacing(oppositeOffsetBlock).toDirection();

        if (isStairs(offsetBlock)
            && States.getHalf(block) == States.getHalf(offsetBlock)
            && States.getAxis(offsetDirection) != States.getAxis(direction)
            && isDifferentOrientation(instance, block, blockPos, offsetDirection.opposite())) {

            if (offsetDirection == States.rotateYCounterclockwise(direction)) {
                return "outer_left";
            } else {
                return "outer_right";
            }
        }

        if (isStairs(oppositeOffsetBlock)
            && States.getHalf(block) == States.getHalf(oppositeOffsetBlock)
            && States.getAxis(oppositeOffsetDirection) != States.getAxis(direction)
            && isDifferentOrientation(instance, block, blockPos, oppositeOffsetDirection)) {

            if (oppositeOffsetDirection == States.rotateYCounterclockwise(direction)) {
                return "inner_left";
            } else {
                return "inner_right";
            }
        }

        return "straight";
    }

    private boolean isDifferentOrientation(
        Block.Getter instance,
        Block block,
        Point blockPos,
        Direction direction
    ) {
        BlockFace facing = States.getFacing(block);
        BlockFace half = States.getHalf(block);
        Block instanceBlock = instance.getBlock(
            blockPos.add(
                direction.normalX(),
                direction.normalY(),
                direction.normalZ()
            )
        );

        BlockFace instanceBlockFacing = States.getFacing(instanceBlock);
        BlockFace instanceBlockHalf = States.getHalf(instanceBlock);

        return !isStairs(instanceBlock) || instanceBlockFacing != facing || instanceBlockHalf != half;
    }

    private boolean isStairs(Block block) {
        return block.name().endsWith("_stairs");
    }
}
