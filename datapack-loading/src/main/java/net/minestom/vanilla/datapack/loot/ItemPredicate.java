package net.minestom.vanilla.datapack.loot;

import net.minestom.vanilla.datapack.LootContext;

import java.util.List;
import java.util.Map;

public interface ItemPredicate {

    String condition();
    boolean test(LootContext context);

    // Evaluates a list of predicates and passes if any one of them passes. Invokable from any context.
    record Alternative(List<ItemPredicate> terms) implements ItemPredicate {
        @Override public String condition() { return "alternative"; }

        @Override
        public boolean test(LootContext context) {
            for (ItemPredicate term : terms) {
                if (term.test(context)) {
                    return true;
                }
            }
            return false;
        }
    }

    // Checks the mined block and its block states. Requires block state provided by loot context, and always fails if
    // not provided.
    record BlockStateProperty(String block, Map<String, Property> properties) {
        interface Property {
            record Exact(String name) implements Property {
            }
            record Range(String min, String max) implements Property {
            }
        }
    }
}
