package net.minestom.vanilla.gamedata.conditions;

import com.google.gson.*;
import net.minestom.server.data.Data;
import net.minestom.server.gamedata.Condition;
import net.minestom.server.gamedata.loottables.ConditionContainer;
import net.minestom.server.gamedata.loottables.LootTableManager;

import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

public class AlternativesCondition implements Condition {

    private final List<Condition> terms;

    public AlternativesCondition(List<Condition> terms) {
        this.terms = terms;
    }

    @Override
    public boolean test(Data data) {
        for(Condition c : terms) {
            if(c.test(data))
                return true;
        }
        return false;
    }

    public static class Deserializer implements JsonDeserializer<AlternativesCondition> {
        private final LootTableManager lootTableManager;

        public Deserializer(LootTableManager lootTableManager) {
            this.lootTableManager = lootTableManager;
        }

        @Override
        public AlternativesCondition deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            List<Condition> terms = new LinkedList<>();
            for(JsonElement e : obj.getAsJsonArray("terms")) {
                ConditionContainer container = context.deserialize(e, ConditionContainer.class);
                terms.add(container.create(lootTableManager));
            }
            return new AlternativesCondition(terms);
        }
    }
}
