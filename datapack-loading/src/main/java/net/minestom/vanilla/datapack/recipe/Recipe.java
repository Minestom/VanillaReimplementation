package net.minestom.vanilla.datapack.recipe;

import com.squareup.moshi.Json;
import com.squareup.moshi.JsonReader;
import net.kyori.adventure.key.Key;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.item.Material;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.vanilla.datapack.DatapackLoader;
import net.minestom.vanilla.datapack.json.JsonUtils;
import net.minestom.vanilla.datapack.json.Optional;
import okio.Buffer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public interface Recipe {

    @NotNull Key type();

    @Nullable String group();

    @NotNull Codec<Recipe> CODEC = makeCodec();

    private static StructCodec<Recipe> makeCodec() {
        return Codec.RegistryTaggedUnion(registries -> {
            class Holder {
                static final @NotNull DynamicRegistry<StructCodec<? extends Recipe>> CODEC = createDefaultRegistry();
            }
            return Holder.CODEC;
        }, Recipe::codec, "type");
    }

    private static DynamicRegistry<StructCodec<? extends Recipe>> createDefaultRegistry() {
        var registry = DynamicRegistry.<StructCodec<? extends Recipe>>create("recipe");
        
        registry.register(Key.key("minecraft:blasting"), Blasting.CODEC);
        registry.register(Key.key("minecraft:campfire_cooking"), CampfireCooking.CODEC);
        registry.register(Key.key("minecraft:crafting_shaped"), Shaped.CODEC);
        registry.register(Key.key("minecraft:crafting_shapeless"), Shapeless.CODEC);
        registry.register(Key.key("minecraft:crafting_transmute"), Transmute.CODEC);
        registry.register(Key.key("minecraft:crafting_special_armordye"), Special.ArmorDye.CODEC);
        registry.register(Key.key("minecraft:crafting_special_bannerduplicate"), Special.BannerDuplicate.CODEC);
        registry.register(Key.key("minecraft:crafting_special_bookcloning"), Special.BookCloning.CODEC);
        registry.register(Key.key("minecraft:crafting_special_firework_rocket"), Special.FireworkRocket.CODEC);
        registry.register(Key.key("minecraft:crafting_special_firework_star"), Special.FireworkStar.CODEC);
        registry.register(Key.key("minecraft:crafting_special_firework_star_fade"), Special.FireworkStarFade.CODEC);
        registry.register(Key.key("minecraft:crafting_special_mapcloning"), Special.MapCloning.CODEC);
        registry.register(Key.key("minecraft:crafting_special_mapextending"), Special.MapExtending.CODEC);
        registry.register(Key.key("minecraft:crafting_special_repairitem"), Special.RepairItem.CODEC);
        registry.register(Key.key("minecraft:crafting_special_shielddecoration"), Special.ShieldDecoration.CODEC);
        registry.register(Key.key("minecraft:crafting_special_tippedarrow"), Special.TippedArrow.CODEC);
        registry.register(Key.key("minecraft:crafting_special_suspiciousstew"), Special.SuspiciousStew.CODEC);
        registry.register(Key.key("minecraft:crafting_decorated_pot"), DecoratedPot.CODEC);
        registry.register(Key.key("minecraft:smelting"), Smelting.CODEC);
        registry.register(Key.key("minecraft:smithing"), Smithing.CODEC);
        registry.register(Key.key("minecraft:smoking"), Smoking.CODEC);
        registry.register(Key.key("minecraft:stonecutting"), Stonecutting.CODEC);
        registry.register(Key.key("minecraft:smithing_trim"), SmithingTrim.CODEC);
        registry.register(Key.key("minecraft:smithing_transform"), SmithingTransform.CODEC);
        
        return registry;
    }

    @NotNull StructCodec<? extends Recipe> codec();

    // New codec-based parsing method
    static Recipe fromCodec(String json) {
        // This would integrate with the Minestom codec system
        // For now, we'll implement a basic codec-to-JSON bridge
        try {
            // Parse using legacy method temporarily to validate codec structure
            Buffer buffer = new Buffer().writeUtf8(json);
            JsonReader reader = JsonReader.of(buffer);
            return fromJson(reader);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse recipe from JSON", e);
        }
    }

    // Method to use codec for parsing (future implementation)
    static Recipe fromJsonWithCodec(JsonReader reader) throws IOException {
        // This method will eventually replace fromJson, using the CODEC field
        // For now, delegate to legacy implementation
        return fromJson(reader);
    }

    // Legacy fromJson method for backward compatibility during transition
    static Recipe fromJson(JsonReader reader) throws IOException {
        return JsonUtils.unionStringTypeAdapted(reader, "type", type -> switch(type) {
            case "minecraft:blasting" -> Blasting.class;
            case "minecraft:campfire_cooking" -> CampfireCooking.class;
            case "minecraft:crafting_shaped" -> Shaped.class;
            case "minecraft:crafting_shapeless" -> Shapeless.class;
            case "minecraft:crafting_transmute" -> Transmute.class;
            case "minecraft:crafting_special_armordye" -> Special.ArmorDye.class;
            case "minecraft:crafting_special_bannerduplicate" -> Special.BannerDuplicate.class;
            case "minecraft:crafting_special_bookcloning" -> Special.BookCloning.class;
            case "minecraft:crafting_special_firework_rocket" -> Special.FireworkRocket.class;
            case "minecraft:crafting_special_firework_star" -> Special.FireworkStar.class;
            case "minecraft:crafting_special_firework_star_fade" -> Special.FireworkStarFade.class;
            case "minecraft:crafting_special_mapcloning" -> Special.MapCloning.class;
            case "minecraft:crafting_special_mapextending" -> Special.MapExtending.class;
            case "minecraft:crafting_special_repairitem" -> Special.RepairItem.class;
            case "minecraft:crafting_special_shielddecoration" -> Special.ShieldDecoration.class;
            case "minecraft:crafting_special_tippedarrow" -> Special.TippedArrow.class;
            case "minecraft:crafting_special_suspiciousstew" -> Special.SuspiciousStew.class;
            case "minecraft:crafting_decorated_pot" -> DecoratedPot.class;
            case "minecraft:smelting" -> Smelting.class;
            case "minecraft:smithing" -> Smithing.class;
            case "minecraft:smoking" -> Smoking.class;
            case "minecraft:stonecutting" -> Stonecutting.class;
            case "minecraft:smithing_trim" -> SmithingTrim.class;
            case "minecraft:smithing_transform" -> SmithingTransform.class;
            default -> null;
        });
    }

    interface CookingRecipe extends Recipe {
        @NotNull List<Ingredient> ingredient();
        @NotNull SingleResult result();
        double experience();
        @Optional Integer cookingTime();
    }

    interface Ingredient {

        @NotNull Codec<Ingredient> CODEC = Codec.either(
                Single.CODEC.cast(),
                Single.CODEC.list().transform(Multi::new, Multi::items)
        ).cast();

        // Legacy fromJson method for backward compatibility during transition
        static Ingredient fromJson(JsonReader reader) throws IOException {
            return JsonUtils.<Ingredient>typeMapMapped(reader, Map.of(
                    JsonReader.Token.BEGIN_ARRAY, json -> {
                        Stream.Builder<Single> items = Stream.builder();
                        json.beginArray();
                        while (json.peek() != JsonReader.Token.END_ARRAY) {
                            items.add(DatapackLoader.moshi(Single.class).apply(json));
                        }
                        json.endArray();
                        return new Multi(items.build().toList());
                    },
                    JsonReader.Token.STRING, DatapackLoader.moshi(Single.class),
                    JsonReader.Token.NULL, json -> {
                        json.nextNull();
                        return new None();
                    }
            ));
        }

        // single means within an array, not necessarily a singular item
        interface Single extends Ingredient {
            @NotNull Codec<Single> CODEC = Codec.STRING.transform(Single::fromString, Single::toString);

            static Single fromString(String content) {
                boolean isTag = content.startsWith("#");
                if (isTag) {
                    return new Tag(Key.key(content.substring(1)));
                }
                return new Item(Material.fromKey(content));
            }

            String toString();

            // Legacy fromJson method for backward compatibility during transition
            static Single fromJson(JsonReader reader) throws IOException {
                String content = reader.nextString();
                boolean isTag = content.startsWith("#");
                if (isTag) {
                    return new Tag(Key.key(content.substring(1)));
                }
                return new Item(Material.fromKey(content));
            }
        }

        record Item(Material item) implements Single {
            @Override
            public String toString() {
                return item.key().toString();
            }
        }

        record Tag(Key tag) implements Single {
            @Override
            public String toString() {
                return "#" + tag.toString();
            }
        }

        record None() implements Ingredient {
        }

        record Multi(List<Single> items) implements Ingredient {
        }
    }

    record Result(Material id, @Optional Integer count) {
        public static final @NotNull StructCodec<Result> CODEC = StructCodec.struct(
                "item", Codec.KEY.transform(Material::fromKey, Material::key), Result::id,
                "count", Codec.INT.optional(), Result::count,
                Result::new
        );
    }

    record SingleResult(Material id) {
        public static final @NotNull Codec<SingleResult> CODEC = Codec.KEY.transform(
                material -> new SingleResult(Material.fromKey(material)),
                result -> result.id.key()
        );
    }

    record Blasting(String group, @Optional String category, JsonUtils.SingleOrList<Ingredient> ingredient, SingleResult result,
                    double experience, @Optional @Json(name = "cookingtime") Integer cookingTime) implements CookingRecipe {
        public static final @NotNull StructCodec<Blasting> CODEC = StructCodec.struct(
                "group", Codec.STRING.optional(""), Blasting::group,
                "category", Codec.STRING.optional(), Blasting::category,
                "ingredient", JsonUtils.SingleOrList.codec(Ingredient.CODEC), Blasting::ingredient,
                "result", SingleResult.CODEC, Blasting::result,
                "experience", Codec.DOUBLE, Blasting::experience,
                "cookingtime", Codec.INT.optional(), Blasting::cookingTime,
                Blasting::new
        );

        @Override
        public @NotNull Key type() {
            return Key.key("minecraft:blasting");
        }

        @Override
        public @NotNull StructCodec<? extends Recipe> codec() {
            return CODEC;
        }
    }

    record CampfireCooking(String group, JsonUtils.SingleOrList<Ingredient> ingredient, SingleResult result,
                           double experience, @Optional @Json(name = "cookingtime") Integer cookingTime) implements CookingRecipe {
        public static final @NotNull StructCodec<CampfireCooking> CODEC = StructCodec.struct(
                "group", Codec.STRING.optional(""), CampfireCooking::group,
                "ingredient", JsonUtils.SingleOrList.codec(Ingredient.CODEC), CampfireCooking::ingredient,
                "result", SingleResult.CODEC, CampfireCooking::result,
                "experience", Codec.DOUBLE, CampfireCooking::experience,
                "cookingtime", Codec.INT.optional(), CampfireCooking::cookingTime,
                CampfireCooking::new
        );

        @Override
        public @NotNull Key type() {
            return Key.key("minecraft:campfire_cooking");
        }

        @Override
        public @NotNull StructCodec<? extends Recipe> codec() {
            return CODEC;
        }
    }

    record Shaped(String group, @Optional String category, List<String> pattern, Map<Character, Ingredient> key, Result result) implements Recipe {
        public static final @NotNull StructCodec<Shaped> CODEC = StructCodec.struct(
                "group", Codec.STRING.optional(""), Shaped::group,
                "category", Codec.STRING.optional(), Shaped::category,
                "pattern", Codec.STRING.list(), Shaped::pattern,
                "key", Codec.map(Codec.STRING.transform(s -> s.charAt(0), c -> String.valueOf(c)), Ingredient.CODEC), Shaped::key,
                "result", Result.CODEC, Shaped::result,
                Shaped::new
        );

        @Override
        public @NotNull Key type() {
            return Key.key("minecraft:crafting_shaped");
        }

        @Override
        public @NotNull StructCodec<? extends Recipe> codec() {
            return CODEC;
        }
    }

    record Shapeless(String group, @Optional String category, JsonUtils.SingleOrList<Ingredient> ingredients, Result result) implements Recipe {
        public static final @NotNull StructCodec<Shapeless> CODEC = StructCodec.struct(
                "group", Codec.STRING.optional(""), Shapeless::group,
                "category", Codec.STRING.optional(), Shapeless::category,
                "ingredients", JsonUtils.SingleOrList.codec(Ingredient.CODEC), Shapeless::ingredients,
                "result", Result.CODEC, Shapeless::result,
                Shapeless::new
        );

        @Override
        public @NotNull Key type() {
            return Key.key("minecraft:crafting_shapeless");
        }

        @Override
        public @NotNull StructCodec<? extends Recipe> codec() {
            return CODEC;
        }
    }

    record Transmute(String group, @Optional String category, JsonUtils.SingleOrList<Ingredient> input,
                     JsonUtils.SingleOrList<Ingredient> material, Result result) implements Recipe {
        public static final @NotNull StructCodec<Transmute> CODEC = StructCodec.struct(
                "group", Codec.STRING.optional(""), Transmute::group,
                "category", Codec.STRING.optional(), Transmute::category,
                "input", JsonUtils.SingleOrList.codec(Ingredient.CODEC), Transmute::input,
                "material", JsonUtils.SingleOrList.codec(Ingredient.CODEC), Transmute::material,
                "result", Result.CODEC, Transmute::result,
                Transmute::new
        );

        @Override
        public @NotNull Key type() {
            return Key.key("minecraft:crafting_transmute");
        }

        @Override
        public @NotNull StructCodec<? extends Recipe> codec() {
            return CODEC;
        }
    }

    sealed interface Special extends Recipe {

        record ArmorDye(String group) implements Special {
            public static final @NotNull StructCodec<ArmorDye> CODEC = StructCodec.struct(
                    "group", Codec.STRING.optional(""), ArmorDye::group,
                    ArmorDye::new
            );

            @Override
            public @NotNull Key type() {
                return Key.key("minecraft:crafting_special_armordye");
            }

            @Override
            public @NotNull StructCodec<? extends Recipe> codec() {
                return CODEC;
            }
        }

        record BannerDuplicate(String group) implements Special {
            public static final @NotNull StructCodec<BannerDuplicate> CODEC = StructCodec.struct(
                    "group", Codec.STRING.optional(""), BannerDuplicate::group,
                    BannerDuplicate::new
            );

            @Override
            public @NotNull Key type() {
                return Key.key("minecraft:crafting_special_bannerduplicate");
            }

            @Override
            public @NotNull StructCodec<? extends Recipe> codec() {
                return CODEC;
            }
        }

        record BookCloning(String group) implements Special {
            public static final @NotNull StructCodec<BookCloning> CODEC = StructCodec.struct(
                    "group", Codec.STRING.optional(""), BookCloning::group,
                    BookCloning::new
            );

            @Override
            public @NotNull Key type() {
                return Key.key("minecraft:crafting_special_bookcloning");
            }

            @Override
            public @NotNull StructCodec<? extends Recipe> codec() {
                return CODEC;
            }
        }

        record FireworkRocket(String group) implements Special {
            public static final @NotNull StructCodec<FireworkRocket> CODEC = StructCodec.struct(
                    "group", Codec.STRING.optional(""), FireworkRocket::group,
                    FireworkRocket::new
            );

            @Override
            public @NotNull Key type() {
                return Key.key("minecraft:crafting_special_firework_rocket");
            }

            @Override
            public @NotNull StructCodec<? extends Recipe> codec() {
                return CODEC;
            }
        }

        record FireworkStar(String group) implements Special {
            public static final @NotNull StructCodec<FireworkStar> CODEC = StructCodec.struct(
                    "group", Codec.STRING.optional(""), FireworkStar::group,
                    FireworkStar::new
            );

            @Override
            public @NotNull Key type() {
                return Key.key("minecraft:crafting_special_firework_star");
            }

            @Override
            public @NotNull StructCodec<? extends Recipe> codec() {
                return CODEC;
            }
        }

        record FireworkStarFade(String group) implements Special {
            public static final @NotNull StructCodec<FireworkStarFade> CODEC = StructCodec.struct(
                    "group", Codec.STRING.optional(""), FireworkStarFade::group,
                    FireworkStarFade::new
            );

            @Override
            public @NotNull Key type() {
                return Key.key("minecraft:crafting_special_firework_star_fade");
            }

            @Override
            public @NotNull StructCodec<? extends Recipe> codec() {
                return CODEC;
            }
        }

        record MapCloning(String group) implements Special {
            public static final @NotNull StructCodec<MapCloning> CODEC = StructCodec.struct(
                    "group", Codec.STRING.optional(""), MapCloning::group,
                    MapCloning::new
            );

            @Override
            public @NotNull Key type() {
                return Key.key("minecraft:crafting_special_mapcloning");
            }

            @Override
            public @NotNull StructCodec<? extends Recipe> codec() {
                return CODEC;
            }
        }

        record MapExtending(String group) implements Special {
            public static final @NotNull StructCodec<MapExtending> CODEC = StructCodec.struct(
                    "group", Codec.STRING.optional(""), MapExtending::group,
                    MapExtending::new
            );

            @Override
            public @NotNull Key type() {
                return Key.key("minecraft:crafting_special_mapextending");
            }

            @Override
            public @NotNull StructCodec<? extends Recipe> codec() {
                return CODEC;
            }
        }

        record RepairItem(String group) implements Special {
            public static final @NotNull StructCodec<RepairItem> CODEC = StructCodec.struct(
                    "group", Codec.STRING.optional(""), RepairItem::group,
                    RepairItem::new
            );

            @Override
            public @NotNull Key type() {
                return Key.key("minecraft:crafting_special_repairitem");
            }

            @Override
            public @NotNull StructCodec<? extends Recipe> codec() {
                return CODEC;
            }
        }

        record ShieldDecoration(String group) implements Special {
            public static final @NotNull StructCodec<ShieldDecoration> CODEC = StructCodec.struct(
                    "group", Codec.STRING.optional(""), ShieldDecoration::group,
                    ShieldDecoration::new
            );

            @Override
            public @NotNull Key type() {
                return Key.key("minecraft:crafting_special_shielddecoration");
            }

            @Override
            public @NotNull StructCodec<? extends Recipe> codec() {
                return CODEC;
            }
        }

        record TippedArrow(String group) implements Special {
            public static final @NotNull StructCodec<TippedArrow> CODEC = StructCodec.struct(
                    "group", Codec.STRING.optional(""), TippedArrow::group,
                    TippedArrow::new
            );

            @Override
            public @NotNull Key type() {
                return Key.key("minecraft:crafting_special_tippedarrow");
            }

            @Override
            public @NotNull StructCodec<? extends Recipe> codec() {
                return CODEC;
            }
        }

        record SuspiciousStew(String group) implements Special {
            public static final @NotNull StructCodec<SuspiciousStew> CODEC = StructCodec.struct(
                    "group", Codec.STRING.optional(""), SuspiciousStew::group,
                    SuspiciousStew::new
            );

            @Override
            public @NotNull Key type() {
                return Key.key("minecraft:crafting_special_suspiciousstew");
            }

            @Override
            public @NotNull StructCodec<? extends Recipe> codec() {
                return CODEC;
            }
        }
    }


    record DecoratedPot(String group, String category) implements Recipe {
        public static final @NotNull StructCodec<DecoratedPot> CODEC = StructCodec.struct(
                "group", Codec.STRING.optional(""), DecoratedPot::group,
                "category", Codec.STRING.optional(""), DecoratedPot::category,
                DecoratedPot::new
        );

        @Override
        public @NotNull Key type() {
            return Key.key("minecraft:decorated_pot");
        }

        @Override
        public @NotNull StructCodec<? extends Recipe> codec() {
            return CODEC;
        }
    }

    record Smelting(String group, @Optional String category, JsonUtils.SingleOrList<Ingredient> ingredient, SingleResult result,
                    double experience, @Optional @Json(name = "cookingtime") Integer cookingTime) implements CookingRecipe {
        public static final @NotNull StructCodec<Smelting> CODEC = StructCodec.struct(
                "group", Codec.STRING.optional(""), Smelting::group,
                "category", Codec.STRING.optional(), Smelting::category,
                "ingredient", JsonUtils.SingleOrList.codec(Ingredient.CODEC), Smelting::ingredient,
                "result", SingleResult.CODEC, Smelting::result,
                "experience", Codec.DOUBLE, Smelting::experience,
                "cookingtime", Codec.INT.optional(), Smelting::cookingTime,
                Smelting::new
        );

        @Override
        public @NotNull Key type() {
            return Key.key("minecraft:smelting");
        }

        @Override
        public @NotNull StructCodec<? extends Recipe> codec() {
            return CODEC;
        }
    }

    record Smoking(String group, JsonUtils.SingleOrList<Ingredient> ingredient, SingleResult result,
                   double experience, @Optional @Json(name = "cookingtime") Integer cookingTime) implements CookingRecipe {
        public static final @NotNull StructCodec<Smoking> CODEC = StructCodec.struct(
                "group", Codec.STRING.optional(""), Smoking::group,
                "ingredient", JsonUtils.SingleOrList.codec(Ingredient.CODEC), Smoking::ingredient,
                "result", SingleResult.CODEC, Smoking::result,
                "experience", Codec.DOUBLE, Smoking::experience,
                "cookingtime", Codec.INT.optional(), Smoking::cookingTime,
                Smoking::new
        );

        @Override
        public @NotNull Key type() {
            return Key.key("minecraft:smoking");
        }

        @Override
        public @NotNull StructCodec<? extends Recipe> codec() {
            return CODEC;
        }
    }

    record Stonecutting(@Nullable String group, JsonUtils.SingleOrList<Ingredient> ingredient, Result result) implements Recipe {
        public static final @NotNull StructCodec<Stonecutting> CODEC = StructCodec.struct(
                "group", Codec.STRING.optional(), Stonecutting::group,
                "ingredient", JsonUtils.SingleOrList.codec(Ingredient.CODEC), Stonecutting::ingredient,
                "result", Result.CODEC, Stonecutting::result,
                Stonecutting::new
        );

        @Override
        public @NotNull Key type() {
            return Key.key("minecraft:stonecutting");
        }

        @Override
        public @NotNull StructCodec<? extends Recipe> codec() {
            return CODEC;
        }
    }

    interface Smithing extends Recipe {

        Ingredient.Single template();
        Ingredient.Single base();
        Ingredient.Single addition();
        default @NotNull Key type() {
            return Key.key("minecraft:smithing");
        }

        // Generic smithing codec placeholder 
        StructCodec<Smithing> CODEC = StructCodec.struct(
                "template", Ingredient.Single.CODEC, smithing -> smithing.template(),
                "base", Ingredient.Single.CODEC, smithing -> smithing.base(),
                "addition", Ingredient.Single.CODEC, smithing -> smithing.addition(),
                (template, base, addition) -> new SmithingTrim("", base, addition, "", template) // Default implementation
        );

        @Override
        default @NotNull StructCodec<? extends Recipe> codec() {
            return CODEC;
        }
    }

    record SmithingTrim(String group, Ingredient.Single base, Ingredient.Single addition, String pattern,
                        Ingredient.Single template) implements Smithing {
        public static final @NotNull StructCodec<SmithingTrim> CODEC = StructCodec.struct(
                "group", Codec.STRING.optional(""), SmithingTrim::group,
                "base", Ingredient.Single.CODEC, SmithingTrim::base,
                "addition", Ingredient.Single.CODEC, SmithingTrim::addition,
                "pattern", Codec.STRING.optional(""), SmithingTrim::pattern,
                "template", Ingredient.Single.CODEC, SmithingTrim::template,
                SmithingTrim::new
        );

        @Override
        public @NotNull Key type() {
            return Key.key("minecraft:smithing_trim");
        }

        @Override
        public @NotNull StructCodec<? extends Recipe> codec() {
            return CODEC;
        }
    }

    record SmithingTransform(String group, Ingredient.Single base, Ingredient.Single addition, Result result,
                             Ingredient.Single template) implements Smithing {
        public static final @NotNull StructCodec<SmithingTransform> CODEC = StructCodec.struct(
                "group", Codec.STRING.optional(""), SmithingTransform::group,
                "base", Ingredient.Single.CODEC, SmithingTransform::base,
                "addition", Ingredient.Single.CODEC, SmithingTransform::addition,
                "result", Result.CODEC, SmithingTransform::result,
                "template", Ingredient.Single.CODEC, SmithingTransform::template,
                SmithingTransform::new
        );

        @Override
        public @NotNull Key type() {
            return Key.key("minecraft:smithing_transform");
        }

        @Override
        public @NotNull StructCodec<? extends Recipe> codec() {
            return CODEC;
        }
    }
}
