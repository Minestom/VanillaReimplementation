package net.minestom.vanilla.datapack;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.squareup.moshi.JsonDataException;
import com.squareup.moshi.JsonReader;

import java.io.IOException;

public class JsonUtils {

    public static JsonElement moshiToGson(JsonReader reader) throws IOException {
        return switch (reader.peek()) {
            case BEGIN_ARRAY -> {
                var array = new com.google.gson.JsonArray();
                reader.beginArray();
                while (reader.hasNext()) {
                    array.add(moshiToGson(reader));
                }
                reader.endArray();
                yield array;
            }
            case BEGIN_OBJECT -> {
                var object = new JsonObject();
                reader.beginObject();
                while (reader.hasNext()) {
                    object.add(reader.nextName(), moshiToGson(reader));
                }
                reader.endObject();
                yield object;
            }
            case STRING -> new com.google.gson.JsonPrimitive(reader.nextString());
            case NUMBER -> {
                try {
                    yield new com.google.gson.JsonPrimitive(reader.nextDouble());
                } catch (JsonDataException e) {
                    try {
                        yield new com.google.gson.JsonPrimitive(reader.nextInt());
                    } catch (JsonDataException e2) {
                        yield new com.google.gson.JsonPrimitive(reader.nextLong());
                    }
                }
            }
            case BOOLEAN -> new com.google.gson.JsonPrimitive(reader.nextBoolean());
            case NULL -> {
                reader.nextNull();
                yield com.google.gson.JsonNull.INSTANCE;
            }

            default -> throw new IllegalStateException("Unexpected value: " + reader.peek());
        };
    }
}
