package net.minestom.vanilla.items;


import net.kyori.adventure.key.Key;
import net.minestom.vanilla.VanillaReimplementation;
import org.jetbrains.annotations.NotNull;

public class ItemsFeature implements VanillaReimplementation.Feature {

    @Override
    public void hook(@NotNull HookContext context) {
        ItemManager manager = ItemManager.accumulate(accumulator -> {
            for (VanillaItems item : VanillaItems.values()) {
                accumulator.accumulate(item.getMaterial(), item.getItemHandlerSupplier().get());
            }
        });
        manager.registerEvents(context.vri().process().eventHandler());
    }

    @Override
    public @NotNull Key key() {
        return Key.key("vri:items");
    }
}
