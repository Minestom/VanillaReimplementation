package net.minestom.vanilla.loot.util.predicate;

import net.minestom.server.codec.StructCodec;
import net.minestom.server.instance.block.predicate.DataComponentPredicates;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.RegistryTag;
import net.minestom.vanilla.loot.LootContext;
import net.minestom.vanilla.loot.util.LootNumberRange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public record ItemPredicate(@Nullable RegistryTag<Material> items, @NotNull LootNumberRange count, @NotNull DataComponentPredicates predicate) {

    public static final @NotNull StructCodec<ItemPredicate> CODEC = StructCodec.struct(
            "items", RegistryTag.codec(Registries::material).optional(), ItemPredicate::items,
            "count", LootNumberRange.CODEC.optional(new LootNumberRange(null, null)), ItemPredicate::count,
            StructCodec.INLINE, DataComponentPredicates.CODEC, ItemPredicate::predicate,
            ItemPredicate::new
    );

    public boolean test(@NotNull ItemStack itemStack, @NotNull LootContext context) {

        if (items != null && !items.contains(itemStack.material())) return false;

        return count.check(context, itemStack.amount()) && false;
        // TODO: Waiting for #2732
        // return count.check(context, itemStack.amount()) && predicate.test(itemStack);
    }

}
