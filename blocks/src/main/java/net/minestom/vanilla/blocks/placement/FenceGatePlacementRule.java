package net.minestom.vanilla.blocks.placement;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.registry.RegistryTag;
import net.minestom.server.registry.TagKey;
import net.minestom.server.utils.Direction;
import net.minestom.vanilla.common.utils.DirectionUtils;
import org.jetbrains.annotations.NotNull;

/**
 * This file contains code ported from Kotlin to Java, adapted from the Blocks and Stuff project.
 * Original source: https://github.com/everbuild-org/blocks-and-stuff
 * <p>
 * Original authors: ChrisB, AEinNico, CreepyX
 * <p>
 * Ported from Kotlin to Java and adapted for use in this project with modifications.
 */
public class FenceGatePlacementRule extends BlockPlacementRule {
    private final RegistryTag<Block> walls;

    public FenceGatePlacementRule(Block block) {
        super(block);
        this.walls = Block.staticRegistry().getTag(TagKey.ofHash("#minecraft:walls"));
    }

    @Override
    public Block blockPlace(@NotNull PlacementState state) {
        Direction direction = DirectionUtils.getNearestHorizontalLookingDirection(state).opposite();
        Block block = state.block()
            .withProperty("facing", direction.toString().toLowerCase());

        return integrateInWalls(state.instance(), state.placePosition(), block);
    }

    @Override
    public @NotNull Block blockUpdate(UpdateState updateState) {
        return integrateInWalls(updateState.instance(), updateState.blockPosition(), updateState.currentBlock());
    }

    private Block integrateInWalls(Block.Getter instance, Point pos, Block block) {
        Direction direction = Direction.valueOf(block.getProperty("facing").toUpperCase());
        Block leftBlock = instance.getBlock(pos.add(DirectionUtils.rotateR(direction).vec()));
        Block rightBlock = instance.getBlock(pos.add(DirectionUtils.rotateL(direction).vec()));
        boolean inWall = walls.contains(leftBlock) || walls.contains(rightBlock);

        return block.withProperty("in_wall", String.valueOf(inWall));
    }
}
