package net.minestom.vanilla.items;

import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.event.player.PlayerUseItemOnBlockEvent;

public interface VanillaItemHandler {

    /**
     * Called when the player right clicks with this item in the air
     *
     * @param event the event object
     */
    default void onUseInAir(PlayerUseItemEvent event) {}

    /**
     * Called when the player right clicks with this item on a block
     *
     * @param event the event object
     * @return true if it prevents normal item use (placing blocks for instance)
     */
    default boolean onUseOnBlock(PlayerUseItemOnBlockEvent event) {
        return false;
    }
}
