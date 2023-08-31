package net.minestom.vanilla.datapack.worldgen;

import com.squareup.moshi.JsonReader;
import net.minestom.server.instance.block.Block;
import net.minestom.vanilla.datapack.json.JsonUtils;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

public record BlockState(String name, Map<String, String> properties) {
    public Block toMinestom() {
        return Objects.requireNonNull(Block.fromNamespaceId(name), () -> "Unknown block: " + name)
                .withProperties(properties);
    }

    public static BlockState fromJson(JsonReader reader) throws IOException {
        reader.beginObject();
        String name = JsonUtils.findProperty(reader.peekJson(), "Name", JsonReader::nextString);
        Map<String, String> properties = JsonUtils.findProperty(reader.peekJson(), "Properties", json -> JsonUtils.readObjectToMap(json, JsonReader::nextString));
        Objects.requireNonNull(name, "expected a non-null name while passing BlockState.");
        properties = Objects.requireNonNullElseGet(properties, Map::of);
        while (reader.peek() != JsonReader.Token.END_OBJECT) {
            reader.skipName();
            reader.skipValue();
        }
        reader.endObject();
        return new BlockState(name, properties);
    }
}
