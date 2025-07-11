package net.minestom.vanilla.common.utils;

import net.minestom.server.instance.block.Block;

/**
 * This file contains code ported from Kotlin to Java, adapted from the Blocks and Stuff project.
 * Original source: https://github.com/everbuild-org/blocks-and-stuff
 * <p>
 * Original authors: ChrisB, AEinNico, CreepyX
 * <p>
 * Ported from Kotlin to Java and adapted for use in this project with modifications.
 */
public class FluidUtils {

    public static boolean isWaterSource(Block block) {
        return block.compare(Block.WATER);
    }

    public static boolean isWater(Block block) {
        return block.compare(Block.WATER) || block.compare(Block.BUBBLE_COLUMN);
    }
}

