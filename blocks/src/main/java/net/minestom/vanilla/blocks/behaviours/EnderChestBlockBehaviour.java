package net.minestom.vanilla.blocks.behaviours;

import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.vanilla.blocks.VanillaBlocks;
import net.minestom.vanilla.system.EnderChestSystem;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class EnderChestBlockBehaviour extends InventoryBlockBehaviour {
    public EnderChestBlockBehaviour(@NotNull VanillaBlocks.BlockContext context) {
        super(context, InventoryType.CHEST_3_ROW, Component.text("Ender Chest"));
    }

    @Override
    public boolean dropContentsOnDestroy() {
        return false;
    }

    @Override
    protected List<ItemStack> getAllItems(Instance instance, Point pos, Player player) {
        return EnderChestSystem.getInstance().getItems(player);
    }
}
