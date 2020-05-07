package net.minestom.vanilla.items;

import net.minestom.server.event.player.PlayerInteractEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.event.player.PlayerUseItemOnBlockEvent;
import net.minestom.server.network.ConnectionManager;

import java.util.function.Supplier;

/**
 * All items with special behaviour available in the vanilla reimplementation
 */
public enum VanillaItems {

    FLINT_AND_STEEL(FlintAndSteel::new);

    private final Supplier<VanillaItem> itemCreator;

    private VanillaItems(Supplier<VanillaItem> itemCreator) {
        this.itemCreator = itemCreator;
    }

    /**
     * Register all vanilla items into the given manager
     * @param connectionManager used to add events to new players
     */
    public static void registerAll(ConnectionManager connectionManager) {
        connectionManager.addPlayerInitialization(player -> {
            for (VanillaItems itemDescription : values()) {
                VanillaItem item = itemDescription.itemCreator.get();
                player.addEventCallback(PlayerUseItemEvent.class, event -> {
                    System.out.println(event.getItemStack().getMaterial());
                    if(event.getItemStack().getMaterial() == item.getMaterial()) {
                        item.onUseInAir(player, event.getItemStack(), event.getHand());
                    }
                });

                player.addEventCallback(PlayerInteractEvent.class, event -> {
                    // TODO
                });

                player.addEventCallback(PlayerUseItemOnBlockEvent.class, event -> {
                    if(event.getItemStack().getMaterial() == item.getMaterial()) {
                        item.onUseOnBlock(player, event.getItemStack(), event.getHand(), event.getPosition(), event.getBlockFace());
                    }
                });
            }
        });
    }
}
