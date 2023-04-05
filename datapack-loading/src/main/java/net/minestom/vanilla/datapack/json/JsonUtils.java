package net.minestom.vanilla.datapack.json;

import com.squareup.moshi.JsonReader;
import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.datapack.DatapackLoader;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class JsonUtils {

    public interface ObjectOrList<O, E> {
        boolean isObject();
        O asObject() throws IllegalStateException;

        boolean isList();
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
            public java.util.List<O> list() {
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
            public java.util.List<L> list() {
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


}
