package net.minestom.vanilla.datapack.loot.function;

import com.squareup.moshi.JsonReader;
import net.minestom.vanilla.datapack.DatapackLoader;
import net.minestom.vanilla.datapack.json.JsonUtils;
import net.minestom.vanilla.datapack.loot.context.LootContext;

import java.io.IOException;
import java.util.Map;

public interface Predicate extends InBuiltPredicates {

    String condition();

    boolean test(LootContext context);

    static Predicate fromJson(JsonReader reader) throws IOException {
        return JsonUtils.unionNamespaceStringTypeAdapted(reader, "condition", Map.ofEntries(
                Map.entry("minecraft:alternative", Alternative.class),
                Map.entry("minecraft:block_state_property", BlockStateProperty.class),
                Map.entry("minecraft:damage_source_properties", DamageSourceProperties.class),
                Map.entry("minecraft:entity_properties", EntityProperties.class),
                Map.entry("minecraft:entity_scores", EntityScores.class),
                Map.entry("minecraft:inverted", Inverted.class),
                Map.entry("minecraft:killed_by_player", KilledByPlayer.class),
                Map.entry("minecraft:location_check", LocationCheck.class),
                Map.entry("minecraft:match_tool", MatchTool.class),
                Map.entry("minecraft:random_chance", RandomChance.class),
                Map.entry("minecraft:random_chance_with_looting", RandomChanceWithLooting.class),
                Map.entry("minecraft:reference", Reference.class),
                Map.entry("minecraft:survives_explosion", SurvivesExplosion.class),
                Map.entry("minecraft:table_bonus", TableBonus.class),
                Map.entry("minecraft:time_check", TimeCheck.class),
                Map.entry("minecraft:value_check", ValueCheck.class),
                Map.entry("minecraft:weather_check", WeatherCheck.class)
        ));
    }
}
