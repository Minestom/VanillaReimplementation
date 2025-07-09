package net.minestom.vanilla.crafting;

import net.kyori.adventure.key.Key;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.recipe.RecipeBookCategory;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.RegistryTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * All recipe types that exist.
 */
@SuppressWarnings("UnstableApiUsage")
public sealed interface Recipe extends net.minestom.server.recipe.Recipe {

    @NotNull StructCodec<Recipe> CODEC = Codec.RegistryTaggedUnion(registries -> {
        class Holder {
            static final @NotNull DynamicRegistry<StructCodec<? extends Recipe>> CODEC = createDefaultRegistry();
        }
        return Holder.CODEC;
    }, Recipe::codec, "type");

    static @NotNull DynamicRegistry<StructCodec<? extends Recipe>> createDefaultRegistry() {
        final DynamicRegistry<StructCodec<? extends Recipe>> registry = DynamicRegistry.create(Key.key("recipes"));

        // Crafting table recipes
        registry.register("crafting_shaped", Crafting.Shaped.CODEC);
        registry.register("crafting_shapeless", Crafting.Shapeless.CODEC);
        registry.register("crafting_transmute", Crafting.Transmute.CODEC);

        // Smelting
        registry.register("smelting", Cooking.Smelting.CODEC);
        registry.register("smoking", Cooking.Smoking.CODEC);
        registry.register("blasting", Cooking.Blasting.CODEC);
        registry.register("campfire_cooking", Cooking.Campfire.CODEC);

        // Smithing & stonecutting
        registry.register("smithing_transform", Smithing.Transform.CODEC);
        registry.register("smithing_trim", Smithing.Trim.CODEC);
        registry.register("stonecutting", Stonecutting.CODEC);

        // Special recipes
        registry.register("crafting_decorated_pot", DecoratedPot.CODEC);
        registry.register("crafting_special_armordye", SpecialArmorDye.CODEC);
        registry.register("crafting_special_bannerduplicate", SpecialBannerDuplicate.CODEC);
        registry.register("crafting_special_bookcloning", SpecialBookCloning.CODEC);
        registry.register("crafting_special_firework_rocket", SpecialFireworkRocket.CODEC);
        registry.register("crafting_special_firework_star", SpecialFireworkStar.CODEC);
        registry.register("crafting_special_firework_star_fade", SpecialFireworkStarFade.CODEC);
        registry.register("crafting_special_mapcloning", SpecialMapCloning.CODEC);
        registry.register("crafting_special_mapextending", SpecialMapExtending.CODEC);
        registry.register("crafting_special_repairitem", SpecialRepairItem.CODEC);
        registry.register("crafting_special_shielddecoration", SpecialShieldDecoration.CODEC);
        registry.register("crafting_special_tippedarrow", SpecialTippedArrow.CODEC);
        return registry;
    }

    /**
     * A crafting recipe - either shaped, shapeless, transmute, or the decorated pot recipe.
     */
    sealed interface Crafting extends Recipe {

        /**
         * @return the recipe book category of this recipe
         */
        @NotNull Category category();

        /**
         * @return the item that this recipe produces
         */
        @NotNull ItemStack result();

        @Override
        default @Nullable RecipeBookCategory recipeBookCategory() {
            return category().category;
        }

        enum Category {
            EQUIPMENT(RecipeBookCategory.CRAFTING_EQUIPMENT),
            BUILDING(RecipeBookCategory.CRAFTING_BUILDING_BLOCKS),
            MISC(RecipeBookCategory.CRAFTING_MISC),
            REDSTONE(RecipeBookCategory.CRAFTING_REDSTONE);

            private final RecipeBookCategory category;
            Category(RecipeBookCategory category) {
                this.category = category;
            }

            public static final @NotNull Codec<Category> CODEC = Codec.Enum(Category.class);
        }

        record Shaped(@Nullable String recipeBookGroup, @NotNull Category category, @NotNull ItemStack result, boolean showNotification, @NotNull List<String> pattern, @NotNull Map<String, RegistryTag<Material>> key) implements Crafting {
            public static final @NotNull StructCodec<Shaped> CODEC = StructCodec.struct(
                    "group", Codec.STRING.optional(), Shaped::recipeBookGroup,
                    "category", Category.CODEC.optional(Category.MISC), Shaped::category,
                    "result", ItemStack.CODEC, Shaped::result,
                    "show_notification", Codec.BOOLEAN.optional(true), Shaped::showNotification,
                    "pattern", Codec.STRING.list(), Shaped::pattern,
                    "key", Codec.STRING.mapValue(RegistryTag.codec(Registries::material)), Shaped::key,
                    Shaped::new
            );

            @Override
            public @NotNull StructCodec<? extends Recipe> codec() {
                return CODEC;
            }
        }

        record Shapeless(@Nullable String recipeBookGroup, @NotNull Category category, @NotNull ItemStack result, @NotNull List<RegistryTag<Material>> ingredients) implements Crafting {
            public static final @NotNull StructCodec<Shapeless> CODEC = StructCodec.struct(
                    "group", Codec.STRING.optional(), Shapeless::recipeBookGroup,
                    "category", Category.CODEC.optional(Category.MISC), Shapeless::category,
                    "result", ItemStack.CODEC, Shapeless::result,
                    "ingredients", RegistryTag.codec(Registries::material).list(), Shapeless::ingredients,
                    Shapeless::new
            );

            @Override
            public @NotNull StructCodec<? extends Recipe> codec() {
                return CODEC;
            }
        }

        record Transmute(@Nullable String recipeBookGroup, @NotNull Category category, @NotNull ItemStack result, @NotNull RegistryTag<Material> input, @NotNull RegistryTag<Material> material) implements Crafting {
            public static final @NotNull StructCodec<Transmute> CODEC = StructCodec.struct(
                    "group", Codec.STRING.optional(), Transmute::recipeBookGroup,
                    "category", Category.CODEC.optional(Category.MISC), Transmute::category,
                    "result", ItemStack.CODEC, Transmute::result,
                    "input", RegistryTag.codec(Registries::material), Transmute::input,
                    "material", RegistryTag.codec(Registries::material), Transmute::material,
                    Transmute::new
            );

            @Override
            public @NotNull StructCodec<? extends Recipe> codec() {
                return CODEC;
            }
        }

    }

    record Cooking(@Nullable String recipeBookGroup, @Nullable Category category, @NotNull RegistryTag<Material> ingredient, @NotNull ItemStack result, int cookingTime, double experience) {
        public enum Category {
            FOOD, BLOCKS, MISC;

            public static final @NotNull Codec<Category> CODEC = Codec.Enum(Category.class);
        }

        public static @NotNull Codec<Cooking> codec(@Nullable Category defaultCategory) {
            return StructCodec.struct(
                    "group", Codec.STRING.optional(), Cooking::recipeBookGroup,
                    "category", defaultCategory == null ? Category.CODEC.optional() : Category.CODEC.optional(defaultCategory), Cooking::category,
                    "ingredient", RegistryTag.codec(Registries::material), Cooking::ingredient,
                    "result", ItemStack.CODEC, Cooking::result,
                    "cookingtime", Codec.INT.optional(100), Cooking::cookingTime,
                    "experience", Codec.DOUBLE.optional(0D), Cooking::experience,
                    Cooking::new
            );
        }

        public record Smelting(@NotNull Cooking cooking) implements Recipe {
            public static final @NotNull StructCodec<Smelting> CODEC = StructCodec.struct(
                    StructCodec.INLINE, Cooking.codec(Category.MISC), Smelting::cooking,
                    Smelting::new
            );

            @Override
            public @NotNull StructCodec<? extends Recipe> codec() {
                return CODEC;
            }
        }

        public record Smoking(@NotNull Cooking cooking) implements Recipe {
            public static final @NotNull StructCodec<Smoking> CODEC = StructCodec.struct(
                    StructCodec.INLINE, Cooking.codec(Category.FOOD), Smoking::cooking,
                    Smoking::new
            );

            @Override
            public @NotNull StructCodec<? extends Recipe> codec() {
                return CODEC;
            }
        }

        public record Blasting(@NotNull Cooking cooking) implements Recipe {
            public static final @NotNull StructCodec<Blasting> CODEC = StructCodec.struct(
                    StructCodec.INLINE, Cooking.codec(Category.MISC), Blasting::cooking,
                    Blasting::new
            );

            @Override
            public @NotNull StructCodec<? extends Recipe> codec() {
                return CODEC;
            }
        }

        public record Campfire(@NotNull Cooking cooking) implements Recipe {
            public static final @NotNull StructCodec<Campfire> CODEC = StructCodec.struct(
                    StructCodec.INLINE, Cooking.codec(null), Campfire::cooking,
                    Campfire::new
            );

            @Override
            public @NotNull StructCodec<? extends Recipe> codec() {
                return CODEC;
            }
        }
    }

    record Smithing(@NotNull RegistryTag<Material> template, @NotNull RegistryTag<Material> base, @NotNull RegistryTag<Material> addition) {

        public static final @NotNull Codec<Smithing> CODEC = StructCodec.struct(
                "template", RegistryTag.codec(Registries::material), Smithing::template,
                "base", RegistryTag.codec(Registries::material), Smithing::base,
                "addition", RegistryTag.codec(Registries::material), Smithing::addition,
                Smithing::new
        );

        public record Transform(@NotNull Smithing smithing, @NotNull ItemStack result) implements Recipe {
            public static final @NotNull StructCodec<Transform> CODEC = StructCodec.struct(
                    StructCodec.INLINE, Smithing.CODEC, Transform::smithing,
                    "result", ItemStack.CODEC, Transform::result,
                    Transform::new
            );

            @Override
            public @NotNull StructCodec<? extends Recipe> codec() {
                return CODEC;
            }
        }

        public record Trim(@NotNull Smithing smithing) implements Recipe {
            public static final @NotNull StructCodec<Trim> CODEC = StructCodec.struct(
                    StructCodec.INLINE, Smithing.CODEC, Trim::smithing,
                    Trim::new
            );

            @Override
            public @NotNull StructCodec<? extends Recipe> codec() {
                return CODEC;
            }
        }

    }

    record Stonecutting(@NotNull RegistryTag<Material> ingredient, @NotNull ItemStack result) implements Recipe {
        public static final @NotNull StructCodec<Stonecutting> CODEC = StructCodec.struct(
                "ingredient", RegistryTag.codec(Registries::material), Stonecutting::ingredient,
                "result", ItemStack.CODEC, Stonecutting::result,
                Stonecutting::new
        );

        @Override
        public @NotNull StructCodec<? extends Recipe> codec() {
            return CODEC;
        }
    }

    record DecoratedPot(@NotNull Recipe.Crafting.Category category) implements Recipe {
        public static final @NotNull StructCodec<DecoratedPot> CODEC = StructCodec.struct(
                "category", Crafting.Category.CODEC.optional(Crafting.Category.MISC), DecoratedPot::category,
                DecoratedPot::new
        );

        @Override
        public @NotNull StructCodec<? extends Recipe> codec() {
            return CODEC;
        }
    }

    record SpecialArmorDye() implements Recipe {
        public static final @NotNull StructCodec<SpecialArmorDye> CODEC = StructCodec.struct(SpecialArmorDye::new);

        @Override
        public @NotNull StructCodec<? extends Recipe> codec() {
            return CODEC;
        }
    }

    record SpecialBannerDuplicate() implements Recipe {
        public static final @NotNull StructCodec<SpecialBannerDuplicate> CODEC = StructCodec.struct(SpecialBannerDuplicate::new);

        @Override
        public @NotNull StructCodec<? extends Recipe> codec() {
            return CODEC;
        }
    }

    record SpecialBookCloning() implements Recipe {
        public static final @NotNull StructCodec<SpecialBookCloning> CODEC = StructCodec.struct(SpecialBookCloning::new);

        @Override
        public @NotNull StructCodec<? extends Recipe> codec() {
            return CODEC;
        }
    }

    record SpecialFireworkRocket() implements Recipe {
        public static final @NotNull StructCodec<SpecialFireworkRocket> CODEC = StructCodec.struct(SpecialFireworkRocket::new);

        @Override
        public @NotNull StructCodec<? extends Recipe> codec() {
            return CODEC;
        }
    }

    record SpecialFireworkStar() implements Recipe {
        public static final @NotNull StructCodec<SpecialFireworkStar> CODEC = StructCodec.struct(SpecialFireworkStar::new);

        @Override
        public @NotNull StructCodec<? extends Recipe> codec() {
            return CODEC;
        }
    }

    record SpecialFireworkStarFade() implements Recipe {
        public static final @NotNull StructCodec<SpecialFireworkStarFade> CODEC = StructCodec.struct(SpecialFireworkStarFade::new);

        @Override
        public @NotNull StructCodec<? extends Recipe> codec() {
            return CODEC;
        }
    }

    record SpecialMapCloning() implements Recipe {
        public static final @NotNull StructCodec<SpecialMapCloning> CODEC = StructCodec.struct(SpecialMapCloning::new);

        @Override
        public @NotNull StructCodec<? extends Recipe> codec() {
            return CODEC;
        }
    }

    record SpecialMapExtending() implements Recipe {
        public static final @NotNull StructCodec<SpecialMapExtending> CODEC = StructCodec.struct(SpecialMapExtending::new);

        @Override
        public @NotNull StructCodec<? extends Recipe> codec() {
            return CODEC;
        }
    }

    record SpecialRepairItem() implements Recipe {
        public static final @NotNull StructCodec<SpecialRepairItem> CODEC = StructCodec.struct(SpecialRepairItem::new);

        @Override
        public @NotNull StructCodec<? extends Recipe> codec() {
            return CODEC;
        }
    }

    record SpecialShieldDecoration() implements Recipe {
        public static final @NotNull StructCodec<SpecialShieldDecoration> CODEC = StructCodec.struct(SpecialShieldDecoration::new);

        @Override
        public @NotNull StructCodec<? extends Recipe> codec() {
            return CODEC;
        }
    }

    record SpecialTippedArrow() implements Recipe {
        public static final @NotNull StructCodec<SpecialTippedArrow> CODEC = StructCodec.struct(SpecialTippedArrow::new);

        @Override
        public @NotNull StructCodec<? extends Recipe> codec() {
            return CODEC;
        }
    }

    /**
     * @return the codec that can encode this recipe
     */
    @NotNull StructCodec<? extends Recipe> codec();

}
