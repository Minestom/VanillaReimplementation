package net.minestom.vanilla.blocks.placement;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.registry.RegistryTag;
import net.minestom.server.registry.TagKey;
import net.minestom.vanilla.common.item.DroppedItemFactory;
import net.minestom.vanilla.common.utils.DirectionUtils;
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
public class SignPlacementRule extends BlockPlacementRule {
    private static final Map<Block, Block> WALL_SIGNS = new HashMap<>();
    private final RegistryTag<Block> wallSigns;

    static {
        WALL_SIGNS.put(Block.ACACIA_SIGN, Block.ACACIA_WALL_SIGN);
        WALL_SIGNS.put(Block.BAMBOO_SIGN, Block.BAMBOO_WALL_SIGN);
        WALL_SIGNS.put(Block.BIRCH_SIGN, Block.BIRCH_WALL_SIGN);
        WALL_SIGNS.put(Block.CHERRY_SIGN, Block.CHERRY_WALL_SIGN);
        WALL_SIGNS.put(Block.CRIMSON_SIGN, Block.CRIMSON_WALL_SIGN);
        WALL_SIGNS.put(Block.DARK_OAK_SIGN, Block.DARK_OAK_WALL_SIGN);
        WALL_SIGNS.put(Block.JUNGLE_SIGN, Block.JUNGLE_WALL_SIGN);
        WALL_SIGNS.put(Block.MANGROVE_SIGN, Block.MANGROVE_WALL_SIGN);
        WALL_SIGNS.put(Block.OAK_SIGN, Block.OAK_WALL_SIGN);
        WALL_SIGNS.put(Block.SPRUCE_SIGN, Block.SPRUCE_WALL_SIGN);
        WALL_SIGNS.put(Block.WARPED_SIGN, Block.WARPED_WALL_SIGN);
    }

    public SignPlacementRule(Block block) {
        super(block);
        this.wallSigns = Block.staticRegistry().getTag(TagKey.ofHash("#minecraft:wall_signs"));
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        Block currentBlock = placementState.instance().getBlock(placementState.placePosition());

        if (placementState.blockFace() == BlockFace.TOP) {
            int rotation = (DirectionUtils.sixteenStepRotation(placementState) + 8) % 16;
            return supportedOrNull(
                placementState.instance(),
                placementState.placePosition(),
                placementState.block()
                    .withProperty("rotation", String.valueOf(rotation))
                    .withProperty("waterlogged", String.valueOf(FluidUtils.isWater(currentBlock)))
            );
        } else if (placementState.blockFace() != null &&
                   placementState.blockFace().toDirection().horizontal()) {

            String facing = placementState.blockFace().toString().toLowerCase();
            BlockHandler handler = placementState.block().handler();
            CompoundBinaryTag nbt = placementState.block().nbt();

            Block wallSign = WALL_SIGNS.get(block);
            if (wallSign == null) return null;

            return supportedOrNull(
                placementState.instance(),
                placementState.placePosition(),
                wallSign
                    .withHandler(handler)
                    .withNbt(nbt)
                    .withProperty("facing", facing)
                    .withProperty("waterlogged", String.valueOf(FluidUtils.isWater(currentBlock)))
            );
        } else {
            return null;
        }
    }

    @Override
    public @NotNull Block blockUpdate(UpdateState updateState) {
        if (!isSupported(updateState.instance(), updateState.currentBlock(), updateState.blockPosition())) {
            DroppedItemFactory.maybeDrop(updateState);
            return Block.AIR;
        }
        return updateState.currentBlock();
    }

    private Block supportedOrNull(Block.Getter instance, Point position, Block block) {
        return isSupported(instance, block, position) ? block : null;
    }

    private boolean isSupported(Block.Getter instance, Block block, Point position) {
        if (wallSigns.contains(block)) {
            BlockFace facing = BlockFace.valueOf(block.getProperty("facing").toUpperCase());
            Point supportingBlockPos = position.add(facing.getOppositeFace().toDirection().vec());
            return !instance.getBlock(supportingBlockPos).isAir();
        } else {
            Block below = instance.getBlock(position.sub(0.0, 1.0, 0.0));
            return below.isSolid();
        }
    }
}
