package net.minestom.vanilla.blocks.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.NotNull;

/**
 * This file contains code ported from Kotlin to Java, adapted from the Blocks and Stuff project.
 * Original source: https://github.com/everbuild-org/blocks-and-stuff
 * <p>
 * Original authors: ChrisB, AEinNico, CreepyX
 * <p>
 * Ported from Kotlin to Java and adapted for use in this project with modifications.
 */
public class WallCoralPlacementRule extends BlockPlacementRule {

    public WallCoralPlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        return placementState.block();
    }

    @Override
    public @NotNull Block blockUpdate(UpdateState updateState) {
        Block currentBlock = updateState.currentBlock();
        String facingStr = currentBlock.getProperty("facing");
        if (facingStr == null) {
            return Block.AIR;
        }

        BlockFace facing = BlockFace.fromDirection(
            Direction.valueOf(
                facingStr.toUpperCase()
            )
        );

        Block supportingBlock = updateState.instance().getBlock(updateState.blockPosition().relative(facing.getOppositeFace()));

        if (!supportingBlock.registry().collisionShape().isFaceFull(facing)) {
            return "true".equals(currentBlock.getProperty("waterlogged")) ? Block.WATER : Block.AIR;
        }

        return super.blockUpdate(updateState);
    }
}

