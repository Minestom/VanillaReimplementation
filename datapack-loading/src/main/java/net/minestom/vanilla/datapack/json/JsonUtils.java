package net.minestom.vanilla.datapack.json;

import com.squareup.moshi.JsonReader;
import net.kyori.adventure.key.Key;
import net.minestom.vanilla.datapack.DatapackLoader;
import okio.Buffer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JsonUtils {

    public static JsonReader jsonReader(String source) {
        try (Buffer buffer = new Buffer().writeUtf8(source)) {
            return JsonReader.of(buffer);
        }
    }

    public interface ObjectOrList<O, E> {
        /** Throws an exception if this is not an object */
        boolean isObject();
        O asObject();

        boolean isList();

        /** Throws an exception if this is not a list */
        List<E> asList();
    }

    public interface SingleOrList<T> extends ObjectOrList<T, T>, ListLike<T> {
        static <T> SingleOrList<T> fromJson(Type elementType, JsonReader reader) throws IOException {
            JsonReader.Token peek = reader.peek();

            if (peek != JsonReader.Token.BEGIN_ARRAY) {
                return new Single<>(DatapackLoader.<T>moshi(elementType).apply(reader));
            }

            Stream.Builder<T> builder = Stream.builder();
            reader.beginArray();
            while (reader.hasNext()) {
                builder.add(DatapackLoader.<T>moshi(elementType).apply(reader));
            }
            reader.endArray();
            return new List<>(builder.build().toList());
        }

        record Single<O>(O object) implements SingleOrList<O> {
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
            public java.util.List<O> asList() {
                throw new IllegalStateException("Not a list");
            }

            @Override
            public java.util.@NotNull List<O> list() {
                return java.util.List.of(object);
            }
        }

        record List<L>(java.util.List<L> list) implements SingleOrList<L> {
            @Override
            public boolean isObject() {
                return false;
            }

            @Override
            public L asObject() {
                throw new IllegalStateException("Not an object");
            }

            @Override
            public boolean isList() {
                return true;
            }

            @Override
            public java.util.List<L> asList() {
                return list;
            }

            @Override
            public java.util.@NotNull List<L> list() {
                return list;
            }
        }
    }

    public interface IoFunction<T, R> {
        R apply(T t) throws IOException;
    }

    public static <T> T unionStringType(JsonReader reader, String key, Function<String, IoFunction<JsonReader, T>> findReader) throws IOException {
        return unionMapType(reader, key, json -> {
            String value = json.nextString();
            if (value == null) return null;
            return Key.key(value).toString();
        }, findReader);
    }

    public static <T> T unionStringTypeAdapted(JsonReader reader, String key, Function<String, Class<? extends T>> findReader) throws IOException {
        return unionStringType(reader, key, str -> {
            Class<? extends T> clazz = findReader.apply(str);
            if (clazz == null) return null;
            return DatapackLoader.moshi(clazz);
        });
    }

    public static <T> T unionStringTypeMap(JsonReader reader, String key, Map<String, IoFunction<JsonReader, T>> map) throws IOException {
        return unionStringType(reader, key, map::get);
    }

    public static <T> T unionStringTypeMapAdapted(JsonReader reader, String key, Map<String, Class<? extends T>> map) throws IOException {
        Map<String, IoFunction<JsonReader, T>> adaptedMap = map.entrySet().stream()
                .map(entry -> {
                    var entryKey = entry.getKey();
                    var value = entry.getValue();
                    return Map.entry(entryKey, DatapackLoader.<T>moshi(value));
                })
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
        return unionStringTypeMap(reader, key, adaptedMap);
    }

    public static <V, T> T unionMapType(JsonReader reader, String key, IoFunction<JsonReader, V> read, Function<V, IoFunction<JsonReader, T>> findReader) throws IOException {
        // Fetch the property
        V property;
        try (JsonReader peek = reader.peekJson()) {
            peek.beginObject();

            property = JsonUtils.findProperty(peek, key, read);
            if (property == null)
                throw new IOException("Expected property '" + key + "'");
        }


        // Find the correct handler, and call it
        IoFunction<JsonReader, T> readFunction = findReader.apply(property);
        if (readFunction == null)
            throw new IOException("Unknown type: " + property);
        return readFunction.apply(reader);
    }

    public static <T> T typeMapMapped(JsonReader reader, Map<JsonReader.Token, IoFunction<JsonReader, T>> type2readFunction) throws IOException {
        return typeMap(reader, type2readFunction::get);
    }

    public static <T> T typeMap(JsonReader reader, IoFunction<JsonReader.Token, IoFunction<JsonReader, T>> type2readFunction) throws IOException {
        JsonReader.Token token = reader.peek();
        IoFunction<JsonReader, T> readFunction = type2readFunction.apply(token);
        if (readFunction == null) throw new IllegalStateException("Unknown token type: " + token);
        return readFunction.apply(reader);
    }

    /**
     * Note that this method MUTATES the reader.
     * In order to safely use this method, you should call {@link JsonReader#peekJson()} and use that instead.
     */
    public static <T> @Nullable T findProperty(JsonReader reader, String property, IoFunction<JsonReader, T> read) throws IOException {
        while (reader.hasNext() && reader.peek() != JsonReader.Token.END_OBJECT) {
            String name = reader.nextName();
            if (name.equals(property)) {
                return read.apply(reader);
            } else {
                reader.skipValue();
            }
        }
        return null;
    }

    public static boolean hasProperty(JsonReader reader, String property) throws IOException {
        JsonReader peek = reader.peekJson();
        while (peek.hasNext() && peek.peek() != JsonReader.Token.END_OBJECT) {
            String name = peek.nextName();
            if (name.equals(property)) {
                return true;
            } else {
                peek.skipValue();
            }
        }
        return false;
    }

    public static <T> Map<String, T> readObjectToMap(JsonReader reader, IoFunction<JsonReader, T> readValue) throws IOException {
        Map<String, T> map = new HashMap<>();
        reader.beginObject();
        while (reader.hasNext() && reader.peek() != JsonReader.Token.END_OBJECT) {
            String key = reader.nextName();
            T value = readValue.apply(reader);
            map.put(key, value);
        }
        reader.endObject();
        return Collections.unmodifiableMap(map);
    }
}
