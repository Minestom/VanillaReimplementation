package net.minestom.vanilla.items;

import net.minestom.server.event.Event;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.event.player.PlayerUseItemOnBlockEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ItemManager {

    public static @NotNull ItemManager accumulate(@NotNull Consumer<Accumulator> accumulator) {
        Map<Material, VanillaItemHandler> itemHandlersByMaterial = new HashMap<>();
        accumulator.accept(itemHandlersByMaterial::put);
        return new ItemManager(itemHandlersByMaterial);
    }

    public interface Accumulator {
        void accumulate(@NotNull Material material, @NotNull VanillaItemHandler itemHandler);
    }

    private final Map<Material, VanillaItemHandler> itemHandlersByMaterial;

    private ItemManager(Map<Material, VanillaItemHandler> itemHandlersByMaterial) {
        this.itemHandlersByMaterial = Map.copyOf(itemHandlersByMaterial);
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
