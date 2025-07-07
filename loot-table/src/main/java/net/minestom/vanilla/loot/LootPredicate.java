package net.minestom.vanilla.loot;

import net.kyori.adventure.key.Key;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.Weather;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.predicate.BlockPredicate;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.enchant.Enchantment;
import net.minestom.server.item.enchant.LevelBasedValue;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.RegistryKey;
import net.minestom.vanilla.loot.util.EnchantmentUtils;
import net.minestom.vanilla.loot.util.LootNumberRange;
import net.minestom.vanilla.loot.util.RelevantEntity;
import net.minestom.vanilla.loot.util.predicate.DamageSourcePredicate;
import net.minestom.vanilla.loot.util.predicate.EntityPredicate;
import net.minestom.vanilla.loot.util.predicate.ItemPredicate;
import net.minestom.vanilla.loot.util.predicate.LocationPredicate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * A predicate over a loot context, returning whether or not a given context passes some arbitrary predicate.
 */
@SuppressWarnings("UnstableApiUsage")
public interface LootPredicate extends Predicate<@NotNull LootContext> {

    @NotNull StructCodec<LootPredicate> CODEC = Codec.RegistryTaggedUnion(registries -> {
        class Holder {
            static final @NotNull DynamicRegistry<StructCodec<? extends LootPredicate>> CODEC = createDefaultRegistry();
        }
        return Holder.CODEC;
    }, LootPredicate::codec, "condition");

    static @NotNull DynamicRegistry<StructCodec<? extends LootPredicate>> createDefaultRegistry() {
        final DynamicRegistry<StructCodec<? extends LootPredicate>> registry = DynamicRegistry.create(Key.key("loot_predicates"));
        registry.register("all_of", AllOf.CODEC);
        registry.register("any_of", AnyOf.CODEC);
        registry.register("block_state_property", BlockStateProperty.CODEC);
        registry.register("damage_source_properties", DamageSourceProperties.CODEC);
        registry.register("enchantment_active_check", EnchantmentActiveCheck.CODEC);
        registry.register("entity_properties", EntityProperties.CODEC);
        registry.register("entity_scores", EntityScores.CODEC);
        registry.register("inverted", Inverted.CODEC);
        registry.register("killed_by_player", KilledByPlayer.CODEC);
        registry.register("location_check", LocationCheck.CODEC);
        registry.register("match_tool", MatchTool.CODEC);
        registry.register("random_chance", RandomChance.CODEC);
        registry.register("random_chance_with_enchanted_bonus", RandomChanceWithEnchantedBonus.CODEC);
        registry.register("reference", Reference.CODEC);
        registry.register("survives_explosion", SurvivesExplosion.CODEC);
        registry.register("table_bonus", TableBonus.CODEC);
        registry.register("time_check", TimeCheck.CODEC);
        registry.register("value_check", ValueCheck.CODEC);
        registry.register("weather_check", WeatherCheck.CODEC);
        return registry;
    }

    /**
     * Returns whether or not the provided loot context passes this predicate.
     * @param context the context object, to use if required
     * @return true if the provided loot context is valid according to this predicate
     */
    @Override
    boolean test(@NotNull LootContext context);

    /**
     * @return the codec that can encode this predicate
     */
    @NotNull StructCodec<? extends LootPredicate> codec();

    /**
     * Returns whether or not every given predicate verifies the provided context.
     */
    static boolean all(@NotNull List<LootPredicate> predicates, @NotNull LootContext context) {
        if (predicates.isEmpty()) {
            return true;
        }
        for (var predicate : predicates) {
            if (!predicate.test(context)) {
                return false;
            }
        }
        return true;
    }

    record AllOf(@NotNull List<LootPredicate> terms) implements LootPredicate {
        public static final @NotNull StructCodec<AllOf> CODEC = StructCodec.struct(
                "terms", LootPredicate.CODEC.list(), AllOf::terms,
                AllOf::new
        );

        @Override
        public boolean test(@NotNull LootContext context) {
            return all(terms, context);
        }

        @Override
        public @NotNull StructCodec<? extends LootPredicate> codec() {
            return CODEC;
        }
    }

    record AnyOf(@NotNull List<LootPredicate> terms) implements LootPredicate {
        public static final @NotNull StructCodec<AnyOf> CODEC = StructCodec.struct(
                "terms", LootPredicate.CODEC.list(), AnyOf::terms,
                AnyOf::new
        );

        @Override
        public boolean test(@NotNull LootContext context) {
            if (terms.isEmpty()) {
                return false;
            }
            for (var predicate : terms) {
                if (predicate.test(context)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public @NotNull StructCodec<? extends LootPredicate> codec() {
            return CODEC;
        }
    }

    record BlockStateProperty(@NotNull Key block, @Nullable BlockPredicate properties) implements LootPredicate {
        public static final @NotNull StructCodec<BlockStateProperty> CODEC = StructCodec.struct(
                "block", Codec.KEY, BlockStateProperty::block,
                "properties", BlockPredicate.CODEC.optional(), BlockStateProperty::properties,
                BlockStateProperty::new
        );

        @Override
        public boolean test(@NotNull LootContext context) {
            Block block = context.get(LootContext.BLOCK_STATE);

            return block != null && this.block.equals(block.key()) && (properties == null || properties.test(block));
        }

        @Override
        public @NotNull StructCodec<? extends LootPredicate> codec() {
            return CODEC;
        }
    }

    record DamageSourceProperties(@Nullable DamageSourcePredicate predicate) implements LootPredicate {
        public static final @NotNull StructCodec<DamageSourceProperties> CODEC = StructCodec.struct(
                "predicate", DamageSourcePredicate.CODEC.optional(), DamageSourceProperties::predicate,
                DamageSourceProperties::new
        );

        @Override
        public boolean test(@NotNull LootContext context) {
            Instance world = context.get(LootContext.WORLD);
            Point origin = context.get(LootContext.ORIGIN);
            DamageType damage = context.get(LootContext.DAMAGE_SOURCE);

            if (predicate == null || world == null || origin == null || damage == null) {
                return false;
            }

            return predicate.test(world, origin, damage);
        }

        @Override
        public @NotNull StructCodec<? extends LootPredicate> codec() {
            return CODEC;
        }
    }

    record EnchantmentActiveCheck(boolean active) implements LootPredicate {
        public static final @NotNull StructCodec<EnchantmentActiveCheck> CODEC = StructCodec.struct(
                "active", Codec.BOOLEAN, EnchantmentActiveCheck::active,
                EnchantmentActiveCheck::new
        );

        @Override
        public boolean test(@NotNull LootContext context) {
            return context.require(LootContext.ENCHANTMENT_ACTIVE) == active;
        }

        @Override
        public @NotNull StructCodec<? extends LootPredicate> codec() {
            return CODEC;
        }
    }

    record EntityProperties(@Nullable EntityPredicate predicate, @NotNull RelevantEntity entity) implements LootPredicate {
        public static final @NotNull StructCodec<EntityProperties> CODEC = StructCodec.struct(
                "predicate", EntityPredicate.CODEC, EntityProperties::predicate,
                "entity", RelevantEntity.CODEC, EntityProperties::entity,
                EntityProperties::new
        );

        @Override
        public boolean test(@NotNull LootContext context) {
            Entity entity = context.get(this.entity.key());
            Point origin = context.get(LootContext.ORIGIN);

            return predicate == null || predicate.test(context.require(LootContext.WORLD), origin, entity);
        }

        @Override
        public @NotNull StructCodec<? extends LootPredicate> codec() {
            return CODEC;
        }
    }

    record EntityScores(@NotNull Map<String, LootNumberRange> scores, @NotNull RelevantEntity entity) implements LootPredicate {
        public static final @NotNull StructCodec<EntityScores> CODEC = StructCodec.struct(
                "scores", Codec.STRING.mapValue(LootNumberRange.CODEC), EntityScores::scores,
                "entity", RelevantEntity.CODEC, EntityScores::entity,
                EntityScores::new
        );

        @Override
        public boolean test(@NotNull LootContext context) {
            Entity entity = context.get(this.entity.key());
            if (entity == null) return false;

            for (var entry : scores.entrySet()) {
                if (true) throw new UnsupportedOperationException("TODO: Implement entity scores (Entity entity -> String objective -> @Nullable Integer)");
                Integer score = null;
                if (score == null || !entry.getValue().check(context, score)) {
                    return false;
                }
            }

            return true;
        }

        @Override
        public @NotNull StructCodec<? extends LootPredicate> codec() {
            return CODEC;
        }
    }

    record Inverted(@NotNull LootPredicate term) implements LootPredicate {
        public static final @NotNull StructCodec<Inverted> CODEC = StructCodec.struct(
                "term", LootPredicate.CODEC, Inverted::term,
                Inverted::new
        );

        @Override
        public boolean test(@NotNull LootContext context) {
            return !term.test(context);
        }

        @Override
        public @NotNull StructCodec<? extends LootPredicate> codec() {
            return CODEC;
        }
    }

    record KilledByPlayer() implements LootPredicate {
        public static final @NotNull StructCodec<KilledByPlayer> CODEC = StructCodec.struct(KilledByPlayer::new);

        @Override
        public boolean test(@NotNull LootContext context) {
            return context.has(LootContext.LAST_DAMAGE_PLAYER);
        }

        @Override
        public @NotNull StructCodec<? extends LootPredicate> codec() {
            return CODEC;
        }
    }

    record LocationCheck(@Nullable LocationPredicate predicate, double offsetX, double offsetY, double offsetZ) implements LootPredicate {
        public static final @NotNull StructCodec<LocationCheck> CODEC = StructCodec.struct(
                "predicate", LocationPredicate.CODEC, LocationCheck::predicate,
                "offsetX", Codec.DOUBLE.optional(0D), LocationCheck::offsetX,
                "offsetY", Codec.DOUBLE.optional(0D), LocationCheck::offsetY,
                "offsetZ", Codec.DOUBLE.optional(0D), LocationCheck::offsetZ,
                LocationCheck::new
        );

        @Override
        public boolean test(@NotNull LootContext context) {
            Point origin = context.get(LootContext.ORIGIN);

            if (origin == null) return false;
            if (predicate == null) return true;

            return predicate.test(context.require(LootContext.WORLD), origin.add(offsetX, offsetY, offsetZ));
        }

        @Override
        public @NotNull StructCodec<? extends LootPredicate> codec() {
            return CODEC;
        }
    }

    record MatchTool(@Nullable ItemPredicate predicate) implements LootPredicate {
        public static final @NotNull StructCodec<MatchTool> CODEC = StructCodec.struct(
                "predicate", ItemPredicate.CODEC, MatchTool::predicate,
                MatchTool::new
        );

        @Override
        public boolean test(@NotNull LootContext context) {
            ItemStack tool = context.get(LootContext.TOOL);

            if (tool == null) return false;
            if (predicate == null) return true;

            return predicate.test(tool, context);
        }

        @Override
        public @NotNull StructCodec<? extends LootPredicate> codec() {
            return CODEC;
        }
    }

    record RandomChance(@NotNull LootNumber chance) implements LootPredicate {
        public static final @NotNull StructCodec<RandomChance> CODEC = StructCodec.struct(
                "chance", LootNumber.CODEC, RandomChance::chance,
                RandomChance::new
        );

        @Override
        public boolean test(@NotNull LootContext context) {
            return context.require(LootContext.RANDOM).nextDouble() < chance.getDouble(context);
        }

        @Override
        public @NotNull StructCodec<? extends LootPredicate> codec() {
            return CODEC;
        }
    }

    record RandomChanceWithEnchantedBonus(@NotNull RegistryKey<Enchantment> enchantment, float unenchantedChance, @NotNull LevelBasedValue enchantedChance) implements LootPredicate {
        public static final @NotNull StructCodec<RandomChanceWithEnchantedBonus> CODEC = StructCodec.struct(
                "enchantment", RegistryKey.codec(Registries::enchantment), RandomChanceWithEnchantedBonus::enchantment,
                "unenchanted_chance", Codec.FLOAT, RandomChanceWithEnchantedBonus::unenchantedChance,
                "enchanted_chance", LevelBasedValue.CODEC, RandomChanceWithEnchantedBonus::enchantedChance,
                RandomChanceWithEnchantedBonus::new
        );

        @Override
        public boolean test(@NotNull LootContext context) {
            Entity attacker = context.get(LootContext.ATTACKING_ENTITY);

            int level = EnchantmentUtils.level(attacker, enchantment);

            float chance = level > 0 ? enchantedChance.calc(level) : unenchantedChance;
            return context.require(LootContext.RANDOM).nextFloat() < chance;
        }

        @Override
        public @NotNull StructCodec<? extends LootPredicate> codec() {
            return CODEC;
        }
    }

    record Reference(@NotNull Key name) implements LootPredicate {
        public static final @NotNull StructCodec<Reference> CODEC = StructCodec.struct(
                "name", Codec.KEY, Reference::name,
                Reference::new
        );

        @Override
        public boolean test(@NotNull LootContext context) {
            if (true) throw new UnsupportedOperationException("TODO: Implement loot predicate registry (Key -> @Nullable LootPredicate)");
            LootPredicate predicate = null;

            return predicate != null && predicate.test(context);
        }

        @Override
        public @NotNull StructCodec<? extends LootPredicate> codec() {
            return CODEC;
        }
    }

    record SurvivesExplosion() implements LootPredicate {
        public static final @NotNull StructCodec<SurvivesExplosion> CODEC = StructCodec.struct(SurvivesExplosion::new);

        @Override
        public boolean test(@NotNull LootContext context) {
            Float radius = context.get(LootContext.EXPLOSION_RADIUS);
            return radius == null || context.require(LootContext.RANDOM).nextFloat() <= (1 / radius);
        }

        @Override
        public @NotNull StructCodec<? extends LootPredicate> codec() {
            return CODEC;
        }
    }

    record TableBonus(@NotNull RegistryKey<Enchantment> enchantment, @NotNull List<Float> chances) implements LootPredicate {
        public static final @NotNull StructCodec<TableBonus> CODEC = StructCodec.struct(
                "enchantment", RegistryKey.codec(Registries::enchantment), TableBonus::enchantment,
                "chances", Codec.FLOAT.list(), TableBonus::chances,
                TableBonus::new
        );

        @Override
        public boolean test(@NotNull LootContext context) {
            ItemStack tool = context.get(LootContext.TOOL);

            int level = EnchantmentUtils.level(tool, enchantment);

            float chance = chances.get(Math.min(this.chances.size() - 1, level));

            return context.require(LootContext.RANDOM).nextFloat() < chance;
        }

        @Override
        public @NotNull StructCodec<? extends LootPredicate> codec() {
            return CODEC;
        }
    }

    record TimeCheck(@Nullable Long period, @NotNull LootNumberRange value) implements LootPredicate {
        public static final @NotNull StructCodec<TimeCheck> CODEC = StructCodec.struct(
                "period", Codec.LONG.optional(), TimeCheck::period,
                "value", LootNumberRange.CODEC, TimeCheck::value,
                TimeCheck::new
        );

        @Override
        public boolean test(@NotNull LootContext context) {
            long time = context.require(LootContext.WORLD).getTime();

            if (period != null) {
                time %= period;
            }

            return value.check(context, time);
        }

        @Override
        public @NotNull StructCodec<? extends LootPredicate> codec() {
            return CODEC;
        }
    }

    record ValueCheck(@NotNull LootNumber value, @NotNull LootNumberRange range) implements LootPredicate {
        public static final @NotNull StructCodec<ValueCheck> CODEC = StructCodec.struct(
                "value", LootNumber.CODEC, ValueCheck::value,
                "range", LootNumberRange.CODEC, ValueCheck::range,
                ValueCheck::new
        );

        @Override
        public boolean test(@NotNull LootContext context) {
            return range.check(context, value.getInt(context));
        }

        @Override
        public @NotNull StructCodec<? extends LootPredicate> codec() {
            return CODEC;
        }
    }

    record WeatherCheck(@Nullable Boolean raining, @Nullable Boolean thundering) implements LootPredicate {
        public static final @NotNull StructCodec<WeatherCheck> CODEC = StructCodec.struct(
                "raining", Codec.BOOLEAN.optional(), WeatherCheck::raining,
                "thundering", Codec.BOOLEAN.optional(), WeatherCheck::thundering,
                WeatherCheck::new
        );

        @Override
        public boolean test(@NotNull LootContext context) {
            Weather weather = context.require(LootContext.WORLD).getWeather();

            return (raining == null || raining == weather.isRaining()) &&
                    (thundering == null || thundering == weather.thunderLevel() > 0);
        }

        @Override
        public @NotNull StructCodec<? extends LootPredicate> codec() {
            return CODEC;
        }
    }

}

