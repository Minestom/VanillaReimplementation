package net.minestom.vanilla.common.tag;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Base implementation of TagProvider that loads tags from registry data
 * @param <T> The type of element being tagged
 */
public abstract class RegistryTagProvider<T> implements TagProvider<T> {
    private final Map<String, Set<T>> values = new HashMap<>();
    private boolean initialized = false;
    private final String type;

    protected RegistryTagProvider(String type) {
        this.type = type;
    }

    /**
     * Maps a string key to the actual element
     * @param key The key to map
     * @return The mapped element
     */
    protected abstract T map(String key);

    /**
     * Ensures the tags are loaded
     */
    private void ensureInitialized() {
        if (!initialized) {
            loadAdditionalTags("blocksandstuff", RegistryTagProvider.class);
            initialized = true;
        }
    }

    /**
     * Loads additional tags from a specific path
     * @param path The path to load from
     * @param module The class to use for resource loading
     */
    public void loadAdditionalTags(String path, Class<?> module) {
        Map<String, Set<String>> raw = TagRegistryLoader.loadTags(path, "block", module);

        raw.forEach((k, v) -> {
            Set<T> mappedValues = v.stream()
                .map(this::map)
                .collect(Collectors.toSet());
            values.put(k, mappedValues);
        });
    }

    @Override
    public Set<T> getTaggedWith(String tag) {
        ensureInitialized();
        if (values.containsKey(tag)) {
            return values.get(tag);
        }
        return new HashSet<>();
    }

    @Override
    public boolean hasTag(T element, String tag) {
        ensureInitialized();
        Set<T> taggedElements = values.get(tag);
        return taggedElements != null && taggedElements.contains(element);
    }
}
