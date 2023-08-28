package net.minestom.vanilla.datapack.worldgen;

import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public interface Holder<T> {

    T value();

    NamespaceID key();

    static <T> Function<Object, Holder<T>> parser(Registry<T> registry, Function<Object, T> directParser) {
        return obj -> {
            if (Util.jsonIsString(obj)) {
                return reference(registry, NamespaceID.from(Util.jsonToString(obj)));
            } else {
                return direct(directParser.apply(obj), null);
            }
        };
    }

    static <T> Holder<T> direct(T value, @Nullable NamespaceID id) {
        return new Holder<>() {
            @Override
            public T value() {
                return value;
            }

            @Override
            public NamespaceID key() {
                return id;
            }
        };
    }

    static <T> Holder<T> reference(Registry<T> registry, NamespaceID id) {
        return new Holder<>() {
            @Override
            public T value() {
                return registry.getOrThrow(id);
            }

            @Override
            public NamespaceID key() {
                return id;
            }
        };
    }
}
