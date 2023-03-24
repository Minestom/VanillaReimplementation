package net.minestom.vanilla.datapack.loot;

import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.datapack.loot.context.LootContext;

import java.util.random.RandomGenerator;

// aka ItemFunction, or ItemModifier
public interface LootFunction extends InBuiltLootFunctions {

    /**
     * @return The function id.
     */
    NamespaceID lootFunctionId();

    /**
     * Applies the function to the item stack.
     *
     * @param context the function context
     * @return the modified item stack
     */
    ItemStack apply(Context context);

    static <T> T fromJson(String s) {
        return null;
    }

    /**
     * The context of the function.
     */
    interface Context extends LootContext {
        /**
         * The random generator used by the function.
         *
         * @return the random generator
         */
        RandomGenerator random();

        /**
         * The item stack to apply the function to.
         *
         * @return the previous item stack
         */
        ItemStack itemStack();

    }
}
