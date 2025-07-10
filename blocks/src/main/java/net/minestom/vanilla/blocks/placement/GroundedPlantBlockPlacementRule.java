package net.minestom.vanilla.blocks.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.registry.RegistryTag;
import net.minestom.server.registry.TagKey;
import net.minestom.vanilla.common.item.DroppedItemFactory;
import org.jetbrains.annotations.NotNull;

/**
 * This file contains code ported from Kotlin to Java, adapted from the Blocks and Stuff project.
 * Original source: https://github.com/everbuild-org/blocks-and-stuff
 * <p>
 * Original authors: ChrisB, AEinNico, CreepyX
 * <p>
 * Ported from Kotlin to Java and adapted for use in this project with modifications.
 */
public class GroundedPlantBlockPlacementRule extends BlockPlacementRule {
    private final RegistryTag<Block> dirtBlocks;

    public GroundedPlantBlockPlacementRule(Block block) {
        super(block);
        this.dirtBlocks = Block.staticRegistry().getTag(TagKey.ofHash("#minecraft:dirt"));
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        Block blockBelow = placementState.instance().getBlock(placementState.placePosition().add(0.0, -1.0, 0.0));

        if (dirtBlocks.contains(blockBelow)) {
            return placementState.block();
        }
        return null;
    }

    @Override
    public @NotNull Block blockUpdate(UpdateState updateState) {
        Block blockBelow = updateState.instance().getBlock(updateState.blockPosition().add(0.0, -1.0, 0.0));

        if (!dirtBlocks.contains(blockBelow)) {
            DroppedItemFactory.maybeDrop(updateState);
            return Block.AIR;
        }

        return updateState.currentBlock();
    }
}
