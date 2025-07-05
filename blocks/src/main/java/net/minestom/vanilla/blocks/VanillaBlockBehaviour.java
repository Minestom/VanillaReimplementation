package net.minestom.vanilla.blocks;

import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Represents a singular vanilla block's logic. e.g. white bed, cake, furnace, etc.
 */
public abstract class VanillaBlockBehaviour implements BlockHandler {

    protected final @NotNull VanillaBlocks.BlockContext context;
    protected final short baseBlock;
    protected final @NotNull Key key;

    protected VanillaBlockBehaviour(@NotNull VanillaBlocks.BlockContext context) {
        this.context = context;
        this.baseBlock = context.stateId();
        this.key = Objects.requireNonNull(Block.fromStateId(context.stateId())).key();
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
    public @NotNull Key getKey() {
        return key;
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
