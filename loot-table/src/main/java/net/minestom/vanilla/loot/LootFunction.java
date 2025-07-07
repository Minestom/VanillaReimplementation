package net.minestom.vanilla.loot;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.util.RGBLike;
import net.minestom.server.MinecraftServer;
import net.minestom.server.ServerFlag;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.Result;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.codec.Transcoder;
import net.minestom.server.color.Color;
import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponentMap;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.*;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.attribute.AttributeModifier;
import net.minestom.server.entity.attribute.AttributeOperation;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.book.FilteredText;
import net.minestom.server.item.component.*;
import net.minestom.server.item.enchant.Enchantment;
import net.minestom.server.item.instrument.Instrument;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.potion.PotionType;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.registry.RegistryTag;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.Either;
import net.minestom.vanilla.loot.util.*;
import net.minestom.vanilla.loot.util.nbt.NBTPath;
import net.minestom.vanilla.loot.util.nbt.NBTReference;
import net.minestom.vanilla.loot.util.nbt.NBTUtils;
import net.minestom.vanilla.loot.util.predicate.ItemPredicate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A function that allows loot to pass through it, potentially making modifications.
 */
@SuppressWarnings("UnstableApiUsage")
public interface LootFunction {

    @NotNull Codec<LootFunction> CODEC = makeCodec().orElse(makeCodec().list().transform(Sequence::new, seq -> ((Sequence) seq).functions()));

    private static StructCodec<LootFunction> makeCodec() {
        return Codec.RegistryTaggedUnion(registries -> {
            class Holder {
                static final @NotNull DynamicRegistry<StructCodec<? extends LootFunction>> CODEC = createDefaultRegistry();
            }
            return Holder.CODEC;
        }, LootFunction::codec, "function");
    }

    static @NotNull DynamicRegistry<StructCodec<? extends LootFunction>> createDefaultRegistry() {
        final DynamicRegistry<StructCodec<? extends LootFunction>> registry = DynamicRegistry.create(Key.key("loot_functions"));
        registry.register("apply_bonus", ApplyBonus.CODEC);
        registry.register("copy_components", CopyComponents.CODEC);
        registry.register("copy_custom_data", CopyCustomData.CODEC);
        registry.register("copy_name", CopyName.CODEC);
        registry.register("copy_state", CopyState.CODEC);
        registry.register("enchanted_count_increase", EnchantedCountIncrease.CODEC);
        registry.register("enchant_randomly", EnchantRandomly.CODEC);
        registry.register("enchant_with_levels", EnchantWithLevels.CODEC);
        registry.register("exploration_map", ExplorationMap.CODEC);
        registry.register("explosion_decay", ExplosionDecay.CODEC);
        registry.register("fill_player_head", FillPlayerHead.CODEC);
        registry.register("filtered", Filtered.CODEC);
        registry.register("furnace_smelt", FurnaceSmelt.CODEC);
        registry.register("limit_count", LimitCount.CODEC);
        registry.register("modify_contents", ModifyContents.CODEC);
        registry.register("reference", Reference.CODEC);
        registry.register("set_attributes", SetAttributes.CODEC);
        registry.register("set_banner_pattern", SetBannerPattern.CODEC);
        registry.register("set_book_cover", SetBookCover.CODEC);
        registry.register("set_components", SetComponents.CODEC);
        registry.register("set_contents", SetContents.CODEC);
        registry.register("set_count", SetCount.CODEC);
        registry.register("set_custom_data", SetCustomData.CODEC);
        registry.register("set_custom_model_data", SetCustomModelData.CODEC);
        registry.register("set_damage", SetDamage.CODEC);
        registry.register("set_enchantments", SetEnchantments.CODEC);
        registry.register("set_firework_explosion", SetFireworkExplosion.CODEC);
        registry.register("set_fireworks", SetFireworks.CODEC);
        registry.register("set_instrument", SetInstrument.CODEC);
        registry.register("set_item", SetItem.CODEC);
        registry.register("set_loot_table", SetLootTable.CODEC);
        registry.register("set_lore", SetLore.CODEC);
        registry.register("set_name", SetName.CODEC);
        registry.register("set_ominous_bottle_amplifier", SetOminousBottleAmplifier.CODEC);
        registry.register("set_potion", SetPotion.CODEC);
        registry.register("set_stew_effect", SetStewEffect.CODEC);
        registry.register("set_writable_book_pages", SetWritableBookPages.CODEC);
        registry.register("set_written_book_pages", SetWrittenBookPages.CODEC);
        registry.register("sequence", Sequence.CODEC);
        registry.register("toggle_tooltips", ToggleTooltips.CODEC);
        return registry;
    }

    /**
     * Performs any mutations on the provided object and returns the result.
     * @param input the input item to this function
     * @param context the context object, to use if required
     * @return the modified form of the input
     */
    @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context);

    /**
     * @return the codec that can encode this function
     */
    @NotNull StructCodec<? extends LootFunction> codec();

    /**
     * Applies each function to the given item consecutively.
     * @param functions the functions to apply
     * @param item the item to modify
     * @param context the context to use
     * @return the modified item
     */
    static @NotNull ItemStack apply(@NotNull Collection<LootFunction> functions, @NotNull ItemStack item, @NotNull LootContext context) {
        for (LootFunction function : functions) {
            item = function.apply(item, context);
        }
        return item;
    }

    /**
     * Applies each function to each of the given items consecutively.
     * @param functions the functions to apply
     * @param items the items to modify
     * @param context the context to use
     * @return the modified items
     */
    static @NotNull List<ItemStack> apply(@NotNull Collection<LootFunction> functions, @NotNull List<ItemStack> items, @NotNull LootContext context) {
        List<ItemStack> newItems = new ArrayList<>(items.size());
        for (ItemStack item : items) {
            newItems.add(LootFunction.apply(functions, item, context));
        }
        return newItems;
    }

    record ApplyBonus(@NotNull List<LootPredicate> predicates, @NotNull RegistryKey<Enchantment> enchantment, @NotNull Formula.Wrapper formula) implements LootFunction {
        public static final @NotNull StructCodec<ApplyBonus> CODEC = StructCodec.struct(
                "conditions", LootPredicate.CODEC.list().optional(List.of()), ApplyBonus::predicates,
                "enchantment", RegistryKey.codec(Registries::enchantment), ApplyBonus::enchantment,
                StructCodec.INLINE, Formula.CODEC, ApplyBonus::formula,
                ApplyBonus::new
        );

        public sealed interface Formula {

            enum FormulaType {
                BINOMIAL_WITH_BONUS_COUNT,
                ORE_DROPS,
                UNIFORM_BONUS_COUNT,
            }

            record Wrapper(Formula parameters) {
                @SuppressWarnings("unchecked")
                private static <T extends Formula> StructCodec<Wrapper> wrap(@NotNull StructCodec<T> codec) {
                    return StructCodec.struct(
                            "parameters", codec.transform(a->a,a->(T)a), Wrapper::parameters,
                            Wrapper::new
                    );
                }

                private static final StructCodec<Wrapper> BINOMIAL_WITH_BONUS_COUNT = wrap(BinomialWithBonusCount.CODEC);
                private static final StructCodec<Wrapper> ORE_DROPS = new StructCodec<>() {
                    @Override
                    public @NotNull <D> Result<Wrapper> decodeFromMap(@NotNull Transcoder<D> coder, Transcoder.@NotNull MapLike<D> map) {
                        return new Result.Ok<>(new Wrapper(new OreDrops()));
                    }

                    @Override
                    public @NotNull <D> Result<D> encodeToMap(@NotNull Transcoder<D> coder, @NotNull Wrapper value, Transcoder.@NotNull MapBuilder<D> map) {
                        return new Result.Ok<>(map.build());
                    }
                };
                private static final StructCodec<Wrapper> UNIFORM_BONUS_COUNT = wrap(UniformBonusCount.CODEC);

                public static @NotNull StructCodec<Wrapper> codec(@NotNull FormulaType type) {
                    return switch (type) {
                        case BINOMIAL_WITH_BONUS_COUNT -> BINOMIAL_WITH_BONUS_COUNT;
                        case ORE_DROPS -> ORE_DROPS;
                        case UNIFORM_BONUS_COUNT -> UNIFORM_BONUS_COUNT;
                    };
                }
            }

            @NotNull Codec<Wrapper> CODEC = Codec.KEY.transform(key -> switch (key.asString()) {
                case "minecraft:binomial_with_bonus_count" -> FormulaType.BINOMIAL_WITH_BONUS_COUNT;
                case "minecraft:ore_drops" -> FormulaType.ORE_DROPS;
                case "minecraft:uniform_bonus_count" -> FormulaType.UNIFORM_BONUS_COUNT;
                default -> throw new IllegalArgumentException();
            }, type -> Key.key(type.toString().toLowerCase())).unionType("formula", Wrapper::codec, (Wrapper formula) -> switch (formula.parameters()) {
                case BinomialWithBonusCount ignored -> FormulaType.BINOMIAL_WITH_BONUS_COUNT;
                case OreDrops ignored -> FormulaType.ORE_DROPS;
                case UniformBonusCount ignored -> FormulaType.UNIFORM_BONUS_COUNT;
            });

            int calculate(@NotNull Random random, int count, int level);

            record UniformBonusCount(int bonusMultiplier) implements Formula {
                public static final @NotNull StructCodec<UniformBonusCount> CODEC = StructCodec.struct(
                        "bonusMultiplier", StructCodec.INT, UniformBonusCount::bonusMultiplier,
                        UniformBonusCount::new
                );

                @Override
                public int calculate(@NotNull Random random, int count, int level) {
                    return count + random.nextInt(bonusMultiplier * level + 1);
                }
            }

            record OreDrops() implements Formula {
                public static final @NotNull StructCodec<OreDrops> CODEC = StructCodec.struct(OreDrops::new);

                @Override
                public int calculate(@NotNull Random random, int count, int level) {
                    if (level <= 0) return count;

                    return count * Math.max(1, random.nextInt(level + 2));
                }
            }

            record BinomialWithBonusCount(float probability, int extra) implements Formula {
                public static final @NotNull StructCodec<BinomialWithBonusCount> CODEC = StructCodec.struct(
                        "probability", Codec.FLOAT, BinomialWithBonusCount::probability,
                        "extra", Codec.INT, BinomialWithBonusCount::extra,
                        BinomialWithBonusCount::new
                );

                @Override
                public int calculate(@NotNull Random random, int count, int level) {
                    for (int i = 0; i < extra + level; i++) {
                        if (random.nextFloat() < probability) {
                            count++;
                        }
                    }

                    return count;
                }
            }

        }

        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            ItemStack tool = context.get(LootContext.TOOL);
            if (tool == null) return input;

            int level = EnchantmentUtils.level(tool, enchantment);
            int newCount = formula.parameters().calculate(context.require(LootContext.RANDOM), input.amount(), level);

            return input.withAmount(newCount);
        }

        @Override
        public @NotNull StructCodec<? extends LootFunction> codec() {
            return CODEC;
        }
    }

    record CopyComponents(@NotNull List<LootPredicate> predicates, @NotNull RelevantTarget source,
                          @Nullable List<DataComponent<?>> include, @Nullable List<DataComponent<?>> exclude) implements LootFunction {
        public static final @NotNull StructCodec<CopyComponents> CODEC = StructCodec.struct(
                "conditions", LootPredicate.CODEC.list().optional(List.of()), CopyComponents::predicates,
                "source", RelevantTarget.CODEC, CopyComponents::source,
                "include", Codec.KEY.<DataComponent<?>>transform(DataComponent::fromKey, DataComponent::key).list().optional(), CopyComponents::include,
                "exclude", Codec.KEY.<DataComponent<?>>transform(DataComponent::fromKey, DataComponent::key).list().optional(), CopyComponents::exclude,
                CopyComponents::new
        );

        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            if (!LootPredicate.all(predicates, context)) return input;

            // TODO: Incomplete
            throw new UnsupportedOperationException("TODO: Implement Tag<DataComponentMap> for blocks.");
        }

        @Override
        public @NotNull StructCodec<? extends LootFunction> codec() {
            return CODEC;
        }
    }
    
    record CopyCustomData(@NotNull List<LootPredicate> predicates, @NotNull LootNBT source, @NotNull List<Operation> ops) implements LootFunction {
        public static final @NotNull StructCodec<CopyCustomData> CODEC = StructCodec.struct(
                "conditions", LootPredicate.CODEC.list().optional(List.of()), CopyCustomData::predicates,
                "source", LootNBT.CODEC, CopyCustomData::source,
                "ops", Operation.CODEC.list(), CopyCustomData::ops,
                CopyCustomData::new
        );

        public record Operation(@NotNull NBTPath source, @NotNull NBTPath target, @NotNull Operator op) {
            public static final @NotNull StructCodec<Operation> CODEC = StructCodec.struct(
                    "source", NBTPath.CODEC, Operation::source,
                    "target", NBTPath.CODEC, Operation::target,
                    "op", Operator.SERIALIZER, Operation::op,
                    Operation::new
            );

            public void execute(@NotNull NBTReference nbt, @NotNull BinaryTag sourceTag) {
                List<BinaryTag> nbts = new ArrayList<>();
                source.get(sourceTag).forEach(ref -> nbts.add(ref.get()));

                if (nbts.isEmpty()) return;
                op.merge(nbt, target, nbts);
            }
        }

        public enum Operator {
            REPLACE() {
                @Override
                public void merge(@NotNull NBTReference nbt, @NotNull NBTPath target, @NotNull List<BinaryTag> source) {
                    target.set(nbt, source.getLast());
                }
            },
            APPEND() {
                @Override
                public void merge(@NotNull NBTReference nbt, @NotNull NBTPath target, @NotNull List<BinaryTag> source) {
                    List<NBTReference> nbts = target.getWithDefaults(nbt, ListBinaryTag::empty);

                    for (var ref : nbts) {
                        source.forEach(ref::listAdd);
                    }
                }
            },
            MERGE() {
                @Override
                public void merge(@NotNull NBTReference nbt, @NotNull NBTPath target, @NotNull List<BinaryTag> source) {
                    List<NBTReference> nbts = target.getWithDefaults(nbt, CompoundBinaryTag::empty);

                    for (var ref : nbts) {
                        if (ref.get() instanceof CompoundBinaryTag compound) {
                            for (var nbt2 : source) {
                                if (nbt2 instanceof CompoundBinaryTag compound2) {
                                    ref.set(NBTUtils.merge(compound, compound2));
                                }
                            }
                        }
                    }
                }
            };

            public static final @NotNull Codec<Operator> SERIALIZER = Codec.Enum(Operator.class);

            public abstract void merge(@NotNull NBTReference nbt, @NotNull NBTPath target, @NotNull List<BinaryTag> source);
        }

        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            if (!LootPredicate.all(predicates, context)) return input;

            BinaryTag sourceNBT = source.getNBT(context);
            if (sourceNBT == null) return input;

            NBTReference targetNBT = NBTReference.of(input.get(DataComponents.CUSTOM_DATA, CustomData.EMPTY).nbt());

            for (Operation operation : ops) {
                operation.execute(targetNBT, sourceNBT);
            }

            if (targetNBT.get() instanceof CompoundBinaryTag compound) {
                return input.with(DataComponents.CUSTOM_DATA, new CustomData(compound));
            } else {
                return input;
            }
        }

        @Override
        public @NotNull StructCodec<? extends LootFunction> codec() {
            return CODEC;
        }
    }
    
    record CopyName(@NotNull List<LootPredicate> predicates, @NotNull RelevantTarget source) implements LootFunction {
        public static final @NotNull StructCodec<CopyName> CODEC = StructCodec.struct(
                "conditions", LootPredicate.CODEC.list().optional(List.of()), CopyName::predicates,
                "source", RelevantTarget.CODEC, CopyName::source,
                CopyName::new
        );

        private static final Tag<Component> CUSTOM_NAME = Tag.Component("CustomName");

        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            if (!LootPredicate.all(predicates, context)) return input;

            Object key = context.get(source.key());

            Component customName;
            if (key instanceof Entity entity && entity.getCustomName() != null) {
                customName = entity.getCustomName();
            } else if (key instanceof Block block && block.hasTag(CUSTOM_NAME)) {
                customName = block.getTag(CUSTOM_NAME);
            } else {
                return input;
            }

            return input.with(DataComponents.CUSTOM_NAME, customName);
        }

        @Override
        public @NotNull StructCodec<? extends LootFunction> codec() {
            return CODEC;
        }
    }

    record CopyState(@NotNull List<LootPredicate> predicates, @NotNull Block block, @NotNull List<String> properties) implements LootFunction {
        public static final @NotNull StructCodec<CopyState> CODEC = StructCodec.struct(
                "conditions", LootPredicate.CODEC.list().optional(List.of()), CopyState::predicates,
                "block", Codec.KEY.transform(Block::fromKey, Block::key), CopyState::block,
                "properties", Codec.STRING.list(), CopyState::properties,
                CopyState::new
        );

        public CopyState {
            List<String> props = new ArrayList<>(properties);
            props.removeIf(name -> !block.properties().containsKey(name));
            properties = List.copyOf(props);
        }

        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            if (!LootPredicate.all(predicates, context)) return input;

            Block block = context.get(LootContext.BLOCK_STATE);
            if (block == null) return input;

            ItemBlockState irritableBowelSyndrome = input.get(DataComponents.BLOCK_STATE, ItemBlockState.EMPTY);

            if (!block.key().equals(this.block.key())) return input;

            for (var prop : properties) {
                @Nullable String value = block.getProperty(prop);
                if (value == null) continue;

                irritableBowelSyndrome = irritableBowelSyndrome.with(prop, value);
            }

            return input.with(DataComponents.BLOCK_STATE, irritableBowelSyndrome);
        }

        @Override
        public @NotNull StructCodec<? extends LootFunction> codec() {
            return CODEC;
        }
    }

    record EnchantedCountIncrease(@NotNull List<LootPredicate> predicates, @NotNull RegistryKey<Enchantment> enchantment,
                                  @NotNull LootNumber count, @Nullable Integer limit) implements LootFunction {
        public static final @NotNull StructCodec<EnchantedCountIncrease> CODEC = StructCodec.struct(
                "conditions", LootPredicate.CODEC.list().optional(List.of()), EnchantedCountIncrease::predicates,
                "enchantment", RegistryKey.codec(Registries::enchantment), EnchantedCountIncrease::enchantment,
                "count", LootNumber.CODEC, EnchantedCountIncrease::count,
                "limit", Codec.INT.optional(), EnchantedCountIncrease::limit,
                EnchantedCountIncrease::new
        );

        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            if (!LootPredicate.all(predicates, context)) return input;

            Entity attacker = context.get(LootContext.ATTACKING_ENTITY);
            int level = EnchantmentUtils.level(attacker, enchantment);

            if (level == 0) return input;

            int newAmount = input.amount() + level * count.getInt(context);

            return input.withAmount(limit != null ? Math.min(limit, newAmount) : newAmount);
        }

        @Override
        public @NotNull StructCodec<? extends LootFunction> codec() {
            return CODEC;
        }
    }

    record EnchantRandomly(@NotNull List<LootPredicate> predicates, @Nullable RegistryTag<Enchantment> options, boolean onlyCompatible) implements LootFunction {
        public static final @NotNull StructCodec<EnchantRandomly> CODEC = StructCodec.struct(
                "conditions", LootPredicate.CODEC.list().optional(List.of()), EnchantRandomly::predicates,
                "options", RegistryTag.codec(Registries::enchantment).optional(), EnchantRandomly::options,
                "only_compatible", Codec.BOOLEAN.optional(true), EnchantRandomly::onlyCompatible,
                EnchantRandomly::new
        );

        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            var reg = MinecraftServer.getEnchantmentRegistry();

            List<RegistryKey<Enchantment>> values = new ArrayList<>();
            (options == null ? reg.keys() : options).forEach(values::add);

            if (onlyCompatible && !input.material().equals(Material.BOOK)) {
                values.removeIf(ench -> !reg.get(ench).supportedItems().contains(input.material()));
            }

            if (values.isEmpty()) return input;

            Random rng = context.require(LootContext.RANDOM);

            RegistryKey<Enchantment> chosen = values.get(rng.nextInt(values.size()));

            int level = rng.nextInt(reg.get(chosen).maxLevel() + 1);

            return EnchantmentUtils.modifyItem(input, map -> map.put(chosen, level));
        }

        @Override
        public @NotNull StructCodec<? extends LootFunction> codec() {
            return CODEC;
        }
    }

    record EnchantWithLevels(@NotNull List<LootPredicate> predicates, @NotNull LootNumber levels, @Nullable RegistryTag<Enchantment> options) implements LootFunction {
        public static final @NotNull StructCodec<EnchantWithLevels> CODEC = StructCodec.struct(
                "conditions", LootPredicate.CODEC.list().optional(List.of()), EnchantWithLevels::predicates,
                "levels", LootNumber.CODEC, EnchantWithLevels::levels,
                "options", RegistryTag.codec(Registries::enchantment).optional(), EnchantWithLevels::options,
                EnchantWithLevels::new
        );

        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            if (!LootPredicate.all(predicates, context)) return input;

            if (true) throw new UnsupportedOperationException("TODO: Implement enchanting (Random, ItemStack, int levels, @Nullable RegistryTag<Enchantment> options -> ItemStack)");
            return null;
        }

        @Override
        public @NotNull StructCodec<? extends LootFunction> codec() {
            return CODEC;
        }
    }

    record ExplorationMap(@NotNull List<LootPredicate> predicates) implements LootFunction {
        public static final @NotNull StructCodec<ExplorationMap> CODEC = StructCodec.struct(
                "conditions", LootPredicate.CODEC.list().optional(List.of()), ExplorationMap::predicates,
                ExplorationMap::new
        );

        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            if (!LootPredicate.all(predicates, context)) return input;

            // TODO: Incomplete
            throw new UnsupportedOperationException("TODO: Implement ExplorationMap functionality and serialization");
        }

        @Override
        public @NotNull StructCodec<? extends LootFunction> codec() {
            return CODEC;
        }
    }

    record ExplosionDecay(@NotNull List<LootPredicate> predicates) implements LootFunction {
        public static final @NotNull StructCodec<ExplosionDecay> CODEC = StructCodec.struct(
                "conditions", LootPredicate.CODEC.list().optional(List.of()), ExplosionDecay::predicates,
                ExplosionDecay::new
        );

        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            if (!LootPredicate.all(predicates, context)) return input;

            Float radius = context.get(LootContext.EXPLOSION_RADIUS);
            if (radius == null) return input;

            Random random = context.require(LootContext.RANDOM);

            float chance = 1 / radius;
            int trials = input.amount();

            int newAmount = 0;

            for (int i = 0; i < trials; i++) {
                if (random.nextFloat() <= chance) {
                    newAmount++;
                }
            }

            return input.withAmount(newAmount);
        }

        @Override
        public @NotNull StructCodec<? extends LootFunction> codec() {
            return CODEC;
        }
    }

    record FillPlayerHead(@NotNull List<LootPredicate> predicates, @NotNull RelevantEntity entity) implements LootFunction {
        public static final @NotNull StructCodec<FillPlayerHead> CODEC = StructCodec.struct(
                "conditions", LootPredicate.CODEC.list().optional(List.of()), FillPlayerHead::predicates,
                "entity", RelevantEntity.CODEC, FillPlayerHead::entity,
                FillPlayerHead::new
        );

        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            if (!LootPredicate.all(predicates, context)) return input;

            if (!input.material().equals(Material.PLAYER_HEAD)) return input;

            if (!(context.get(entity.key()) instanceof Player player)) return input;

            PlayerSkin skin = player.getSkin();
            if (skin == null) return input;

            return input.with(DataComponents.PROFILE, new HeadProfile(skin));
        }

        @Override
        public @NotNull StructCodec<? extends LootFunction> codec() {
            return CODEC;
        }
    }
    
    record Filtered(@NotNull List<LootPredicate> predicates, @NotNull ItemPredicate predicate, @NotNull LootFunction modifier) implements LootFunction {
        public static final @NotNull StructCodec<Filtered> CODEC = StructCodec.struct(
                "conditions", LootPredicate.CODEC.list().optional(List.of()), Filtered::predicates,
                "item_filter", ItemPredicate.CODEC, Filtered::predicate,
                "modifier", LootFunction.CODEC, Filtered::modifier,
                Filtered::new
        );

        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            return LootPredicate.all(predicates, context) && predicate.test(input, context) ?
                    modifier.apply(input, context) : input;
        }

        @Override
        public @NotNull StructCodec<? extends LootFunction> codec() {
            return CODEC;
        }
    }
    
    record FurnaceSmelt(@NotNull List<LootPredicate> predicates) implements LootFunction {
        public static final @NotNull StructCodec<FurnaceSmelt> CODEC = StructCodec.struct(
                "conditions", LootPredicate.CODEC.list().optional(List.of()), FurnaceSmelt::predicates,
                FurnaceSmelt::new
        );

        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            if (!LootPredicate.all(predicates, context)) return input;

            if (true) throw new UnsupportedOperationException("TODO: Implement smelting (ItemStack -> ItemStack)");
            ItemStack smelted = null;

            return smelted != null ? smelted.withAmount(input.amount()) : input;
        }

        @Override
        public @NotNull StructCodec<? extends LootFunction> codec() {
            return CODEC;
        }
    }

    record LimitCount(@NotNull List<LootPredicate> predicates, @NotNull LootNumberRange limit) implements LootFunction {
        public static final @NotNull StructCodec<LimitCount> CODEC = StructCodec.struct(
                "conditions", LootPredicate.CODEC.list().optional(List.of()), LimitCount::predicates,
                "limit", LootNumberRange.CODEC, LimitCount::limit,
                LimitCount::new
        );

        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            if (!LootPredicate.all(predicates, context)) return input;
            return input.withAmount(i -> (int) limit.limit(context, i));
        }

        @Override
        public @NotNull StructCodec<? extends LootFunction> codec() {
            return CODEC;
        }
    }

    record ModifyContents(@NotNull List<LootPredicate> predicates, @NotNull List<LootFunction> modifier,
                          @NotNull DataComponent<List<ItemStack>> component) implements LootFunction {
        private static final @NotNull Map<Key, DataComponent<List<ItemStack>>> NAMED_CONTAINERS =
                Stream.of(DataComponents.CONTAINER, DataComponents.BUNDLE_CONTENTS, DataComponents.CHARGED_PROJECTILES)
                        .collect(Collectors.toMap(DataComponent::key, Function.identity()));

        private static final @NotNull Codec<DataComponent<List<ItemStack>>> CONTAINER = Codec.KEY.transform(NAMED_CONTAINERS::get, DataComponent::key);

        public static final @NotNull StructCodec<ModifyContents> CODEC = StructCodec.struct(
                "conditions", LootPredicate.CODEC.list().optional(List.of()), ModifyContents::predicates,
                "modifier", LootFunction.CODEC.list(), ModifyContents::modifier,
                "component", CONTAINER, ModifyContents::component,
                ModifyContents::new
        );

        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            if (!LootPredicate.all(predicates, context)) return input;

            List<ItemStack> items = input.get(component);
            if (items == null) return input;

            List<ItemStack> updated = new ArrayList<>();
            for (ItemStack item : items) {
                updated.add(LootFunction.apply(modifier, item, context));
            }

            return input.with(component, updated);
        }

        @Override
        public @NotNull StructCodec<? extends LootFunction> codec() {
            return CODEC;
        }
    }

    record Reference(@NotNull List<LootPredicate> predicates, @NotNull Key name) implements LootFunction {
        public static final @NotNull StructCodec<Reference> CODEC = StructCodec.struct(
                "conditions", LootPredicate.CODEC.list().optional(List.of()), Reference::predicates,
                "name", Codec.KEY, Reference::name,
                Reference::new
        );

        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            if (!LootPredicate.all(predicates, context)) return input;

            if (true) throw new UnsupportedOperationException("TODO: Implement loot function registry (Key -> @Nullable LootFunction)");
            LootFunction function = null;

            return function != null ? function.apply(input, context) : input;
        }

        @Override
        public @NotNull StructCodec<? extends LootFunction> codec() {
            return CODEC;
        }
    }

    record SetAttributes(@NotNull List<LootPredicate> predicates, @NotNull List<AttributeDirective> modifiers, boolean replace) implements LootFunction {
        public static final @NotNull StructCodec<SetAttributes> CODEC = StructCodec.struct(
                "conditions", LootPredicate.CODEC.list().optional(List.of()), SetAttributes::predicates,
                "modifiers", AttributeDirective.CODEC.list(), SetAttributes::modifiers,
                "replace", Codec.BOOLEAN.optional(true), SetAttributes::replace,
                SetAttributes::new
        );

        public record AttributeDirective(@NotNull Key id, @NotNull Attribute attribute, @NotNull AttributeOperation operation,
                                         @NotNull LootNumber amount, @NotNull List<EquipmentSlot> slots) {

            public static final @NotNull Codec<EquipmentSlot> CUSTOM_SLOT;

            static {
                Function<EquipmentSlot, String> name = slot -> slot.name().toLowerCase(Locale.ROOT).replace("_", "");

                Map<String, EquipmentSlot> named = Stream.of(EquipmentSlot.values()).collect(Collectors.toMap(name, Function.identity()));

                CUSTOM_SLOT = Codec.STRING.transform(string -> Objects.requireNonNull(named.get(string)), name::apply);
            }

            public static final @NotNull Codec<AttributeDirective> CODEC = StructCodec.struct(
                    "id", Codec.KEY, AttributeDirective::id,
                    "attribute", Attribute.CODEC, AttributeDirective::attribute,
                    "operation", AttributeOperation.CODEC, AttributeDirective::operation,
                    "amount", LootNumber.CODEC, AttributeDirective::amount,
                    "slot", CUSTOM_SLOT.listOrSingle(Integer.MAX_VALUE), AttributeDirective::slots,
                    AttributeDirective::new
            );

        }

        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            if (!LootPredicate.all(predicates, context)) return input;

            var component = input.get(DataComponents.ATTRIBUTE_MODIFIERS, AttributeList.EMPTY);

            List<AttributeList.Modifier> list = replace ? new ArrayList<>() : new ArrayList<>(component.modifiers());

            for (var modifier : modifiers) {
                if (modifier.slots().isEmpty()) continue;

                AttributeModifier mod = new AttributeModifier(
                        modifier.id(),
                        modifier.amount().getDouble(context),
                        modifier.operation()
                );

                EquipmentSlot slot = modifier.slots().get(context.require(LootContext.RANDOM).nextInt(modifier.slots().size()));

                EquipmentSlotGroup group = switch (slot) {
                    case MAIN_HAND -> EquipmentSlotGroup.MAIN_HAND;
                    case OFF_HAND -> EquipmentSlotGroup.OFF_HAND;
                    case BOOTS -> EquipmentSlotGroup.FEET;
                    case LEGGINGS -> EquipmentSlotGroup.LEGS;
                    case CHESTPLATE -> EquipmentSlotGroup.CHEST;
                    case HELMET -> EquipmentSlotGroup.HEAD;
                    case BODY -> EquipmentSlotGroup.BODY;
                    case SADDLE -> EquipmentSlotGroup.SADDLE;
                };

                list.add(new AttributeList.Modifier(modifier.attribute(), mod, group));
            }

            return input.with(DataComponents.ATTRIBUTE_MODIFIERS, new AttributeList(list));
        }

        @Override
        public @NotNull StructCodec<? extends LootFunction> codec() {
            return CODEC;
        }
    }

    record SetBannerPattern(@NotNull List<LootPredicate> predicates, @NotNull BannerPatterns patterns, boolean append) implements LootFunction {
        public static final @NotNull StructCodec<SetBannerPattern> CODEC = StructCodec.struct(
                "conditions", LootPredicate.CODEC.list().optional(List.of()), SetBannerPattern::predicates,
                "patterns", BannerPatterns.CODEC, SetBannerPattern::patterns,
                "append", Codec.BOOLEAN, SetBannerPattern::append,
                SetBannerPattern::new
        );

        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            if (!LootPredicate.all(predicates, context)) return input;

            if (append) {
                BannerPatterns patterns = input.get(DataComponents.BANNER_PATTERNS);
                if (patterns != null) {
                    List<BannerPatterns.Layer> layers = new ArrayList<>(patterns.layers());
                    layers.addAll(this.patterns().layers());
                    return input.with(DataComponents.BANNER_PATTERNS, new BannerPatterns(layers));
                }
            }

            return input.with(DataComponents.BANNER_PATTERNS, patterns);
        }

        @Override
        public @NotNull StructCodec<? extends LootFunction> codec() {
            return CODEC;
        }
    }

    record SetBookCover(@NotNull List<LootPredicate> predicates, @Nullable FilteredText<String> title,
                        @Nullable String author, @Nullable Integer generation) implements LootFunction {
        public static final @NotNull StructCodec<SetBookCover> CODEC = StructCodec.struct(
                "conditions", LootPredicate.CODEC.list().optional(List.of()), SetBookCover::predicates,
                "title", FilteredText.STRING_CODEC.optional(), SetBookCover::title,
                "author", Codec.STRING.optional(), SetBookCover::author,
                "generation", Codec.INT.optional(), SetBookCover::generation,
                SetBookCover::new
        );

        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            if (!LootPredicate.all(predicates, context)) return input;

            WrittenBookContent content = input.get(DataComponents.WRITTEN_BOOK_CONTENT, WrittenBookContent.EMPTY);

            WrittenBookContent updated = new WrittenBookContent(
                    title != null ? title : content.title(),
                    author != null ? author : content.author(),
                    generation != null ? generation : content.generation(),
                    content.pages(),
                    content.resolved()
            );

            return input.with(DataComponents.WRITTEN_BOOK_CONTENT, updated);
        }

        @Override
        public @NotNull StructCodec<? extends LootFunction> codec() {
            return CODEC;
        }
    }

    record SetComponents(@NotNull List<LootPredicate> predicates, @NotNull DataComponentMap changes) implements LootFunction {
        public static final @NotNull StructCodec<SetComponents> CODEC = StructCodec.struct(
                "conditions", LootPredicate.CODEC.list().optional(List.of()), SetComponents::predicates,
                "components", DataComponent.PATCH_CODEC, SetComponents::changes,
                SetComponents::new
        );

        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            if (!LootPredicate.all(predicates, context)) return input;

            ItemStack.Builder builder = input.builder();

            // This and .constantGeneric are hacks for until there exists a way to apply a patch to an item
            for (DataComponent.Value entry : changes.entrySet()) {
                constantGeneric(builder, entry.component(), entry.value());
            }

            return builder.build();
        }

        @SuppressWarnings("unchecked")
        private static <T> void constantGeneric(@NotNull ItemStack.Builder builder, @NotNull DataComponent<T> key, Object value) {
            builder.set(key, (T) value);
        }

        @Override
        public @NotNull StructCodec<? extends LootFunction> codec() {
            return CODEC;
        }
    }

    record SetContents(@NotNull List<LootPredicate> predicates, @NotNull List<LootEntry> entries,
                       @NotNull DataComponent<List<ItemStack>> type) implements LootFunction {
        public static final @NotNull StructCodec<SetContents> CODEC = StructCodec.struct(
                "conditions", LootPredicate.CODEC.list().optional(List.of()), SetContents::predicates,
                "modifier", LootEntry.CODEC.list(), SetContents::entries,
                "type", ModifyContents.CONTAINER, SetContents::type,
                SetContents::new
        );

        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            if (!LootPredicate.all(predicates, context)) return input;

            List<ItemStack> contents = new ArrayList<>();

            for (LootEntry entry : entries) {
                for (LootEntry.Choice choice : entry.requestChoices(context)) {
                    contents.addAll(choice.generate(context));
                }
            }

            return input.with(type, contents);
        }

        @Override
        public @NotNull StructCodec<? extends LootFunction> codec() {
            return CODEC;
        }
    }

    record SetCount(@NotNull List<LootPredicate> predicates, @NotNull LootNumber count, boolean add) implements LootFunction {
        public static final @NotNull StructCodec<SetCount> CODEC = StructCodec.struct(
                "conditions", LootPredicate.CODEC.list().optional(List.of()), SetCount::predicates,
                "count", LootNumber.CODEC, SetCount::count,
                "add", Codec.BOOLEAN.optional(false), SetCount::add,
                SetCount::new
        );

        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            if (!LootPredicate.all(predicates, context)) return input;
            return input.withAmount(amount -> (this.add ? amount : 0) + this.count.getInt(context));
        }

        @Override
        public @NotNull StructCodec<? extends LootFunction> codec() {
            return CODEC;
        }
    }

    record SetCustomData(@NotNull List<LootPredicate> predicates, @NotNull CompoundBinaryTag tag) implements LootFunction {
        public static final @NotNull StructCodec<SetCustomData> CODEC = StructCodec.struct(
                "conditions", LootPredicate.CODEC.list().optional(List.of()), SetCustomData::predicates,
                "tag", Codec.NBT_COMPOUND, SetCustomData::tag,
                SetCustomData::new
        );

        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            if (!LootPredicate.all(predicates, context)) return input;

            return input.with(DataComponents.CUSTOM_DATA, new CustomData(tag));
        }

        @Override
        public @NotNull StructCodec<? extends LootFunction> codec() {
            return CODEC;
        }
    }

    record SetCustomModelData(@NotNull List<LootPredicate> predicates, @NotNull LootNumber value) implements LootFunction {
        public static final @NotNull StructCodec<SetCustomModelData> CODEC = StructCodec.struct(
                "conditions", LootPredicate.CODEC.list().optional(List.of()), SetCustomModelData::predicates,
                "value", LootNumber.CODEC, SetCustomModelData::value,
                SetCustomModelData::new
        );

        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            if (!LootPredicate.all(predicates, context)) return input;

            return input;
            // TODO: Incomplete
//            return input.with(ItemComponent.CUSTOM_MODEL_DATA, new CustomModelData(value.getInt(context));
        }

        @Override
        public @NotNull StructCodec<? extends LootFunction> codec() {
            return CODEC;
        }
    }

    record SetDamage(@NotNull List<LootPredicate> predicates, @NotNull LootNumber damage, boolean add) implements LootFunction {
        public static final @NotNull StructCodec<SetDamage> CODEC = StructCodec.struct(
                "conditions", LootPredicate.CODEC.list().optional(List.of()), SetDamage::predicates,
                "damage", LootNumber.CODEC, SetDamage::damage,
                "add", Codec.BOOLEAN.optional(false), SetDamage::add,
                SetDamage::new
        );

        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            if (!LootPredicate.all(predicates, context)) return input;

            int maxDamage = input.get(DataComponents.MAX_DAMAGE, -1);
            if (maxDamage == -1) return input;

            double damage = input.get(DataComponents.DAMAGE, 0) / (double) maxDamage;

            double currentDura = add ? 1 - damage : 0;
            double newDura = Math.max(0, Math.min(1, currentDura + this.damage.getDouble(context)));

            double newDamage = 1 - newDura;

            return input.with(DataComponents.DAMAGE, (int) Math.floor(newDamage * maxDamage));
        }

        @Override
        public @NotNull StructCodec<? extends LootFunction> codec() {
            return CODEC;
        }
    }

    record SetEnchantments(@NotNull List<LootPredicate> predicates, @NotNull Map<RegistryKey<Enchantment>, LootNumber> enchantments, boolean add) implements LootFunction {
        public static final @NotNull StructCodec<SetEnchantments> CODEC = StructCodec.struct(
                "conditions", LootPredicate.CODEC.list().optional(List.of()), SetEnchantments::predicates,
                "enchantments", RegistryKey.codec(Registries::enchantment).mapValue(LootNumber.CODEC), SetEnchantments::enchantments,
                "add", Codec.BOOLEAN.optional(false), SetEnchantments::add,
                SetEnchantments::new
        );

        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            if (!LootPredicate.all(predicates, context)) return input;

            return EnchantmentUtils.modifyItem(input, map -> {
                this.enchantments.forEach((enchantment, number) -> {
                    int count = number.getInt(context);
                    if (add) {
                        count += map.getOrDefault(enchantment, 0);
                    }
                    map.put(enchantment, count);
                });
            });
        }

        @Override
        public @NotNull StructCodec<? extends LootFunction> codec() {
            return CODEC;
        }
    }

    record SetFireworkExplosion(@NotNull List<LootPredicate> predicates, @Nullable FireworkExplosion.Shape shape,
                                @Nullable List<RGBLike> colors, @Nullable List<RGBLike> fadeColors,
                                @Nullable Boolean trail, @Nullable Boolean twinkle) implements LootFunction {
        public static final @NotNull StructCodec<SetFireworkExplosion> CODEC = StructCodec.struct(
                "conditions", LootPredicate.CODEC.list().optional(List.of()), SetFireworkExplosion::predicates,
                "shape", Codec.Enum(FireworkExplosion.Shape.class).optional(), SetFireworkExplosion::shape,
                "colors", Color.CODEC.list().optional(), SetFireworkExplosion::colors,
                "fade_colors", Color.CODEC.list().optional(), SetFireworkExplosion::fadeColors,
                "has_trail", Codec.BOOLEAN.optional(), SetFireworkExplosion::trail,
                "has_twinkle", Codec.BOOLEAN.optional(), SetFireworkExplosion::twinkle,
                SetFireworkExplosion::new
        );

        private static final @NotNull FireworkExplosion DEFAULT = new FireworkExplosion(FireworkExplosion.Shape.SMALL_BALL, List.of(), List.of(), false, false);

        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            if (!LootPredicate.all(predicates, context)) return input;

            FireworkExplosion firework = input.get(DataComponents.FIREWORK_EXPLOSION, DEFAULT);

            FireworkExplosion updated = new FireworkExplosion(
                    this.shape != null ? this.shape : firework.shape(),
                    this.colors != null ? this.colors : firework.colors(),
                    this.fadeColors != null ? this.fadeColors : firework.fadeColors(),
                    this.trail != null ? this.trail : firework.hasTrail(),
                    this.twinkle != null ? this.twinkle : firework.hasTwinkle()
            );

            return input.with(DataComponents.FIREWORK_EXPLOSION, updated);
        }

        @Override
        public @NotNull StructCodec<? extends LootFunction> codec() {
            return CODEC;
        }
    }

    record SetFireworks(@NotNull List<LootPredicate> predicates, @Nullable Integer flightDuration, @NotNull ListOperation.WithValues<FireworkExplosion> explosions) implements LootFunction {
        public static final @NotNull StructCodec<SetFireworks> CODEC = StructCodec.struct(
                "conditions", LootPredicate.CODEC.list().optional(List.of()), SetFireworks::predicates,
                "flight_duration", Codec.INT.optional(), SetFireworks::flightDuration,
                "explosions", ListOperation.WithValues.codec(FireworkExplosion.CODEC), SetFireworks::explosions,
                SetFireworks::new
        );

        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            if (!LootPredicate.all(predicates, context)) return input;

            FireworkList list = input.get(DataComponents.FIREWORKS, FireworkList.EMPTY);

            FireworkList updated = new FireworkList(
                    this.flightDuration != null ? this.flightDuration.byteValue() : list.flightDuration(),
                    explosions.operation().apply(this.explosions.values(), list.explosions())
            );

            return input.with(DataComponents.FIREWORKS, updated);
        }

        @Override
        public @NotNull StructCodec<? extends LootFunction> codec() {
            return CODEC;
        }
    }

    record SetInstrument(@NotNull List<LootPredicate> predicates, @NotNull RegistryTag<Instrument> options) implements LootFunction {
        public static final @NotNull StructCodec<SetInstrument> CODEC = StructCodec.struct(
                "conditions", LootPredicate.CODEC.list().optional(List.of()), SetInstrument::predicates,
                "options", RegistryTag.codec(Registries::instrument), SetInstrument::options,
                SetInstrument::new
        );

        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            if (!LootPredicate.all(predicates, context)) return input;
            if (options.size() == 0) return input;

            int count = context.require(LootContext.RANDOM).nextInt(options.size());

            var it = options.iterator();
            for (int i = 0; i < count; i++) {
                it.next();
            }

            return input.with(DataComponents.INSTRUMENT, new InstrumentComponent(Either.right(it.next())));
        }

        @Override
        public @NotNull StructCodec<? extends LootFunction> codec() {
            return CODEC;
        }
    }

    record SetItem(@NotNull List<LootPredicate> predicates, @NotNull Material item) implements LootFunction {
        public static final @NotNull StructCodec<SetItem> CODEC = StructCodec.struct(
                "conditions", LootPredicate.CODEC.list().optional(List.of()), SetItem::predicates,
                "item", Material.CODEC, SetItem::item,
                SetItem::new
        );

        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            if (!LootPredicate.all(predicates, context)) return input;

            return input.builder().material(item).build();
        }

        @Override
        public @NotNull StructCodec<? extends LootFunction> codec() {
            return CODEC;
        }
    }

    record SetLootTable(@NotNull List<LootPredicate> predicates, @NotNull Key name, long seed) implements LootFunction {
        public static final @NotNull StructCodec<SetLootTable> CODEC = StructCodec.struct(
                "conditions", LootPredicate.CODEC.list().optional(List.of()), SetLootTable::predicates,
                "name", Codec.KEY, SetLootTable::name,
                "seed", Codec.LONG.optional(0L), SetLootTable::seed,
                SetLootTable::new
        );

        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            if (!LootPredicate.all(predicates, context)) return input;
            if (input.isAir()) return input;

            return input.with(DataComponents.CONTAINER_LOOT, new SeededContainerLoot(name.asString(), seed));
        }

        @Override
        public @NotNull StructCodec<? extends LootFunction> codec() {
            return CODEC;
        }
    }

    record SetLore(@NotNull List<LootPredicate> predicates, @NotNull List<Component> lore,
                   @NotNull ListOperation operation, @Nullable RelevantEntity entity) implements LootFunction {
        public static final @NotNull StructCodec<SetLore> CODEC = StructCodec.struct(
                "conditions", LootPredicate.CODEC.list().optional(List.of()), SetLore::predicates,
                "lore", Codec.COMPONENT.list(), SetLore::lore,
                StructCodec.INLINE, ListOperation.CODEC, SetLore::operation,
                "entity", RelevantEntity.CODEC.optional(), SetLore::entity,
                SetLore::new
        );

        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            if (!LootPredicate.all(predicates, context)) return input;

            List<Component> components = input.get(DataComponents.LORE, List.of());

            // TODO: Incomplete
            // TODO: https://minecraft.wiki/w/Raw_JSON_text_format#Component_resolution
            //       This is not used in vanilla so it's fine for now.

            return input.with(DataComponents.LORE, operation.apply(lore, components));
        }

        @Override
        public @NotNull StructCodec<? extends LootFunction> codec() {
            return CODEC;
        }
    }

    record SetName(@NotNull List<LootPredicate> predicates, @Nullable Component name,
                   @Nullable RelevantEntity entity, @NotNull Target target) implements LootFunction {
        public static final @NotNull StructCodec<SetName> CODEC = StructCodec.struct(
                "conditions", LootPredicate.CODEC.list().optional(List.of()), SetName::predicates,
                "name", Codec.COMPONENT.optional(), SetName::name,
                "entity", RelevantEntity.CODEC.optional(), SetName::entity,
                "target", Target.CODEC.optional(Target.CUSTOM_NAME), SetName::target,
                SetName::new
        );

        public enum Target {
            ITEM_NAME("item_name", DataComponents.ITEM_NAME),
            CUSTOM_NAME("custom_name", DataComponents.CUSTOM_NAME);

            public static final @NotNull Codec<Target> CODEC = Codec.Enum(Target.class); // Relies on the enum names themselves being accurate

            private final String id;
            private final DataComponent<Component> component;

            Target(String id, DataComponent<Component> component) {
                this.id = id;
                this.component = component;
            }

            public String id() {
                return id;
            }

            public DataComponent<Component> component() {
                return component;
            }
        }

        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            if (!LootPredicate.all(predicates, context)) return input;

            if (name == null) return input;

            Component component = this.name;
            // TODO: Incomplete
            // TODO: https://minecraft.wiki/w/Raw_JSON_text_format#Component_resolution
            //       This is not used in vanilla so it's fine for now.

            return input.with(target.component(), component);
        }

        @Override
        public @NotNull StructCodec<? extends LootFunction> codec() {
            return CODEC;
        }
    }

    record SetOminousBottleAmplifier(@NotNull List<LootPredicate> predicates, @NotNull LootNumber amplifier) implements LootFunction {
        public static final @NotNull StructCodec<SetOminousBottleAmplifier> CODEC = StructCodec.struct(
                "conditions", LootPredicate.CODEC.list().optional(List.of()), SetOminousBottleAmplifier::predicates,
                "amplifier", LootNumber.CODEC, SetOminousBottleAmplifier::amplifier,
                SetOminousBottleAmplifier::new
        );

        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            if (!LootPredicate.all(predicates, context)) return input;

            int amplifier = Math.max(0, Math.min(this.amplifier.getInt(context), 4));

            return input.with(DataComponents.OMINOUS_BOTTLE_AMPLIFIER, amplifier);
        }

        @Override
        public @NotNull StructCodec<? extends LootFunction> codec() {
            return CODEC;
        }
    }

    record SetPotion(@NotNull List<LootPredicate> predicates, @NotNull Key id) implements LootFunction {
        public static final @NotNull StructCodec<SetPotion> CODEC = StructCodec.struct(
                "conditions", LootPredicate.CODEC.list().optional(List.of()), SetPotion::predicates,
                "id", Codec.KEY, SetPotion::id,
                SetPotion::new
        );

        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            if (!LootPredicate.all(predicates, context)) return input;

            if (id.asString().equals("minecraft:empty")) {
                return input.without(DataComponents.POTION_CONTENTS);
            }

            PotionContents existing = input.get(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
            PotionContents updated = new PotionContents(PotionType.fromKey(id), existing.customColor(), existing.customEffects());

            return input.with(DataComponents.POTION_CONTENTS, updated);
        }

        @Override
        public @NotNull StructCodec<? extends LootFunction> codec() {
            return CODEC;
        }
    }

    record SetStewEffect(@NotNull List<LootPredicate> predicates, @NotNull List<AddedEffect> effects) implements LootFunction {
        public static final @NotNull StructCodec<SetStewEffect> CODEC = StructCodec.struct(
                "conditions", LootPredicate.CODEC.list().optional(List.of()), SetStewEffect::predicates,
                "effects", AddedEffect.CODEC.list(), SetStewEffect::effects,
                SetStewEffect::new
        );

        public record AddedEffect(@NotNull PotionEffect effect, @NotNull LootNumber duration) {
            public static final @NotNull StructCodec<AddedEffect> CODEC = StructCodec.struct(
                    "type", PotionEffect.CODEC, AddedEffect::effect,
                    "duration", LootNumber.CODEC, AddedEffect::duration,
                    AddedEffect::new
            );
        }

        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            if (!LootPredicate.all(predicates, context)) return input;

            if (!Material.SUSPICIOUS_STEW.equals(input.material()) || effects.isEmpty()) return input;

            AddedEffect effect = effects.get(context.require(LootContext.RANDOM).nextInt(effects.size()));

            long duration = effect.duration().getInt(context);
            if (!effect.effect().registry().isInstantaneous()) {
                duration *= ServerFlag.SERVER_TICKS_PER_SECOND;
            }

            SuspiciousStewEffects.Effect added = new SuspiciousStewEffects.Effect(effect.effect(), (int) duration);

            SuspiciousStewEffects current = input.get(DataComponents.SUSPICIOUS_STEW_EFFECTS, SuspiciousStewEffects.EMPTY);
            return input.with(DataComponents.SUSPICIOUS_STEW_EFFECTS, current.with(added));
        }

        @Override
        public @NotNull StructCodec<? extends LootFunction> codec() {
            return CODEC;
        }
    }

    record SetWritableBookPages(@NotNull List<LootPredicate> predicates, @NotNull List<FilteredText<String>> pages, @NotNull ListOperation operation) implements LootFunction {
        public static final @NotNull StructCodec<SetWritableBookPages> CODEC = StructCodec.struct(
                "conditions", LootPredicate.CODEC.list().optional(List.of()), SetWritableBookPages::predicates,
                "pages", FilteredText.STRING_CODEC.list(), SetWritableBookPages::pages,
                StructCodec.INLINE, ListOperation.CODEC, SetWritableBookPages::operation,
                SetWritableBookPages::new
        );

        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            if (!LootPredicate.all(predicates, context)) return input;

            WritableBookContent content = input.get(DataComponents.WRITABLE_BOOK_CONTENT, WritableBookContent.EMPTY);

            return input.with(DataComponents.WRITABLE_BOOK_CONTENT, new WritableBookContent(operation.apply(pages, content.pages())));
        }

        @Override
        public @NotNull StructCodec<? extends LootFunction> codec() {
            return CODEC;
        }
    }

    record SetWrittenBookPages(@NotNull List<LootPredicate> predicates, @NotNull List<FilteredText<Component>> pages, @NotNull ListOperation operation) implements LootFunction {
        public static final @NotNull StructCodec<SetWrittenBookPages> CODEC = StructCodec.struct(
                "conditions", LootPredicate.CODEC.list().optional(List.of()), SetWrittenBookPages::predicates,
                "pages", FilteredText.COMPONENT_CODEC.list(), SetWrittenBookPages::pages,
                StructCodec.INLINE, ListOperation.CODEC, SetWrittenBookPages::operation,
                SetWrittenBookPages::new
        );

        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            if (!LootPredicate.all(predicates, context)) return input;

            WrittenBookContent content = input.get(DataComponents.WRITTEN_BOOK_CONTENT, WrittenBookContent.EMPTY);
            WrittenBookContent updated = new WrittenBookContent(content.title(), content.author(), content.generation(), operation.apply(pages, content.pages()), content.resolved());

            return input.with(DataComponents.WRITTEN_BOOK_CONTENT, updated);
        }

        @Override
        public @NotNull StructCodec<? extends LootFunction> codec() {
            return CODEC;
        }
    }
    
    record Sequence(@NotNull List<LootFunction> functions) implements LootFunction {
        public static final @NotNull StructCodec<Sequence> CODEC = StructCodec.struct(
                "functions", LootFunction.CODEC.list(), Sequence::functions,
                Sequence::new
        );

        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            return LootFunction.apply(functions, input, context);
        }

        @Override
        public @NotNull StructCodec<? extends LootFunction> codec() {
            return CODEC;
        }
    }
    
    record ToggleTooltips(@NotNull List<LootPredicate> predicates, @NotNull Map<DataComponent<?>, Boolean> toggles) implements LootFunction {
        public static final @NotNull StructCodec<ToggleTooltips> CODEC = StructCodec.struct(
                "conditions", LootPredicate.CODEC.list().optional(List.of()), ToggleTooltips::predicates,
                "toggles", DataComponent.CODEC.mapValue(StructCodec.BOOLEAN), ToggleTooltips::toggles,
                ToggleTooltips::new
        );

        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack input, @NotNull LootContext context) {
            if (!LootPredicate.all(predicates, context)) return input;

            TooltipDisplay display = input.get(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.EMPTY);

            for (var entry : toggles.entrySet()) {
                display = entry.getValue() ? display.with(entry.getKey()) : display.without(entry.getKey());
            }

            return input.with(DataComponents.TOOLTIP_DISPLAY, display);
        }

        @Override
        public @NotNull StructCodec<? extends LootFunction> codec() {
            return CODEC;
        }
    }

}
