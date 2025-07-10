package net.minestom.vanilla.blocks.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.vanilla.common.utils.DirectionUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
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
public class HeadPlacementRule extends BlockPlacementRule {
    private static final Map<Block, Block> WALL_VARIANTS = new HashMap<>();

    static {
        WALL_VARIANTS.put(Block.PLAYER_HEAD, Block.PLAYER_WALL_HEAD);
        WALL_VARIANTS.put(Block.SKELETON_SKULL, Block.SKELETON_WALL_SKULL);
        WALL_VARIANTS.put(Block.WITHER_SKELETON_SKULL, Block.WITHER_SKELETON_WALL_SKULL);
        WALL_VARIANTS.put(Block.ZOMBIE_HEAD, Block.ZOMBIE_WALL_HEAD);
        WALL_VARIANTS.put(Block.CREEPER_HEAD, Block.CREEPER_WALL_HEAD);
        WALL_VARIANTS.put(Block.DRAGON_HEAD, Block.DRAGON_WALL_HEAD);
    }

    public HeadPlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        BlockFace clickedFace = placementState.blockFace();
        if (clickedFace == null) {
            return block;
        }

        if (clickedFace == BlockFace.TOP || clickedFace == BlockFace.BOTTOM) {
            int rotation = DirectionUtils.sixteenStepRotation(placementState);
            return block.withProperty("rotation", String.valueOf(rotation));
        } else {
            Block wallVariant = WALL_VARIANTS.get(block);
            if (wallVariant == null) {
                return block;
            }
            String facing = clickedFace.name().toLowerCase(Locale.ROOT);
            return wallVariant.withProperty("facing", facing);
        }
    }

    @Override
    public @NotNull Block blockUpdate(UpdateState updateState) {
        return updateState.currentBlock();
    }
}
