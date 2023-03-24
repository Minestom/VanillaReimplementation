package net.minestom.vanilla.datapack.loot.context;

import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public record TraitImpl<T>(String id, Class<T> type) implements LootContext.Trait<T> {
    @Override
    public Function<Object, @Nullable T> finder() {
        return o -> {
            if (type.isInstance(o)) {
                return type.cast(o);
            }
            return null;
        };
    }
}
