package net.minestom.vanilla.common.utils;

import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockManager;

/**
 * This file contains code ported from Kotlin to Java, adapted from the Blocks and Stuff project.
 * Original source: https://github.com/everbuild-org/blocks-and-stuff
 * <p>
 * Original authors: ChrisB, AEinNico, CreepyX
 * <p>
 * Ported from Kotlin to Java and adapted for use in this project with modifications.
 */
public class BlockUtil {

    private static final BlockManager blocks = MinecraftServer.getBlockManager();

    /**
     * Ensures a block has its default handler
     * @param block The block to check
     * @return The block with its default handler, or the original block if it already has a handler
     */
    public static Block withDefaultHandler(Block block) {
        if (block.handler() != null) {
            return block;
        }
        return block.withHandler(blocks.getHandler(block.key().asString()));
    }
}
