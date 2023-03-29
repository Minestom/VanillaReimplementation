package net.minestom.vanilla.datapack.json;

import com.squareup.moshi.JsonReader;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Map;

public class JsonUtils {

    public interface ObjectOrList<O, E> {
        boolean isObject();
        O asObject();

        boolean isList();
        List<E> asList();

        record Object<O>(O object) implements ObjectOrList<O, Void> {
            @Override
            public boolean isObject() {
                return true;
            }

            @Override
            public O asObject() {
                return object;
            }

            @Override
            public boolean isList() {
                return false;
            }

            @Override
            public List<Void> asList() {
                throw new IllegalStateException("Not a list");
            }
        }

        record List<L>(List<L> list) implements ObjectOrList<Void, L> {
            @Override
            public boolean isObject() {
                return false;
            }

            @Override
            public Void asObject() {
                throw new IllegalStateException("Not an object");
            }

            @Override
            public boolean isList() {
                return true;
            }

            @Override
            public List<L> asList() {
                return list;
            }
        }
    }

    public interface IoFunction<T, R> {
        R apply(T t) throws IOException;
    }

    public static <T> T unionNamespaceStringType(JsonReader reader, String key, Map<String, IoFunction<JsonReader, T>> map) throws IOException {
        return unionMapType(reader, key, json -> {
            String value = json.nextString();
            if (value == null) return null;
            return NamespaceID.from(value).toString();
        }, map);
    }

    public static <V, T> T unionMapType(JsonReader reader, String key, IoFunction<JsonReader, V> read, Map<V, IoFunction<JsonReader, T>> map) throws IOException {
        // Fetch the property
        V property;
        try (JsonReader peek = reader.peekJson()) {
            peek.beginObject();

            property = JsonUtils.findProperty(peek, key, read);
            if (property == null)
                throw new IOException("Expected property '" + key + "'");
        }


        // Find the correct handler, and call it
        IoFunction<JsonReader, T> readFunction = map.get(property);
        if (readFunction == null) throw new IOException("Unknown type: " + property);
        return readFunction.apply(reader);
    }

    public static <T> T typeMap(JsonReader reader, Map<JsonReader.Token, IoFunction<JsonReader, T>> type2readFunction) throws IOException {
        JsonReader.Token token = reader.peek();
        IoFunction<JsonReader, T> readFunction = type2readFunction.get(token);
        if (readFunction == null) throw new IllegalStateException("Unknown token type: " + token);
        return readFunction.apply(reader);
    }

    public static <T> @Nullable T findProperty(JsonReader reader, String property, IoFunction<JsonReader, T> read) throws IOException {
        while (reader.selectName(JsonReader.Options.of(property)) == -1) {
            if (reader.peek() == JsonReader.Token.END_OBJECT)
                return null;
            reader.skipName();
            reader.skipValue();
        }
        return read.apply(reader);
    }


}
