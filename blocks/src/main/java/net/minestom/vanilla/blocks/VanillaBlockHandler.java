package net.minestom.vanilla.blocks;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Represents a singular vanilla block's logic. e.g. white bed, cake, furnace, etc.
 */
public abstract class VanillaBlockHandler implements BlockHandler {

    protected final @NotNull VanillaBlocks.BlockContext context;
    protected final @NotNull Block baseBlock;
    protected final @NotNull NamespaceID namespaceID;

    protected VanillaBlockHandler(@NotNull VanillaBlocks.BlockContext context) {
        this.context = context;
        this.baseBlock = context.block();
        this.namespaceID = baseBlock.namespace();
    }

    @Override
    public @NotNull NamespaceID getNamespaceId() {
        return namespaceID;
    }

    public @NotNull Map<Tag<?>, ?> defaultTagValues() {
        return Map.of();
    }

    /**
     * Deprecated, use {@link #onPlace(VanillaPlacement)} instead.
     *
     * @param placement the placement object
     */
    @Override
    @Deprecated
    public void onPlace(@NotNull Placement placement) {
    }

    public void onPlace(@NotNull VanillaPlacement placement) {
    }

    public interface VanillaPlacement {

        /**
         * @return the block that will be placed
         */
        @NotNull Block blockToPlace();

        /**
         * @return the instance that will be modified
         */
        @NotNull Instance instance();

        /**
         * @return the position of the block that will be placed
         */
        @NotNull Point position();

        /**
         * Overrides the current block to be placed.
         *
         * @param newBlock the new block to be placed
         * @return the old block that will no longer be used
         */
        @NotNull Block blockToPlace(@NotNull Block newBlock);

        interface HasPlayer {
            @NotNull Player player();
        }
    }
}
