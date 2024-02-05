package net.minestom.vanilla.datapack.recipe;

import com.squareup.moshi.Json;
import com.squareup.moshi.JsonReader;
import net.minestom.server.item.Material;
import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.datapack.DatapackLoader;
import net.minestom.vanilla.datapack.json.JsonUtils;
import net.minestom.vanilla.datapack.json.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

public interface Recipe {

    @NotNull NamespaceID type();

    @Nullable String group();

    static Recipe fromJson(JsonReader reader) throws IOException {
        return JsonUtils.unionStringTypeAdapted(reader, "type", type -> switch(type) {
            case "minecraft:blasting" -> Blasting.class;
            case "minecraft:campfire_cooking" -> CampfireCooking.class;
            case "minecraft:crafting_shaped" -> Shaped.class;
            case "minecraft:crafting_shapeless" -> Shapeless.class;
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
            case "minecraft:crafting_special_shulkerboxcoloring" -> Special.ShulkerBoxColoring.class;
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
        @NotNull Material result();
        double experience();
        @Optional Integer cookingTime();
    }

    interface Ingredient {

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
                    JsonReader.Token.BEGIN_OBJECT, DatapackLoader.moshi(Single.class),
                    JsonReader.Token.NULL, json -> {
                        json.nextNull();
                        return new None();
                    }
            ));
        }

        // single means within an array, not necessarily a singular item
        interface Single extends Ingredient {
            static Single fromJson(JsonReader reader) throws IOException {
                JsonReader peek = reader.peekJson();
                peek.beginObject();
                boolean isTag = JsonUtils.hasProperty(peek, "tag");
                if (isTag) {
                    return DatapackLoader.moshi(Tag.class).apply(reader);
                }
                boolean isItem = JsonUtils.hasProperty(peek, "item");
                if (isItem) {
                    return DatapackLoader.moshi(Item.class).apply(reader);
                }
                throw new IOException("Ingredient must have either tag or item");
            }
        }

        record Item(Material item) implements Single {
        }

        record Tag(NamespaceID tag) implements Single {
        }

        record None() implements Ingredient {
        }

        record Multi(List<Single> items) implements Ingredient {
        }
    }

    record Result(Material item, @Optional Integer count) {
    }

    record Blasting(String group, JsonUtils.SingleOrList<Ingredient> ingredient, Material result,
                    double experience, @Optional @Json(name = "cookingtime") Integer cookingTime) implements CookingRecipe {
        @Override
        public @NotNull NamespaceID type() {
            return NamespaceID.from("minecraft:blasting");
        }
    }

    record CampfireCooking(String group, JsonUtils.SingleOrList<Ingredient> ingredient, Material result,
                           double experience, @Optional @Json(name = "cookingtime") Integer cookingTime) implements CookingRecipe {
        @Override
        public @NotNull NamespaceID type() {
            return NamespaceID.from("minecraft:campfire_cooking");
        }
    }

    record Shaped(String group, List<String> pattern, Map<Character, Ingredient> key, Result result) implements Recipe {
        @Override
        public @NotNull NamespaceID type() {
            return NamespaceID.from("minecraft:crafting_shaped");
        }
    }

    record Shapeless(String group, JsonUtils.SingleOrList<Ingredient> ingredients, Result result) implements Recipe {
        @Override
        public @NotNull NamespaceID type() {
            return NamespaceID.from("minecraft:crafting_shapeless");
        }
    }

    sealed interface Special extends Recipe {

        record ArmorDye(String group) implements Special {
            @Override
            public @NotNull NamespaceID type() {
                return NamespaceID.from("minecraft:crafting_special_armordye");
            }
        }

        record BannerDuplicate(String group) implements Special {
            @Override
            public @NotNull NamespaceID type() {
                return NamespaceID.from("minecraft:crafting_special_bannerduplicate");
            }
        }

        record BookCloning(String group) implements Special {
            @Override
            public @NotNull NamespaceID type() {
                return NamespaceID.from("minecraft:crafting_special_bookcloning");
            }
        }

        record FireworkRocket(String group) implements Special {
            @Override
            public @NotNull NamespaceID type() {
                return NamespaceID.from("minecraft:crafting_special_firework_rocket");
            }
        }

        record FireworkStar(String group) implements Special {
            @Override
            public @NotNull NamespaceID type() {
                return NamespaceID.from("minecraft:crafting_special_firework_star");
            }
        }

        record FireworkStarFade(String group) implements Special {
            @Override
            public @NotNull NamespaceID type() {
                return NamespaceID.from("minecraft:crafting_special_firework_star_fade");
            }
        }

        record MapCloning(String group) implements Special {
            @Override
            public @NotNull NamespaceID type() {
                return NamespaceID.from("minecraft:crafting_special_mapcloning");
            }
        }

        record MapExtending(String group) implements Special {
            @Override
            public @NotNull NamespaceID type() {
                return NamespaceID.from("minecraft:crafting_special_mapextending");
            }
        }

        record RepairItem(String group) implements Special {
            @Override
            public @NotNull NamespaceID type() {
                return NamespaceID.from("minecraft:crafting_special_repairitem");
            }
        }

        record ShieldDecoration(String group) implements Special {
            @Override
            public @NotNull NamespaceID type() {
                return NamespaceID.from("minecraft:crafting_special_shielddecoration");
            }
        }

        record ShulkerBoxColoring(String group) implements Special {
            @Override
            public @NotNull NamespaceID type() {
                return NamespaceID.from("minecraft:crafting_special_shulkerboxcoloring");
            }
        }

        record TippedArrow(String group) implements Special {
            @Override
            public @NotNull NamespaceID type() {
                return NamespaceID.from("minecraft:crafting_special_tippedarrow");
            }
        }

        record SuspiciousStew(String group) implements Special {
            @Override
            public @NotNull NamespaceID type() {
                return NamespaceID.from("minecraft:crafting_special_suspiciousstew");
            }
        }
    }


    record DecoratedPot(String group, String category) implements Recipe {
        @Override
        public @NotNull NamespaceID type() {
            return NamespaceID.from("minecraft:decorated_pot");
        }
    }

    record Smelting(String group, JsonUtils.SingleOrList<Ingredient> ingredient, Material result,
                    double experience, @Optional @Json(name = "cookingtime") Integer cookingTime) implements CookingRecipe {
        @Override
        public @NotNull NamespaceID type() {
            return NamespaceID.from("minecraft:smelting");
        }
    }

    public interface Smithing extends Recipe {

        Ingredient.Single template();
        Ingredient.Single base();
        Ingredient.Single addition();
        default @NotNull NamespaceID type() {
            return NamespaceID.from("minecraft:smithing");
        }
    }

    record Smoking(String group, JsonUtils.SingleOrList<Ingredient> ingredient, Material result,
                   double experience, @Optional @Json(name = "cookingtime") Integer cookingTime) implements CookingRecipe {
        @Override
        public @NotNull NamespaceID type() {
            return NamespaceID.from("minecraft:smoking");
        }
    }

    record Stonecutting(String group, JsonUtils.SingleOrList<Ingredient> ingredient, Material result,
                        int count) implements Recipe {
        @Override
        public @NotNull NamespaceID type() {
            return NamespaceID.from("minecraft:stonecutting");
        }
    }

    record SmithingTrim(String group, Ingredient.Single base, Ingredient.Single addition, Result result,
                        Ingredient.Single template) implements Smithing {
        @Override
        public @NotNull NamespaceID type() {
            return NamespaceID.from("minecraft:smithing_trim");
        }
    }

    record SmithingTransform(String group, Ingredient.Single base, Ingredient.Single addition, Result result,
                             Ingredient.Single template) implements Smithing {
        @Override
        public @NotNull NamespaceID type() {
            return NamespaceID.from("minecraft:smithing_transform");
        }
    }
}