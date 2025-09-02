package net.minestom.vanilla.blocks.placement;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.vanilla.common.item.DroppedItemFactory;
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
public class FloorFillerPlacementRule extends BlockPlacementRule {

    public FloorFillerPlacementRule(Block block) {
        super(block);
    }

    public String getPropertyName() {
        if (block == Block.LEAF_LITTER) {
            return "segment_amount";
        }
        return "flower_amount";
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        if (!isSupported(placementState.instance(), placementState.placePosition())) {
            return null;
        }

        String facing = DirectionUtils.getNearestHorizontalLookingDirection(placementState).toString().toLowerCase();
        Block previousBlock = placementState.instance().getBlock(placementState.placePosition());
        boolean isSelf = previousBlock.compare(block, Block.Comparator.ID);

        String amountProperty = previousBlock.getProperty(getPropertyName());
        int petals = (amountProperty != null) ? Integer.parseInt(amountProperty) : 0;

        Block resultBlock;
        if (isSelf) {
            resultBlock = previousBlock;
        } else {
            resultBlock = placementState.block().withProperty("facing", facing);
        }

        return resultBlock.withProperty(getPropertyName(), String.valueOf(petals + 1));
    }

    @Override
    public @NotNull Block blockUpdate(UpdateState updateState) {
        if (!isSupported(updateState.instance(), updateState.blockPosition())) {
            DroppedItemFactory.maybeDrop(updateState);
            return Block.AIR;
        }
        return super.blockUpdate(updateState);
    }

    @Override
    public boolean isSelfReplaceable(Replacement replacement) {
        String amountProperty = replacement.block().getProperty(getPropertyName());
        int petals = (amountProperty != null) ? Integer.parseInt(amountProperty) : 0;
        return petals < 4;
    }

    public boolean isSupported(Block.Getter instance, Point block) {
        Block below = instance.getBlock(block.sub(0.0, 1.0, 0.0));
        return below.registry().collisionShape().isFaceFull(BlockFace.TOP);
    }
}
