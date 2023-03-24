package net.minestom.vanilla.datapack.loot;

import net.minestom.vanilla.datapack.loot.context.LootContext;

public interface Predicate extends InBuiltPredicates {

    String condition();
    boolean test(LootContext context);
}
