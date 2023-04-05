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
        String source = reader.peekJson().nextSource().readUtf8();
        return JsonUtils.unionNamespaceStringType(reader, "condition", Map.ofEntries(
                Map.entry("minecraft:alternative", DatapackLoader.moshi(Alternative.class)),
                Map.entry("minecraft:block_state_property", DatapackLoader.moshi(BlockStateProperty.class)),
                Map.entry("minecraft:damage_source_properties", DatapackLoader.moshi(DamageSourceProperties.class)),
                Map.entry("minecraft:entity_properties", DatapackLoader.moshi(EntityProperties.class)),
                Map.entry("minecraft:entity_scores", DatapackLoader.moshi(EntityScores.class)),
                Map.entry("minecraft:inverted", DatapackLoader.moshi(Inverted.class)),
                Map.entry("minecraft:killed_by_player", DatapackLoader.moshi(KilledByPlayer.class)),
                Map.entry("minecraft:location_check", DatapackLoader.moshi(LocationCheck.class)),
                Map.entry("minecraft:match_tool", DatapackLoader.moshi(MatchTool.class)),
                Map.entry("minecraft:random_chance", DatapackLoader.moshi(RandomChance.class)),
                Map.entry("minecraft:random_chance_with_looting", DatapackLoader.moshi(RandomChanceWithLooting.class)),
                Map.entry("minecraft:reference", DatapackLoader.moshi(Reference.class)),
                Map.entry("minecraft:survives_explosion", DatapackLoader.moshi(SurvivesExplosion.class)),
                Map.entry("minecraft:table_bonus", DatapackLoader.moshi(TableBonus.class)),
                Map.entry("minecraft:time_check", DatapackLoader.moshi(TimeCheck.class)),
                Map.entry("minecraft:value_check", DatapackLoader.moshi(ValueCheck.class)),
                Map.entry("minecraft:weather_check", DatapackLoader.moshi(WeatherCheck.class))
        ));
    }
}
