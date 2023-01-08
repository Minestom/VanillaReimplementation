package net.minestom.vanilla.generation;

import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class Registry<T> {
    public static final Registry<Registry<?>> REGISTRY = new Registry<>(NamespaceID.from("root"));

    private final Map<NamespaceID, T> storage = new HashMap<>();
    private final Map<NamespaceID, T> builtin = new HashMap<>();

    public final NamespaceID key;
    public @Nullable Function<Object, T> parser;

    Registry(NamespaceID key, @Nullable Function<Object, T> parser) {
        this.key = key;
        this.parser = parser;
    }

    Registry(NamespaceID key) {
        this(key, null);
    }

    public Holder<T> register(NamespaceID id, T value, boolean builtin) {
        this.storage.put(id, value);
        if (builtin) {
            this.builtin.put(id, value);
        }
        return Holder.reference(this, id);
    }

    public Holder<T> register(NamespaceID id, T value) {
        return this.register(id, value, false);
    }

    public @Nullable T delete(NamespaceID id) {
        T deleted = this.storage.remove(id);
        this.builtin.remove(id);
        return deleted;
    }

    public Set<NamespaceID> keys() {
        return Collections.unmodifiableSet(storage.keySet());
    }

    public boolean as(NamespaceID id) {
        return this.storage.containsKey(id);
    }

    public T get(NamespaceID id) {
        return this.storage.get(id);
    }

    public T getOrThrow(NamespaceID id) {
        T value = this.get(id);
        if (value == null) {
            throw new IllegalArgumentException("No value for " + id + " in " + this.key);
        }
        return value;
    }

    public T parse(Object obj) {
        if (this.parser == null) {
            throw new Error("No parser exists for " + this.key);
        }
        return this.parser.apply(obj);
    }

    public void clear() {
        this.storage.clear();
        this.storage.putAll(this.builtin);
    }

    public Registry<T> assign(Registry<T> other) {
        if (!this.key.equals(other.key)) {
            throw new Error("Cannot assign registry of type " + other.key + " to registry of type " + this.key);
        }
        for (var key : other.keys()) {
            this.storage.put(key, other.getOrThrow(key));
        }
        return this;
    }

    public Registry<T> cloneEmpty() {
        return new Registry<>(this.key, this.parser);
    }

    public void forEach(Consumer<Entry<T>> entryConsumer) {
        for (var entry : this.storage.entrySet()) {
            entryConsumer.accept(new Entry<>() {
                @Override
                public NamespaceID key() {
                    return entry.getKey();
                }

                @Override
                public T value() {
                    return entry.getValue();
                }

                @Override
                public Registry<T> registry() {
                    return Registry.this;
                }
            });
        }
    }

    interface Entry<T> {
        NamespaceID key();

        T value();

        Registry<T> registry();
    }

    public <U> List<U> map(Function<Entry<T>, U> fn) {
        return this.storage.entrySet().stream().map(entry -> fn.apply(new Entry<>() {
            @Override
            public NamespaceID key() {
                return entry.getKey();
            }

            @Override
            public T value() {
                return entry.getValue();
            }

            @Override
            public Registry<T> registry() {
                return Registry.this;
            }
        })).toList();
    }
}
