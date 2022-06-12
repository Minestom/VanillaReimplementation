package net.minestom.vanilla.items;

import net.minestom.server.item.Material;
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
