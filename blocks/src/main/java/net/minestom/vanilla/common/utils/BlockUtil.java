package net.minestom.vanilla.common.utils;

import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockManager;

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
