package net.minestom.vanilla.blocks.behaviours.recipe;

import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.vanilla.blocks.VanillaBlocks;
import net.minestom.vanilla.blocks.behaviours.InventoryBlockBehaviour;
import net.minestom.vanilla.blocks.behaviours.chestlike.BlockInventory;
import net.minestom.vanilla.events.FurnaceTickEvent;
import org.jetbrains.annotations.NotNull;

public class FurnaceBehaviour extends InventoryBlockBehaviour {
    public FurnaceBehaviour(VanillaBlocks.@NotNull BlockContext context) {
        super(context, InventoryType.FURNACE, Component.text("Furnace"));
    }

    @Override
    public boolean onInteract(@NotNull BlockHandler.Interaction interaction) {
        Instance instance = interaction.getInstance();
        Point pos = interaction.getBlockPosition();
        Inventory inventory = BlockInventory.from(instance, pos, InventoryType.FURNACE, Component.text("Furnace"));
        Player player = interaction.getPlayer();
        player.openInventory(inventory);
        return false;
    }

    @Override
    public boolean dropContentsOnDestroy() {
        return true;
    }

    @Override
    public boolean isTickable() {
        return true;
    }

    @Override
    public void tick(@NotNull BlockHandler.Tick tick) {
        var events = this.context.vri().process().eventHandler();
        if (!events.hasListener(FurnaceTickEvent.class)) return; // fast exit since this is hot code
        Instance instance = tick.getInstance();
        Point pos = tick.getBlockPosition();
        Inventory inventory = BlockInventory.from(instance, pos, InventoryType.FURNACE, Component.text("Furnace"));
        FurnaceTickEvent event = new FurnaceTickEvent(tick.getBlock(), tick.getInstance(), new BlockVec(tick.getBlockPosition()), inventory);
        events.call(event);
    }
}
