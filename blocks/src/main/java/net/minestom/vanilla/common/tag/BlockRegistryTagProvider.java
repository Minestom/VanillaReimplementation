package net.minestom.vanilla.common.tag;

import net.minestom.server.instance.block.Block;

/**
 *
 * This file contains code ported from Kotlin to Java, adapted from the Blocks and Stuff project.
 * Original source: https://github.com/everbuild-org/blocks-and-stuff
 * <p>
 * Original authors: ChrisB, AEinNico, CreepyX
 * <p>
 * Ported from Kotlin to Java and adapted for use in this project with modifications.
 * <p>
 * A registry tag provider for blocks
 */
public class BlockRegistryTagProvider extends RegistryTagProvider<Block> {

    private static final BlockRegistryTagProvider INSTANCE = new BlockRegistryTagProvider();

    private BlockRegistryTagProvider() {
        super("block");
    }

    public static BlockRegistryTagProvider getInstance() {
        return INSTANCE;
    }

    @Override
    protected Block map(String key) {
        return Block.fromKey(key);
    }
}
