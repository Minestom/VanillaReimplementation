package net.minestom.vanilla.blocks;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Represents a singular vanilla block's logic. e.g. white bed, cake, furnace, etc.
 */
public abstract class VanillaBlockBehaviour implements BlockHandler {

    protected final @NotNull VanillaBlocks.BlockContext context;
    protected final short baseBlock;
    protected final @NotNull NamespaceID namespaceID;

    protected VanillaBlockBehaviour(@NotNull VanillaBlocks.BlockContext context) {
        this.context = context;
        this.baseBlock = context.stateId();
        this.namespaceID = Objects.requireNonNull(Block.fromStateId(context.stateId())).namespace();
    }

    /**
     * DO NOT USE THIS.
     * @see #onPlace(VanillaPlacement) instead.
     */
    @Override
    @Deprecated
    public void onPlace(@NotNull BlockHandler.Placement placement) {
    }

    public void onPlace(@NotNull VanillaPlacement placement) {
    }

    @Override
    public @NotNull NamespaceID getNamespaceId() {
        return namespaceID;
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
         */
        void blockToPlace(@NotNull Block newBlock);

        interface HasPlayer {
            @NotNull Player player();
        }
    }

}
