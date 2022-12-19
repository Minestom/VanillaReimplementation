package net.minestom.vanilla.blocks;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Represents a singular vanilla block's logic. e.g. white bed, cake, furnace, etc.
 */
//@Deprecated(forRemoval = true)
public abstract class VanillaBlockBehaviour {

    protected final @NotNull VanillaBlocks.BlockContext context;
    protected final short baseBlock;
    protected final @NotNull NamespaceID namespaceID;

    protected VanillaBlockBehaviour(@NotNull VanillaBlocks.BlockContext context) {
        this.context = context;
        this.baseBlock = context.stateId();
        this.namespaceID = Block.fromStateId(context.stateId()).namespace();
    }

    public void onPlace(@NotNull VanillaPlacement placement) {
    }

    /**
     * Handles interactions with this block. Can also block normal item use (containers should block when opening the
     * menu, this prevents the player from placing a block when opening it for instance).
     *
     * @param interaction the interaction details
     * @return false to stop the interaction from continuing, true to allow it to continue
     */
    public boolean onInteract(@NotNull VanillaInteraction interaction) {
        return true;
    }

    /**
     * Called when a block has been destroyed or replaced.
     *
     * @param destroy the destroy object
     */
    public void onDestroy(@NotNull VanillaDestroy destroy) {
    }

    /**
     * Defines custom behaviour for entities touching this block.
     *
     * @param touch the contact details
     */
    public void onTouch(@NotNull VanillaTouch touch) {
    }


    public void tick(@NotNull VanillaTick tick) {
    }

    public boolean isTickable() {
        return false;
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

    public interface VanillaInteraction {
        @NotNull Block block();

        @NotNull Instance instance();

        @NotNull Point blockPosition();

        @NotNull Player player();

        @NotNull Player.Hand hand();
    }

    public interface VanillaDestroy {
        @NotNull Block block();

        @NotNull Instance instance();

        @NotNull Point blockPosition();
    }

    public interface VanillaTouch {
        @NotNull Block block();

        @NotNull Instance instance();

        @NotNull Point blockPosition();

        @NotNull Entity touching();
    }

    public interface VanillaTick {
        @NotNull Block block();

        @NotNull Instance instance();

        @NotNull Point getBlockPosition();
    }
}
