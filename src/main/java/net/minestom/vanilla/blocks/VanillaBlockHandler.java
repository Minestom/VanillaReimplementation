package net.minestom.vanilla.blocks;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.blocks.update.info.BlockUpdate;
import net.minestom.vanilla.blocks.update.info.BlockUpdateInfo;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a singular vanilla block's logic. e.g. white bed, cake, furnace, etc.
 */
public abstract class VanillaBlockHandler implements BlockHandler {

    protected final @NotNull Block baseBlock;
    protected final @NotNull NamespaceID namespaceID;

    protected VanillaBlockHandler(@NotNull Block baseBlock) {
        this.baseBlock = baseBlock;
        this.namespaceID = baseBlock.namespace();
    }

    @Override
    public @NotNull NamespaceID getNamespaceId() {
        return namespaceID;
    }

    public void updateBlock(BlockUpdate blockUpdate) {
    }
}
