package net.minestom.vanilla.blocks.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.vanilla.common.utils.BlockUtil;
import net.minestom.vanilla.common.utils.FluidUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * This file contains code ported from Kotlin to Java, adapted from the Blocks and Stuff project.
 * Original source: https://github.com/everbuild-org/blocks-and-stuff
 * <p>
 * Original authors: ChrisB, AEinNico, CreepyX
 * <p>
 * Ported from Kotlin to Java and adapted for use in this project with modifications.
 */
public class CoralPlacementRule extends BlockPlacementRule {
    private static final Map<Block, Block> WALL_CORALS = new HashMap<>();

    static {
        WALL_CORALS.put(Block.TUBE_CORAL_FAN, Block.TUBE_CORAL_WALL_FAN);
        WALL_CORALS.put(Block.BRAIN_CORAL_FAN, Block.BRAIN_CORAL_WALL_FAN);
        WALL_CORALS.put(Block.BUBBLE_CORAL_FAN, Block.BUBBLE_CORAL_WALL_FAN);
        WALL_CORALS.put(Block.FIRE_CORAL_FAN, Block.FIRE_CORAL_WALL_FAN);
        WALL_CORALS.put(Block.HORN_CORAL_FAN, Block.HORN_CORAL_WALL_FAN);
        WALL_CORALS.put(Block.DEAD_TUBE_CORAL_FAN, Block.DEAD_TUBE_CORAL_WALL_FAN);
        WALL_CORALS.put(Block.DEAD_BRAIN_CORAL_FAN, Block.DEAD_BRAIN_CORAL_WALL_FAN);
        WALL_CORALS.put(Block.DEAD_BUBBLE_CORAL_FAN, Block.DEAD_BUBBLE_CORAL_WALL_FAN);
        WALL_CORALS.put(Block.DEAD_FIRE_CORAL_FAN, Block.DEAD_FIRE_CORAL_WALL_FAN);
        WALL_CORALS.put(Block.DEAD_HORN_CORAL_FAN, Block.DEAD_HORN_CORAL_WALL_FAN);
    }

    public CoralPlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        String waterlogged = String.valueOf(FluidUtils.isWater(placementState.instance().getBlock(placementState.placePosition())));

        if (placementState.blockFace().toDirection().horizontal() &&
            placementState.instance().getBlock(placementState.placePosition().relative(placementState.blockFace().getOppositeFace()))
                .registry().collisionShape().isFaceFull(placementState.blockFace())) {

            Block wallCoralBlock = WALL_CORALS.get(block);
            if (wallCoralBlock != null) {
                return BlockUtil.withDefaultHandler(wallCoralBlock)
                    .withProperty("facing", placementState.blockFace().toDirection().toString().toLowerCase())
                    .withProperty("waterlogged", waterlogged);
            }
        }

        if (!placementState.instance().getBlock(placementState.placePosition().relative(BlockFace.BOTTOM)).registry()
                .collisionShape().isFaceFull(BlockFace.TOP)) {
            return null;
        }

        return block.withProperty("waterlogged", waterlogged);
    }

    @Override
    public @NotNull Block blockUpdate(UpdateState updateState) {
        if (!updateState.instance().getBlock(updateState.blockPosition().relative(BlockFace.BOTTOM)).registry()
                .collisionShape().isFaceFull(BlockFace.TOP)) {
            return "true".equals(updateState.currentBlock().getProperty("waterlogged")) ? Block.WATER : Block.AIR;
        }

        return super.blockUpdate(updateState);
    }
}
