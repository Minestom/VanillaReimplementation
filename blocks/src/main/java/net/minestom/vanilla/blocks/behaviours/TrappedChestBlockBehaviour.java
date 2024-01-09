package net.minestom.vanilla.blocks.behaviours;

import net.kyori.adventure.text.Component;
import net.minestom.server.inventory.InventoryType;
import net.minestom.vanilla.blocks.VanillaBlocks;
import org.jetbrains.annotations.NotNull;

public class TrappedChestBlockBehaviour extends InventoryBlockBehaviour {
    // TODO: redstone signal

    public TrappedChestBlockBehaviour(@NotNull VanillaBlocks.BlockContext context) {
        super(context, InventoryType.CHEST_3_ROW, Component.text("Trapped Chest"));
    }

//    @Override
//    protected BlockPropertyList createPropertyValues() {
//        return super.createPropertyValues().property("type", "single", "left", "right");
//    }

    @Override
    public boolean dropContentsOnDestroy() {
        return true;
    }
}
