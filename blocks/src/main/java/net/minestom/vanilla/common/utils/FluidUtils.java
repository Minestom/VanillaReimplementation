package net.minestom.vanilla.common.utils;

import net.minestom.server.instance.block.Block;

public class FluidUtils {

    public static boolean isWaterSource(Block block) {
        return block.compare(Block.WATER);
    }

    public static boolean isWater(Block block) {
        return block.compare(Block.WATER) || block.compare(Block.BUBBLE_COLUMN);
    }
}

