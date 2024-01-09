package net.minestom.vanilla.blocks.behaviours;

import net.kyori.adventure.text.Component;
import net.minestom.server.inventory.InventoryType;
import net.minestom.vanilla.blocks.VanillaBlocks;
import org.jetbrains.annotations.NotNull;

public class ChestBlockBehaviour extends InventoryBlockBehaviour {
    public ChestBlockBehaviour(@NotNull VanillaBlocks.BlockContext context) {
        super(context, InventoryType.CHEST_3_ROW, Component.text("Chest"));
    }

    @Override
    public boolean dropContentsOnDestroy() {
        return true;
    }
}
