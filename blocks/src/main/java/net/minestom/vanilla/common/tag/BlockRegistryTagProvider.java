package net.minestom.vanilla.common.tag;

import net.minestom.server.instance.block.Block;

/**
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
