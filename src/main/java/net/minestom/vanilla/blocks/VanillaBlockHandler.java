package net.minestom.vanilla.blocks;

import com.google.common.collect.ImmutableMap;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.blocks.redstone.signal.info.RedstoneSignalTarget;
import net.minestom.vanilla.blocks.redstone.signal.info.RedstoneSignal;
import net.minestom.vanilla.blocks.update.info.BlockUpdate;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

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

    public void onBlockUpdate(BlockUpdate blockUpdate) {
    }

    public @NotNull Map<Tag<?>, ?> defaultTagValues() {
        return ImmutableMap.of();
    }
}
