package net.minestom.vanilla.common.utils;

import java.util.Set;
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

    private static final Set<Block> glassPanes = Set.of(
        Block.WHITE_STAINED_GLASS_PANE,
        Block.LIGHT_GRAY_STAINED_GLASS_PANE,
        Block.GRAY_STAINED_GLASS_PANE,
        Block.BLACK_STAINED_GLASS_PANE,
        Block.BROWN_STAINED_GLASS_PANE,
        Block.RED_STAINED_GLASS_PANE,
        Block.ORANGE_STAINED_GLASS_PANE,
        Block.YELLOW_STAINED_GLASS_PANE,
        Block.LIME_STAINED_GLASS_PANE,
        Block.GREEN_STAINED_GLASS_PANE,
        Block.CYAN_STAINED_GLASS_PANE,
        Block.LIGHT_BLUE_STAINED_GLASS_PANE,
        Block.BLUE_STAINED_GLASS_PANE,
        Block.PURPLE_STAINED_GLASS_PANE,
        Block.MAGENTA_STAINED_GLASS_PANE,
        Block.PINK_STAINED_GLASS_PANE,
        Block.GLASS_PANE
    );

    public static Set<Block> getGlassPanes() {
        return glassPanes;
    }

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
