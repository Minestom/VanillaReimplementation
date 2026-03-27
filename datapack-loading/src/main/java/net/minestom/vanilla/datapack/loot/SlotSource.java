package net.minestom.vanilla.datapack.loot;

import com.squareup.moshi.JsonReader;
import net.kyori.adventure.key.Key;
import net.minestom.vanilla.datapack.json.JsonUtils;
import net.minestom.vanilla.datapack.tags.ConditionsFor;

import java.io.IOException;
import java.util.List;

/**
 * Slot source definitions introduced in datapack version 92.0.
 */
public sealed interface SlotSource {
    static SlotSource fromJson(JsonReader reader) throws IOException {
        return JsonUtils.typeMap(reader, token -> switch (token) {
            case BEGIN_ARRAY -> json -> {
                json.beginArray();
                java.util.ArrayList<SlotSource> terms = new java.util.ArrayList<>();
                while (json.hasNext()) {
                    terms.add(fromJson(json));
                }
                json.endArray();
                return new Group(List.copyOf(terms));
            };
            case BEGIN_OBJECT -> json -> JsonUtils.unionStringTypeAdapted(json, "type", type -> switch (type) {
                case "minecraft:empty" -> Empty.class;
                case "minecraft:group" -> Group.class;
                case "minecraft:slot_range" -> SlotRange.class;
                case "minecraft:contents" -> Contents.class;
                case "minecraft:filtered" -> Filtered.class;
                case "minecraft:limit_slots" -> LimitSlots.class;
                default -> null;
            });
            default -> null;
        });
    }

    record Empty() implements SlotSource {
    }

    record Group(List<SlotSource> terms) implements SlotSource {
    }

    record SlotRange(String source, String slots) implements SlotSource {
    }

    record Contents(Key component, SlotSource slot_source) implements SlotSource {
    }

    record Filtered(ConditionsFor.Item item_filter, SlotSource slot_source) implements SlotSource {
    }

    record LimitSlots(int limit, SlotSource slot_source) implements SlotSource {
    }
}
