package net.minestom.vanilla.items;

import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.VanillaRegistry;
import net.minestom.vanilla.VanillaReimplementation;
import org.jetbrains.annotations.NotNull;

public class ItemsFeature implements VanillaReimplementation.Feature {
    @Override
    public void hook(@NotNull VanillaReimplementation vri, @NotNull VanillaRegistry registry) {
        ItemManager manager = ItemManager.accumulate(accumulator -> {
            for (VanillaItems item : VanillaItems.values()) {
                accumulator.accumulate(item.getMaterial(), item.getItemHandlerSupplier().get());
            }
        });
        manager.registerEvents(vri.process().eventHandler());
    }

    @Override
    public @NotNull NamespaceID namespaceId() {
        return NamespaceID.from("vri:items");
    }
}
