package net.minestom.vanilla.loot;

import net.kyori.adventure.key.Key;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * A loot table.
 * @param pools the pools that generate items in this table
 * @param functions the functions applied to each output item of this table
 * @param randomSequence An ID specifying the name of the random sequence that is used to generate loot from this loot table.
 */
public record LootTable(@NotNull List<LootPool> pools, @NotNull List<LootFunction> functions, @Nullable Key randomSequence) implements LootGenerator {

    public static final @NotNull LootTable EMPTY = new LootTable(List.of(), List.of(), null);

    @SuppressWarnings("UnstableApiUsage")
    public static final @NotNull StructCodec<LootTable> CODEC = StructCodec.struct(
            "pools", LootPool.CODEC.list().optional(List.of()), LootTable::pools,
            "functions", LootFunction.CODEC.list().optional(List.of()), LootTable::functions,
            "random_sequence", Codec.KEY.optional(), LootTable::randomSequence,
            LootTable::new
    );

    @Override
    public @NotNull List<ItemStack> generate(@NotNull LootContext context) {
        List<ItemStack> items = new ArrayList<>();

        for (var pool : pools) {
            for (var item : pool.generate(context)) {
                items.add(LootFunction.apply(functions, item, context));
            }
        }

        return items;
    }
}
