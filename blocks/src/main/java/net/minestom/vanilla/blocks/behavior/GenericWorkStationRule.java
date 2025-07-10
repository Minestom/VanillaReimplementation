package net.minestom.vanilla.blocks.behavior;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;

public class GenericWorkStationRule implements BlockHandler {
    private final Block block;
    private final InventoryType type;
    private final String title;

    public GenericWorkStationRule(Block block, InventoryType type, String title) {
        this.block = block;
        this.type = type;
        this.title = title;
    }

    @Override
    public Key getKey() {
        return block.key();
    }

    @Override
    public boolean onInteract(Interaction interaction) {
        if (interaction.getPlayer().isSneaking() && !interaction.getPlayer().getItemInMainHand().isAir()) {
            return BlockHandler.super.onInteract(interaction);
        }

        interaction.getPlayer().openInventory(new Inventory(type, Component.translatable(title)));
        return false;
    }
}
