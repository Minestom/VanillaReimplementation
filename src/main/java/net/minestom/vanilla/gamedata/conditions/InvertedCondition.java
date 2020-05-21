package net.minestom.vanilla.gamedata.conditions;

import com.google.gson.*;
import net.minestom.server.data.Data;
import net.minestom.server.gamedata.Condition;
import net.minestom.server.gamedata.loottables.ConditionContainer;
import net.minestom.server.gamedata.loottables.LootTableManager;

import java.lang.reflect.Type;

public class InvertedCondition implements Condition {

    private final Condition baseCondition;

    public InvertedCondition(Condition baseCondition) {
        this.baseCondition = baseCondition;
    }

    @Override
    public boolean test(Data data) {
        return !baseCondition.test(data);
    }

    public static class Deserializer implements JsonDeserializer<InvertedCondition> {
        private final LootTableManager lootTableManager;

        public Deserializer(LootTableManager lootTableManager) {
            this.lootTableManager = lootTableManager;
        }

        @Override
        public InvertedCondition deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            ConditionContainer container = context.deserialize(obj.getAsJsonObject("term"), ConditionContainer.class);
            return new InvertedCondition(container.create(lootTableManager));
        }
    }
}
