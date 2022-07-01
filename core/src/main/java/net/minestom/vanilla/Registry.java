package net.minestom.vanilla;

import net.minestom.server.utils.NamespaceID;

public interface Registry<T> {

    static <I extends Registry<T>, T> Registry<T> create(Class<I> implClass) {
        var registry = new RegistryImpl<T>();
        RegistryImpl.REGISTRIES.put(implClass, registry);
        return registry;
    }

    static <T> void register(Class<Registry<T>> registry, NamespaceID key, T value) {
        ((Registry<T>) RegistryImpl.REGISTRIES.get(registry)).register(key, value);
    }


    static <T> T get(Class<? extends Registry<T>> registry, NamespaceID key) {
        return ((Registry<T>) RegistryImpl.REGISTRIES.get(registry)).get(key);
    }


    static <T> boolean isPresent(Class<? extends Registry<T>> registry, NamespaceID key) {
        return ((Registry<T>) RegistryImpl.REGISTRIES.get(registry)).get(key) != null;
    }


    T get(NamespaceID key);

    void register(NamespaceID key, T value);

}
