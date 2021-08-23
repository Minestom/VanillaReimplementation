package net.minestom.vanilla.items;

import net.minestom.server.event.Event;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.event.player.PlayerUseItemOnBlockEvent;
import net.minestom.server.event.trait.ItemEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ItemManager {

    private final Map<Material, VanillaItemHandler> itemHandlersByMaterial;

    public ItemManager() {
        this.itemHandlersByMaterial = Arrays.stream(VanillaItems.values())
                .collect(Collectors.toUnmodifiableMap(
                        VanillaItems::getMaterial,
                        item -> item.getItemHandlerSupplier().get()
                ));
    }

    private void handlePlayerUseItemEvent(PlayerUseItemEvent event) {
        ItemStack itemStack = event.getItemStack();

        VanillaItemHandler itemHandler = itemHandlersByMaterial.get(itemStack.getMaterial());

        if (itemHandler == null) {
            return;
        }

        itemHandler.onUseInAir(event);
    }

    private void handlePlayerUseItemOnBlockEvent(PlayerUseItemOnBlockEvent event) {
        ItemStack itemStack = event.getItemStack();

        VanillaItemHandler itemHandler = itemHandlersByMaterial.get(itemStack.getMaterial());

        if (itemHandler == null) {
            return;
        }

        itemHandler.onUseOnBlock(event);
    }

    public void registerEvents(EventNode<Event> itemEventNode) {
        itemEventNode.addListener(
                EventListener.of(PlayerUseItemEvent.class, this::handlePlayerUseItemEvent)
        );

        itemEventNode.addListener(
                EventListener.of(PlayerUseItemOnBlockEvent.class, this::handlePlayerUseItemOnBlockEvent)
        );
    }
}
