package net.minestom.vanilla.blocks.placement;

import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.vanilla.common.utils.BlockUtil;
import net.minestom.vanilla.common.utils.FluidUtils;

import java.util.HashMap;
import java.util.Map;

public class CandlePlacementRule extends BlockPlacementRule {
    private static final Map<Block, Block> CANDLE_CAKE = new HashMap<>();

    static {
        CANDLE_CAKE.put(Block.CANDLE, Block.CANDLE_CAKE);
        CANDLE_CAKE.put(Block.WHITE_CANDLE, Block.WHITE_CANDLE_CAKE);
        CANDLE_CAKE.put(Block.ORANGE_CANDLE, Block.ORANGE_CANDLE_CAKE);
        CANDLE_CAKE.put(Block.MAGENTA_CANDLE, Block.MAGENTA_CANDLE_CAKE);
        CANDLE_CAKE.put(Block.LIGHT_BLUE_CANDLE, Block.LIGHT_BLUE_CANDLE_CAKE);
        CANDLE_CAKE.put(Block.YELLOW_CANDLE, Block.YELLOW_CANDLE_CAKE);
        CANDLE_CAKE.put(Block.LIME_CANDLE, Block.LIME_CANDLE_CAKE);
        CANDLE_CAKE.put(Block.PINK_CANDLE, Block.PINK_CANDLE_CAKE);
        CANDLE_CAKE.put(Block.GRAY_CANDLE, Block.GRAY_CANDLE_CAKE);
        CANDLE_CAKE.put(Block.LIGHT_GRAY_CANDLE, Block.LIGHT_GRAY_CANDLE_CAKE);
        CANDLE_CAKE.put(Block.CYAN_CANDLE, Block.CYAN_CANDLE_CAKE);
        CANDLE_CAKE.put(Block.PURPLE_CANDLE, Block.PURPLE_CANDLE_CAKE);
        CANDLE_CAKE.put(Block.BLUE_CANDLE, Block.BLUE_CANDLE_CAKE);
        CANDLE_CAKE.put(Block.BROWN_CANDLE, Block.BROWN_CANDLE_CAKE);
        CANDLE_CAKE.put(Block.GREEN_CANDLE, Block.GREEN_CANDLE_CAKE);
        CANDLE_CAKE.put(Block.RED_CANDLE, Block.RED_CANDLE_CAKE);
        CANDLE_CAKE.put(Block.BLACK_CANDLE, Block.BLACK_CANDLE_CAKE);
    }

    public static Map<Block, Block> getCANDLE_CAKE() {
        return CANDLE_CAKE;
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
            Block candleCake = CANDLE_CAKE.get(block);
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
