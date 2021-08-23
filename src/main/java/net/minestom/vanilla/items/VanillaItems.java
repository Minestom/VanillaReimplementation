package net.minestom.vanilla.items;

import net.minestom.server.data.Data;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.event.player.PlayerUseItemOnBlockEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.ConnectionManager;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * All items with special behaviour available in the vanilla reimplementation
 */
public enum VanillaItems {

    FLINT_AND_STEEL(Material.FLINT_AND_STEEL, FlintAndSteelHandler::new);

    private final Material material;
    private final Supplier<VanillaItemHandler> itemCreator;

    VanillaItems(@NotNull Material material, Supplier<VanillaItemHandler> itemCreator) {
        this.itemCreator = itemCreator;
        this.material = material;
    }

    public @NotNull Material getMaterial() {
        return material;
    }

    public @NotNull Supplier<VanillaItemHandler> getItemHandlerSupplier() {
        return itemCreator;
    }
}
