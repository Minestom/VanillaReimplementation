package net.minestom.vanilla.blocks;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.vanilla.VanillaReimplementation;
import net.minestom.vanilla.datapack.Datapack;
import net.minestom.vanilla.datapack.loot.LootTable;
import net.minestom.vanilla.datapack.loot.context.LootContext;
import net.minestom.vanilla.datapack.loot.function.LootFunction;
import net.minestom.vanilla.datapack.loot.function.Predicate;
import net.minestom.vanilla.files.FileSystem;
import net.minestom.vanilla.logging.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.random.RandomGenerator;

public record VanillaBlockLoot(VanillaReimplementation vri, Datapack datapack) {

    private record LootEntry(@Nullable List<LootFunction> functions, List<ItemStack> items, double weight) {
    }

    public void spawnLoot(@NotNull PlayerBlockBreakEvent event) {
        String blockName = event.getBlock().namespace().value();
        datapack.namespacedData().forEach((namespace, data) -> {
            FileSystem<LootTable> blocks = data.loot_tables().folder("blocks");
            var lootTable = blocks.file(blockName + ".json");
            if (lootTable == null) return;

            Block blockState = event.getBlock();
            Point origin = event.getBlockPosition();
            ItemStack tool = event.getPlayer().getInventory().getItemInMainHand();
            Player entity = event.getPlayer();
            Block blockEntity = blockState.registry().blockEntity() == null ? null : blockState;
            Random random = vri.random(entity);

            LootContext context = new LootContext.Block(blockState, origin, tool, entity, blockEntity, null);

            List<ItemStack> items = new ArrayList<>();
            generateLootItems(lootTable, context, random, items::add);

            for (ItemStack item : items) {
                ItemEntity itemEntity = new ItemEntity(item);
                itemEntity.setInstance(entity.getInstance(), origin.add(0.5));
            }
        });
    }

    public List<ItemStack> getLoot(LootTable lootTable, LootContext context) {
        return getLoot(lootTable, context, vri.random(0));
    }

    public List<ItemStack> getLoot(LootTable lootTable, LootContext context, Random random) {
        List<ItemStack> items = new ArrayList<>();
        generateLootItems(lootTable, context, random, items::add);
        return items;
    }

    private void generateLootItems(LootTable lootTable, LootContext context, Random random, Consumer<ItemStack> out) {
        if (lootTable.pools() == null) return; // TODO: handle random_sequence
        for (LootTable.Pool pool : lootTable.pools()) {

            // Ensure all conditions are met
            if (fails(pool.conditions(), context)) continue;

            int rolls = pool.rolls().asInt().apply(() -> random);

            // collect all of the loot entries
            List<LootEntry> entries = new ArrayList<>();
            for (LootTable.Pool.Entry entry : pool.entries()) {

                // Ensure all conditions are met
                if (fails(entry.conditions(), context)) continue;

                // now we can add the entries
                addEntries(context, entry, itemGenerator -> {
                    double weight = itemGenerator.weight() == null ? 1 : Objects.requireNonNull(itemGenerator.weight()).asDouble().apply(() -> random);
                    var lootEntries = itemGenerator.apply(datapack, context);
                    for (List<ItemStack> lootEntryItems : lootEntries) {
                        entries.add(new LootEntry(itemGenerator.functions(), lootEntryItems, weight));
                    }
                });
            }

            // if there is no entries, we can skip this pool
            if (entries.isEmpty()) continue;

            // now that we have all the entries, we can roll for them
            double totalWeight = entries.stream().mapToDouble(LootEntry::weight).sum();
            for (int i = 0; i < rolls; i++) {
                LootEntry chosenLootEntry = null;
                double roll = random.nextDouble() * totalWeight;
                for (LootEntry lootEntry : entries) {
                    roll -= lootEntry.weight();
                    if (roll <= 0) {
                        chosenLootEntry = lootEntry;
                        break;
                    }
                }
                Objects.requireNonNull(chosenLootEntry);

                // we now have the loot entry, we need to apply the loot functions
                LootEntry finalChosenLootEntry = chosenLootEntry;
                chosenLootEntry.items()
                        .stream()
                        // loot entry functions
                        .map(item -> {
                            if (finalChosenLootEntry.functions() == null) return item;
                            for (LootFunction function : finalChosenLootEntry.functions()) {
                                item = function.apply(new LootFunctionContext(random, item, context));
                            }
                            return item;
                        })
                        // pool functions
                        .map(item -> {
                            if (pool.functions() == null) return item;
                            for (LootFunction function : pool.functions()) {
                                item = function.apply(new LootFunctionContext(random, item, context));
                            }
                            return item;
                        })
                        // table functions
                        .map(item -> {
                            if (lootTable.functions() == null) return item;
                            for (LootFunction function : lootTable.functions()) {
                                item = function.apply(new LootFunctionContext(random, item, context));
                            }
                            return item;
                        })
                        // add all of the items to the list
                        .forEach(out);
            }
        }
    }

    private record LootFunctionContext(RandomGenerator random, ItemStack itemStack, LootContext context) implements LootFunction.Context {
        @Override
        public <T> @Nullable T get(Trait<T> trait) {
            return context.get(trait);
        }
    }

    private static boolean fails(@Nullable List<Predicate> predicates, LootContext context) {
        if (predicates == null) return false;
        for (Predicate predicate : predicates) {
            if (!predicate.test(context)) {
                return true;
            }
        }
        return false;
    }

    private void addEntries(LootContext context, LootTable.Pool.Entry entry, Consumer<LootTable.Pool.Entry.ItemGenerator> out) {
        if (fails(entry.conditions(), context)) return;
        switch (entry.type().toString()) {
            case "minecraft:item", "minecraft:tag", "minecraft:dynamic", "minecraft:empty" -> out.accept((LootTable.Pool.Entry.ItemGenerator) entry);
            case "minecraft:loot_table" -> // TODO: recursive loot tables
                    Logger.debug("Recursive loot tables are not yet supported");
            case "minecraft:group" -> {
                LootTable.Pool.Entry.Group group = (LootTable.Pool.Entry.Group) entry;
                for (LootTable.Pool.Entry groupEntry : group.children()) {
                    addEntries(context, groupEntry, out);
                }
            }
            case "minecraft:alternatives" -> {
                LootTable.Pool.Entry.Alternatives alternatives = (LootTable.Pool.Entry.Alternatives) entry;
                for (LootTable.Pool.Entry alternative : alternatives.children()) {
                    if (fails(alternative.conditions(), context)) continue;
                    addEntries(context, alternative, out);
                    break;
                }
            }
            case "minecraft:sequence" -> {
                LootTable.Pool.Entry.Sequence sequence = (LootTable.Pool.Entry.Sequence) entry;
                for (LootTable.Pool.Entry alternative : sequence.children()) {
                    if (fails(alternative.conditions(), context)) break;
                    addEntries(context, alternative, out);
                }
            }
        }
    }
}
