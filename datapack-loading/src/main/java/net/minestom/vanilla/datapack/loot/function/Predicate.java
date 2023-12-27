package net.minestom.vanilla.datapack.loot.function;

import com.squareup.moshi.JsonReader;
import net.minestom.vanilla.datapack.json.JsonUtils;
import net.minestom.vanilla.datapack.loot.context.LootContext;

import java.io.IOException;
import java.util.Map;

public interface Predicate extends InBuiltPredicates {

    String condition();

    boolean test(LootContext context);

    static Predicate fromJson(JsonReader reader) throws IOException {
        return JsonUtils.unionStringTypeAdapted(reader, "condition", condition -> switch (condition) {
            case "minecraft:alternative" -> Alternative.class;
            case "minecraft:block_state_property" -> BlockStateProperty.class;
            case "minecraft:damage_source_properties" -> DamageSourceProperties.class;
            case "minecraft:entity_properties" -> EntityProperties.class;
            case "minecraft:entity_scores" -> EntityScores.class;
            case "minecraft:inverted" -> Inverted.class;
            case "minecraft:killed_by_player" -> KilledByPlayer.class;
            case "minecraft:location_check" -> LocationCheck.class;
            case "minecraft:match_tool" -> MatchTool.class;
            case "minecraft:random_chance" -> RandomChance.class;
            case "minecraft:random_chance_with_looting" -> RandomChanceWithLooting.class;
            case "minecraft:reference" -> Reference.class;
            case "minecraft:survives_explosion" -> SurvivesExplosion.class;
            case "minecraft:table_bonus" -> TableBonus.class;
            case "minecraft:time_check" -> TimeCheck.class;
            case "minecraft:value_check" -> ValueCheck.class;
            case "minecraft:weather_check" -> WeatherCheck.class;
            case "minecraft:any_of" -> AnyOf.class;
            case "minecraft:all_of" -> AllOf.class;
            default -> null;
        });
    }
}
