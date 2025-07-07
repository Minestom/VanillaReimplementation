package net.minestom.vanilla.loot;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;

/**
 * Stores a dynamic amount of information that may be relevant during the generation of loot.
 */
public sealed interface LootContext permits LootContextImpl {

    @NotNull LootContext.Key<Random> RANDOM = LootContext.key("minecraft:random");
    @NotNull LootContext.Key<Float> EXPLOSION_RADIUS = LootContext.key("minecraft:explosion_radius");
    @NotNull LootContext.Key<Player> LAST_DAMAGE_PLAYER = LootContext.key("minecraft:last_damage_player");
    @NotNull LootContext.Key<Instance> WORLD = LootContext.key("minecraft:world");
    @NotNull LootContext.Key<ItemStack> TOOL = LootContext.key("minecraft:tool");
    @NotNull LootContext.Key<Boolean> ENCHANTMENT_ACTIVE = LootContext.key("minecraft:enchantment_active");
    @NotNull LootContext.Key<Block> BLOCK_STATE = LootContext.key("minecraft:block_state");
    @NotNull LootContext.Key<DamageType> DAMAGE_SOURCE = LootContext.key("minecraft:damage_source");
    @NotNull LootContext.Key<Point> ORIGIN = LootContext.key("minecraft:origin");
    @NotNull LootContext.Key<Entity> DIRECT_ATTACKING_ENTITY = LootContext.key("minecraft:direct_attacking_entity");
    @NotNull LootContext.Key<Entity> ATTACKING_ENTITY = LootContext.key("minecraft:attacking_entity");
    @NotNull LootContext.Key<Entity> THIS_ENTITY = LootContext.key("minecraft:this_entity");
    @NotNull LootContext.Key<Double> LUCK = LootContext.key("minecraft:luck");
    @NotNull LootContext.Key<Integer> ENCHANTMENT_LEVEL = LootContext.key("minecraft:enchantment_level");

    /**
     * Creates a loot context from the provided map of key -> object.
     * @param data the values of the context
     * @return the new context instance
     */
    static @NotNull LootContext from(@NotNull Map<Key<?>, Object> data) {
        return LootContextImpl.from(data);
    }

    /**
     * Creates a key from the provided key.
     */
    static <T> LootContext.@NotNull Key<T> key(@NotNull String key) {
        return new LootContext.Key<>(key);
    }

    /**
     * Represents a key that stores information in a loot context.
     * @param id the string id of the key
     * @param <T> the type parameter of the key
     */
    @SuppressWarnings("unused")
    record Key<T>(@NotNull String id) {}

    /**
     * Returns whether or not this context has the provided key.
     * @param key the key to search for
     * @return true if this context has the key, false if not
     */
    boolean has(@NotNull Key<?> key);

    /**
     * Gets the object associated with the provided key, returning null if not.
     * @param key the key to search for
     * @return the optional value
     * @param <T> the type of object desired
     */
    <T> @Nullable T get(@NotNull Key<T> key);

    /**
     * Gets the object associated with the provided key, returning the default value if not.
     * @param key the key to search for
     * @param defaultValue the default value to use
     * @return the optional value
     * @param <T> the type of object desired
     */
    <T> @NotNull T get(@NotNull Key<T> key, @NotNull T defaultValue);

    /**
     * Gets the object associated with the provided key, throwing an exception if not.
     * @param key the key to search for
     * @return the object associated with the provided key
     * @param <T> the type of object desired
     */
    <T> @NotNull T require(@NotNull Key<T> key);

}

record LootContextImpl(@NotNull Map<String, Object> data) implements LootContext {

    LootContextImpl {
        data = Map.copyOf(data);
    }

    static @NotNull LootContext from(@NotNull Map<Key<?>, Object> data) {
        Map<String, Object> mapped = new HashMap<>();
        for (Map.Entry<Key<?>, Object> entry : data.entrySet()) {
            mapped.put(entry.getKey().id(), entry.getValue());
        }

        return new LootContextImpl(mapped);
    }

    @Override
    public boolean has(@NotNull Key<?> key) {
        return data.containsKey(key.id());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> @Nullable T get(@NotNull Key<T> key) {
        return (T) data.get(key.id());
    }

    @Override
    public <T> @NotNull T get(@NotNull Key<T> key, @NotNull T defaultValue) {
        T get = get(key);
        return get != null ? get : defaultValue;
    }

    @Override
    public <T> @NotNull T require(@NotNull Key<T> key) {
        T get = get(key);
        if (get != null) {
            return get;
        }

        throw new NoSuchElementException("No value for key '" + key + "'");
    }
}