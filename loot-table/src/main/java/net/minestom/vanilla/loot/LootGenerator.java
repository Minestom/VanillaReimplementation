package net.minestom.vanilla.loot;

import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Something that can generate loot.
 */
public interface LootGenerator {

    @NotNull List<ItemStack> generate(@NotNull LootContext context);

}
