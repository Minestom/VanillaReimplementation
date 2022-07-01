package net.minestom.vanilla;

import net.minestom.server.utils.NamespaceID;

import java.util.HashMap;
import java.util.Map;

public class RegistryImpl<T> implements Registry<T> {
    static Map<Class<?>, RegistryImpl<?>> REGISTRIES = new HashMap<>();

    private final Map<NamespaceID, T> registry = new HashMap<>();

    @Override
    public T get(NamespaceID key) {
        return registry.get(key);
    }

    @Override
    public void register(NamespaceID key, T value) {
        registry.put(key, value);
    }
}
