package net.minestom.vanilla.blocks.placement;

import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.utils.Direction;
import net.minestom.vanilla.common.item.DroppedItemFactory;
import net.minestom.vanilla.common.tag.BlockTags;
import net.minestom.vanilla.common.utils.DirectionUtils;
import net.minestom.vanilla.common.utils.FluidUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * This file contains code ported from Kotlin to Java, adapted from the Blocks and Stuff project.
 * Original source: https://github.com/everbuild-org/blocks-and-stuff
 * <p>
 * Original authors: ChrisB, AEinNico, CreepyX
 * <p>
 * Ported from Kotlin to Java and adapted for use in this project with modifications.
 */
public class BigDripleafPlacementRule extends BlockPlacementRule {
    private final Set<Block> plantableOn;

    public BigDripleafPlacementRule(Block block) {
        super(block);
        this.plantableOn = BlockTags.getInstance().getTaggedWith("minecraft:big_dripleaf_placeable");
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        Block blockBelow = placementState.instance().getBlock(placementState.placePosition().sub(0.0, 1.0, 0.0));
        boolean placingInsideWater = FluidUtils.isWater(placementState.instance().getBlock(placementState.placePosition()));

        // Check if placing on valid ground
        for (Block validBlock : plantableOn) {
            if (validBlock.compare(blockBelow)) {
                Direction direction = DirectionUtils.getNearestHorizontalLookingDirection(placementState);
                return Block.BIG_DRIPLEAF
                    .withProperty("facing", direction.name().toLowerCase())
                    .withProperty("waterlogged", String.valueOf(placingInsideWater));
            }
        }

        // Check if placing on another dripleaf
        if (blockBelow.compare(Block.BIG_DRIPLEAF)) {
            String direction = blockBelow.getProperty("facing");
            boolean bottomInsideWater = Boolean.parseBoolean(blockBelow.getProperty("waterlogged"));
            Instance instance = (Instance) placementState.instance();

            instance.setBlock(
                placementState.placePosition().sub(0.0, 1.0, 0.0),
                Block.BIG_DRIPLEAF_STEM
                    .withProperty("facing", direction)
                    .withProperty("waterlogged", String.valueOf(bottomInsideWater))
            );

            return Block.BIG_DRIPLEAF
                .withProperty("facing", direction)
                .withProperty("waterlogged", String.valueOf(placingInsideWater));
        }

        return null;
    }

    @Override
    public @NotNull Block blockUpdate(UpdateState updateState) {
        Block blockAbove = updateState.instance().getBlock(updateState.blockPosition().add(0.0, 1.0, 0.0));
        if (block.compare(Block.BIG_DRIPLEAF_STEM)
            && !(blockAbove.compare(Block.BIG_DRIPLEAF_STEM) || blockAbove.compare(Block.BIG_DRIPLEAF))
        ) {
            DroppedItemFactory.maybeDrop(updateState);
            return Block.AIR;
        }

        Block blockBelow = updateState.instance().getBlock(updateState.blockPosition().sub(0.0, 1.0, 0.0));

        boolean validBelow = false;
        if (blockBelow.compare(Block.BIG_DRIPLEAF_STEM)) {
            validBelow = true;
        } else {
            for (Block validBlock : plantableOn) {
                if (validBlock.compare(blockBelow)) {
                    validBelow = true;
                    break;
                }
            }
        }

        if (!validBelow) {
            DroppedItemFactory.maybeDrop(updateState);
            return Block.AIR;
        }

        return updateState.currentBlock();
    }
}
