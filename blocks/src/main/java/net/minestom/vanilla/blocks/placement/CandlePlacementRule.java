package net.minestom.vanilla.blocks.placement;

import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.vanilla.common.utils.BlockUtil;
import net.minestom.vanilla.common.utils.FluidUtils;

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
public class CandlePlacementRule extends BlockPlacementRule {

    private static final Map<Block, Block> CAKE_VARIANTS = Map.copyOf(Map.ofEntries(
        Map.entry(Block.CANDLE, Block.CANDLE_CAKE),
        Map.entry(Block.WHITE_CANDLE, Block.WHITE_CANDLE_CAKE),
        Map.entry(Block.ORANGE_CANDLE, Block.ORANGE_CANDLE_CAKE),
        Map.entry(Block.MAGENTA_CANDLE, Block.MAGENTA_CANDLE_CAKE),
        Map.entry(Block.LIGHT_BLUE_CANDLE, Block.LIGHT_BLUE_CANDLE_CAKE),
        Map.entry(Block.YELLOW_CANDLE, Block.YELLOW_CANDLE_CAKE),
        Map.entry(Block.LIME_CANDLE, Block.LIME_CANDLE_CAKE),
        Map.entry(Block.PINK_CANDLE, Block.PINK_CANDLE_CAKE),
        Map.entry(Block.GRAY_CANDLE, Block.GRAY_CANDLE_CAKE),
        Map.entry(Block.LIGHT_GRAY_CANDLE, Block.LIGHT_GRAY_CANDLE_CAKE),
        Map.entry(Block.CYAN_CANDLE, Block.CYAN_CANDLE_CAKE),
        Map.entry(Block.PURPLE_CANDLE, Block.PURPLE_CANDLE_CAKE),
        Map.entry(Block.BLUE_CANDLE, Block.BLUE_CANDLE_CAKE),
        Map.entry(Block.BROWN_CANDLE, Block.BROWN_CANDLE_CAKE),
        Map.entry(Block.GREEN_CANDLE, Block.GREEN_CANDLE_CAKE),
        Map.entry(Block.RED_CANDLE, Block.RED_CANDLE_CAKE),
        Map.entry(Block.BLACK_CANDLE, Block.BLACK_CANDLE_CAKE)
    ));

    public static Map<Block, Block> getCakeVariants() {
        return CAKE_VARIANTS;
    }

    public CandlePlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        var positionBelow = placementState.placePosition().sub(0.0, 1.0, 0.0);
        var blockBelow = placementState.instance().getBlock(positionBelow);

        // Handle placing candle on cake
        if (blockBelow.compare(Block.CAKE, Block.Comparator.ID)) {
            Block candleCake = CAKE_VARIANTS.get(block);
            if (candleCake == null) {
                return null;
            }

            ((Instance) placementState.instance()).setBlock(
                positionBelow,
                BlockUtil.withDefaultHandler(candleCake)
            );
            return Block.AIR;
        }

        // Check for valid support
        if (!blockBelow.registry().collisionShape().isFaceFull(BlockFace.TOP)) {
            return null;
        }

        // Handle stacking or waterlogging
        Block oldBlock = placementState.instance().getBlock(placementState.placePosition());
        if (!oldBlock.compare(block, Block.Comparator.ID)) {
            if (FluidUtils.isWater(oldBlock)) {
                return block.withProperty("waterlogged", "true");
            } else {
                return block;
            }
        }

        // Handle stacking candles
        String candlesProperty = oldBlock.getProperty("candles");
        int oldCandles = candlesProperty != null ? Integer.parseInt(candlesProperty) : 0;
        return oldBlock.withProperty("candles", String.valueOf(oldCandles + 1));
    }

    @Override
    public boolean isSelfReplaceable(Replacement replacement) {
        String candlesProperty = replacement.block().getProperty("candles");
        if (candlesProperty == null) {
            return false;
        }
        int candles = Integer.parseInt(candlesProperty);
        return candles < 4;
    }
}
