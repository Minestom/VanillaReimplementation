package net.minestom.vanilla.datapack.loot.context;

import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public record MappedTraitImpl<T, N>(LootContext.Trait<T> trait, Function<T, N> mapper) implements LootContext.Trait<N> {
    @Override
    public String id() {
        return trait.id();
    }

    @Override
    public Function<Object, @Nullable N> finder() {
        return baseValue -> {
            T value = trait.finder().apply(baseValue);
            return value == null ? null : mapper.apply(value);
        };
    }
}
