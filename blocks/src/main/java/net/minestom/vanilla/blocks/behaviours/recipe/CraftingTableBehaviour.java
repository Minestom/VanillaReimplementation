package net.minestom.vanilla.blocks.behaviours.recipe;

import net.minestom.server.entity.Player;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.vanilla.blocks.VanillaBlockBehaviour;
import net.minestom.vanilla.blocks.VanillaBlocks;
import org.jetbrains.annotations.NotNull;

public class CraftingTableBehaviour extends VanillaBlockBehaviour {
    public CraftingTableBehaviour(VanillaBlocks.@NotNull BlockContext context) {
        super(context);
    }

    @Override
    public boolean onInteract(@NotNull BlockHandler.Interaction interaction) {
        Inventory inventory = new Inventory(InventoryType.CRAFTING, "Crafting Table");
        Player player = interaction.getPlayer();
        player.openInventory(inventory);
        player.eventNode().addListener(
                EventListener.builder(InventoryCloseEvent.class)
                        .filter(event -> inventory == event.getInventory())
                        .expireCount(1)
                        .handler(event -> {
                            // TODO: Drop all items instead of adding them to the player's inventory?
                            for (ItemStack itemStack : inventory.getItemStacks()) {
                                player.getInventory().addItemStack(itemStack);
                            }
                        })
                        .build()
        );
        return false;
    }
}
