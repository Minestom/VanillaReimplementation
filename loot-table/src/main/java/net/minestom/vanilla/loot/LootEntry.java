package net.minestom.vanilla.loot;

import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.registry.RegistryTag;
import net.minestom.server.utils.Either;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.ArrayList;
import java.util.List;

/**
 * An entry in a loot table that can generate a list of {@link Choice choices} that each have their own loot and weight.
 */
@SuppressWarnings("UnstableApiUsage")
public interface LootEntry {
    
    @NotNull StructCodec<LootEntry> CODEC = Codec.RegistryTaggedUnion(registries -> {
        class Holder {
            static final @NotNull DynamicRegistry<StructCodec<? extends LootEntry>> CODEC = createDefaultRegistry();
        }
        return Holder.CODEC;
    }, LootEntry::codec, "type");

    static @NotNull DynamicRegistry<StructCodec<? extends LootEntry>> createDefaultRegistry() {
        final DynamicRegistry<StructCodec<? extends LootEntry>> registry = DynamicRegistry.create(Key.key("loot_entries"));
        registry.register("alternatives", Alternatives.CODEC);
        registry.register("dynamic", Dynamic.CODEC);
        registry.register("empty", Empty.CODEC);
        registry.register("group", Group.CODEC);
        registry.register("item", Item.CODEC);
        registry.register("loot_table", LootTable.CODEC);
        registry.register("sequence", Sequence.CODEC);
        registry.register("tag", Tag.CODEC);
        return registry;
    }

    /**
     * Generates any number of possible choices to choose from when generating loot.
     * @param context the context object, to use if required
     * @return a list, with undetermined mutability, containing the options that were generated
     */
    @NotNull List<Choice> requestChoices(@NotNull LootContext context);

    /**
     * @return the codec that can encode this entry
     */
    @NotNull StructCodec<? extends LootEntry> codec();

    /**
     * A choice, generated from an entry, that could potentially be chosen.
     */
    interface Choice extends LootGenerator {

        /**
         * Calculates the weight of this choice, to be used when choosing which choices should be used.
         * This number should not be below 1.<br>
         * When using the result of this method, be aware of the fact that it's valid for implementations of this method
         * to return different values even when the provided context is the identical.
         * @param context the context object, to use if required
         * @return the weight of this choice
         */
        @Range(from = 1L, to = Long.MAX_VALUE) long getWeight(@NotNull LootContext context);

        
        /**
         * A choice that uses the standard method of generating weight - adding the {@link #weight()} to the {@link #quality()}
         * where the quality is multiplied by the provided context's luck ({@link LootContext#LUCK}).
         */
        interface Standard extends Choice {

            /**
             * The weight of this choice. When calculating the final weight, this value is simply added to the result.
             * @return the base weight of this choice
             */
            @Range(from = 1L, to = Long.MAX_VALUE) long weight();

            /**
             * The quality of the choice. When calculating the final weight, this number is multiplied by the context's luck
             * value, which is stored at the key {@link LootContext#LUCK}.
             * @return the quality of the choice
             */
            @Range(from = 0L, to = Long.MAX_VALUE) long quality();

            @Override
            default @Range(from = 1L, to = Long.MAX_VALUE) long getWeight(@NotNull LootContext context) {
                return Math.max(1, (long) Math.floor(weight() + quality() * context.get(LootContext.LUCK, 0d)));
            }

        }

        /**
         * A standard single choice entry that only returns itself when its conditions all succeed.
         */
        interface Single extends LootEntry, LootEntry.Choice, Standard {

            /**
             * @return this choice's predicates
             */
            @NotNull List<LootPredicate> predicates();

            /**
             * Requests choices, returning none if {@link #predicates()} are all true.
             * {@inheritDoc}
             */
            @Override
            default @NotNull List<Choice> requestChoices(@NotNull LootContext context) {
                return LootPredicate.all(predicates(), context) ? List.of(this) : List.of();
            }

        }

    }
    
    record Alternatives(@NotNull List<LootPredicate> predicates, @NotNull List<LootEntry> children) implements LootEntry {
        public static final @NotNull StructCodec<Alternatives> CODEC = StructCodec.struct(
                "conditions", LootPredicate.CODEC.list().optional(List.of()), Alternatives::predicates,
                "children", LootEntry.CODEC.list().optional(List.of()), Alternatives::children,
                Alternatives::new
        );

        @Override
        public @NotNull List<Choice> requestChoices(@NotNull LootContext context) {
            if (!LootPredicate.all(predicates, context)) return List.of();

            for (var entry : this.children()) {
                var options = entry.requestChoices(context);
                if (!options.isEmpty()) {
                    return options;
                }
            }
            return List.of();
        }

        @Override
        public @NotNull StructCodec<? extends LootEntry> codec() {
            return CODEC;
        }
    }

    record Dynamic(@NotNull List<LootPredicate> predicates, @NotNull List<LootFunction> functions,
                   long weight, long quality, @NotNull Key name) implements Choice.Single {
        public static final @NotNull StructCodec<Dynamic> CODEC = StructCodec.struct(
                "conditions", LootPredicate.CODEC.list().optional(List.of()), Dynamic::predicates,
                "functions", LootFunction.CODEC.list().optional(List.of()), Dynamic::functions,
                "weight", Codec.LONG.optional(1L), Dynamic::weight,
                "quality", Codec.LONG.optional(0L), Dynamic::quality,
                "name", Codec.KEY, Dynamic::name,
                Dynamic::new
        );

        private static final net.minestom.server.tag.Tag<List<Material>> DECORATED_POT_SHERDS = net.minestom.server.tag.Tag.String("sherds")
                .map(Key::key, Key::asString)
                .map(Material::fromKey, Material::key)
                .list().defaultValue(List::of);

        private static final net.minestom.server.tag.Tag<List<ItemStack>> CONTAINER_ITEMS = net.minestom.server.tag.Tag.ItemStack("Items").list().defaultValue(List::of);

        @Override
        public @NotNull List<ItemStack> generate(@NotNull LootContext context) {
            Block block = context.get(LootContext.BLOCK_STATE);
            if (block == null) return List.of();

            return switch (name.asString()) {
                case "minecraft:sherds" -> {
                    List<ItemStack> items = new ArrayList<>();
                    for (Material material : block.getTag(DECORATED_POT_SHERDS)) {
                        items.add(ItemStack.of(material));
                    }
                    yield items;
                }
                case "minecraft:contents" -> block.getTag(CONTAINER_ITEMS);
                default -> List.of();
            };
        }

        @Override
        public @NotNull StructCodec<? extends LootEntry> codec() {
            return CODEC;
        }
    }

    record Empty(@NotNull List<LootPredicate> predicates, @NotNull List<LootFunction> functions,
                 long weight, long quality) implements Choice.Single {
        public static final @NotNull StructCodec<Empty> CODEC = StructCodec.struct(
                "conditions", LootPredicate.CODEC.list().optional(List.of()), Empty::predicates,
                "functions", LootFunction.CODEC.list().optional(List.of()), Empty::functions,
                "weight", Codec.LONG.optional(1L), Empty::weight,
                "quality", Codec.LONG.optional(0L), Empty::quality,
                Empty::new
        );

        @Override
        public @NotNull List<ItemStack> generate(@NotNull LootContext context) {
            return List.of();
        }

        @Override
        public @NotNull StructCodec<? extends LootEntry> codec() {
            return CODEC;
        }
    }

    record Group(@NotNull List<LootPredicate> predicates, @NotNull List<LootEntry> children) implements LootEntry {
        public static final @NotNull StructCodec<Group> CODEC = StructCodec.struct(
                "conditions", LootPredicate.CODEC.list().optional(List.of()), Group::predicates,
                "children", LootEntry.CODEC.list().optional(List.of()), Group::children,
                Group::new
        );

        @Override
        public @NotNull List<Choice> requestChoices(@NotNull LootContext context) {
            if (!LootPredicate.all(predicates, context)) return List.of();

            List<Choice> choices = new ArrayList<>();
            for (var entry : this.children()) {
                choices.addAll(entry.requestChoices(context));
            }
            return choices;
        }

        @Override
        public @NotNull StructCodec<? extends LootEntry> codec() {
            return CODEC;
        }
    }

    record Item(@NotNull List<LootPredicate> predicates, @NotNull List<LootFunction> functions,
                long weight, long quality, @NotNull Material name) implements Choice.Single {
        public static final @NotNull StructCodec<Item> CODEC = StructCodec.struct(
                "conditions", LootPredicate.CODEC.list().optional(List.of()), Item::predicates,
                "functions", LootFunction.CODEC.list().optional(List.of()), Item::functions,
                "weight", Codec.LONG.optional(1L), Item::weight,
                "quality", Codec.LONG.optional(0L), Item::quality,
                "name", Material.CODEC, Item::name,
                Item::new
        );

        @Override
        public @NotNull List<ItemStack> generate(@NotNull LootContext context) {
            return List.of(LootFunction.apply(functions, ItemStack.of(name), context));
        }

        @Override
        public @NotNull StructCodec<? extends LootEntry> codec() {
            return CODEC;
        }
    }

    record LootTable(@NotNull List<LootPredicate> predicates, @NotNull List<LootFunction> functions,
                     long weight, long quality, @NotNull Either<Key, net.minestom.vanilla.loot.LootTable> value) implements Choice.Single {
        public static final @NotNull StructCodec<LootTable> CODEC = StructCodec.struct(
                "conditions", LootPredicate.CODEC.list().optional(List.of()), LootTable::predicates,
                "functions", LootFunction.CODEC.list().optional(List.of()), LootTable::functions,
                "weight", Codec.LONG.optional(1L), LootTable::weight,
                "quality", Codec.LONG.optional(0L), LootTable::quality,
                "value", Codec.Either(Codec.KEY, net.minestom.vanilla.loot.LootTable.CODEC), LootTable::value,
                LootTable::new
        );

        @Override
        public @NotNull List<ItemStack> generate(@NotNull LootContext context) {
            var table = switch (value) {
                case Either.Left(Key key) -> throw new UnsupportedOperationException("TODO: Implement loot table registry (Key -> @Nullable LootTable)");
                case Either.Right(net.minestom.vanilla.loot.LootTable right) -> right;
            };

            if (table == null) return List.of();

            return LootFunction.apply(functions, table.generate(context), context);
        }

        @Override
        public @NotNull StructCodec<? extends LootEntry> codec() {
            return CODEC;
        }
    }

    record Sequence(@NotNull List<LootPredicate> predicates, @NotNull List<LootEntry> children) implements LootEntry {
        public static final @NotNull StructCodec<Sequence> CODEC = StructCodec.struct(
                "conditions", LootPredicate.CODEC.list().optional(List.of()), Sequence::predicates,
                "children", LootEntry.CODEC.list().optional(List.of()), Sequence::children,
                Sequence::new
        );

        @Override
        public @NotNull List<Choice> requestChoices(@NotNull LootContext context) {
            if (!LootPredicate.all(predicates, context)) return List.of();

            List<Choice> options = new ArrayList<>();
            for (var entry : this.children()) {
                var choices = entry.requestChoices(context);
                if (choices.isEmpty()) {
                    break;
                }
                options.addAll(choices);
            }
            return options;
        }

        @Override
        public @NotNull StructCodec<? extends LootEntry> codec() {
            return CODEC;
        }
    }

    record Tag(@NotNull List<LootPredicate> predicates, @NotNull List<LootFunction> functions,
               long weight, long quality, @NotNull RegistryTag<Material> name, boolean expand) implements Choice.Single {
        public static final @NotNull StructCodec<Tag> CODEC = StructCodec.struct(
                "conditions", LootPredicate.CODEC.list().optional(List.of()), Tag::predicates,
                "functions", LootFunction.CODEC.list().optional(List.of()), Tag::functions,
                "weight", Codec.LONG.optional(1L), Tag::weight,
                "quality", Codec.LONG.optional(0L), Tag::quality,
                "name", RegistryTag.codec(Registries::material), Tag::name,
                "expand", Codec.BOOLEAN, Tag::expand,
                Tag::new
        );

        @Override
        public @NotNull List<Choice> requestChoices(@NotNull LootContext context) {
            if (!LootPredicate.all(predicates, context)) {
                return List.of();
            } else if (!expand) {
                return List.of(this);
            }

            List<Choice> choices = new ArrayList<>();

            for (RegistryKey<Material> key : name) {
                Material material = MinecraftServer.process().material().get(key);
                if (material == null) continue;

                choices.add(new Choice() {
                    @Override
                    public @Range(from = 1L, to = Long.MAX_VALUE) long getWeight(@NotNull LootContext context) {
                        return Tag.this.getWeight(context);
                    }

                    @Override
                    public @NotNull List<ItemStack> generate(@NotNull LootContext context) {
                        return List.of(ItemStack.of(material));
                    }

                });
            }

            return choices;
        }

        @Override
        public @NotNull List<ItemStack> generate(@NotNull LootContext context) {
            List<ItemStack> items = new ArrayList<>();

            for (RegistryKey<Material> key : name) {
                Material material = MinecraftServer.process().material().get(key);
                if (material == null) continue;

                items.add(LootFunction.apply(functions, ItemStack.of(material), context));
            }

            return items;
        }

        @Override
        public @NotNull StructCodec<? extends LootEntry> codec() {
            return CODEC;
        }
    }

}
