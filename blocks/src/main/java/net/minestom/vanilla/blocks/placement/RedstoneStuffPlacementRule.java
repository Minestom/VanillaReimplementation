package net.minestom.vanilla.blocks.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.utils.Direction;
import net.minestom.vanilla.common.item.DroppedItemFactory;
import net.minestom.vanilla.common.utils.DirectionUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * This file contains code ported from Kotlin to Java, adapted from the Blocks and Stuff project.
 * Original source: https://github.com/everbuild-org/blocks-and-stuff
 * <p>
 * Original authors: ChrisB, AEinNico, CreepyX
 * <p>
 * Ported from Kotlin to Java and adapted for use in this project with modifications.
 */
public class RedstoneStuffPlacementRule extends BlockPlacementRule {

    public RedstoneStuffPlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        var supportPosition = placementState.placePosition().relative(BlockFace.BOTTOM);
        var supportBlock = placementState.instance().getBlock(supportPosition);

        if (supportBlock.isAir() || supportBlock.compare(block)) {
            return null;
        }

        Direction facing = DirectionUtils.getNearestHorizontalLookingDirection(placementState);
        return block
            .withProperty("facing", facing.name().toLowerCase(Locale.ROOT))
            .withProperty("powered", "false");
    }

    @Override
    public @NotNull Block blockUpdate(UpdateState updateState) {
        var supportPosition = updateState.blockPosition().relative(BlockFace.BOTTOM);
        var supportBlock = updateState.instance().getBlock(supportPosition);

        if (supportBlock.isAir()) {
            DroppedItemFactory.maybeDrop(updateState);
            return Block.AIR;
        }
        return updateState.currentBlock();
    }
}
