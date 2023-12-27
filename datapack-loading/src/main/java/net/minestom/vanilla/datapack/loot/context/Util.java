package net.minestom.vanilla.datapack.loot.context;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

class Util {

    public interface EmptyLootContext extends LootContext {
        @Override
        default <T> @Nullable T get(Trait<T> trait) {
            return null;
        }
    }

    public interface LootContextTraitMap<C extends LootContext> {
        <T> T obtain(C context, LootContext.Trait<T> trait);

        static <C extends LootContext> Builder<C> builder() {
            return new BuilderImpl<>();
        }

        interface Builder<C extends LootContext> {
            <T> Builder<C> put(LootContext.Trait<T> trait, Function<C, T> value);

            LootContextTraitMap<C> build();
        }
    }

    static class BuilderImpl<C extends LootContext> implements LootContextTraitMap.Builder<C> {
        private final Map<String, Function<C, ?>> map = new HashMap<>();

        @Override
        public <T> LootContextTraitMap.Builder<C> put(LootContext.Trait<T> trait, Function<C, T> value) {
            map.put(trait.id(), value);
            return this;
        }

        @Override
        public LootContextTraitMap<C> build() {
            Map<String, Function<C, ?>> copy = Map.copyOf(this.map);
            return new LootContextTraitMap<>() {
                @Override
                public <T> T obtain(C context, LootContext.Trait<T> trait) {
                    Object baseValue = copy.get(trait.id()).apply(context);
                    return trait.finder().apply(baseValue);
                }
            };
        }
    }

}
