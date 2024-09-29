package net.minestom.vanilla.datapack.loot.function;

import com.squareup.moshi.JsonReader;
import net.minestom.server.item.ItemStack;
import net.kyori.adventure.key.Key;
import net.minestom.vanilla.datapack.json.JsonUtils;
import net.minestom.vanilla.datapack.loot.context.LootContext;

import java.io.IOException;
import java.util.Map;
import java.util.random.RandomGenerator;

// aka ItemFunction, or ItemModifier
// https://minecraft.fandom.com/wiki/Item_modifier
public interface LootFunction extends InBuiltLootFunctions {

    /**
     * @return The function id.
     */
    Key function();

    /**
     * Applies the function to the item stack.
     *
     * @param context the function context
     * @return the modified item stack
     */
    ItemStack apply(Context context);

    static LootFunction fromJson(JsonReader reader) throws IOException {
        return JsonUtils.unionStringTypeMapAdapted(reader, "function", Map.ofEntries(
                Map.entry("minecraft:apply_bonus", ApplyBonus.class),
                Map.entry("minecraft:copy_name", CopyName.class),
                Map.entry("minecraft:copy_nbt", CopyNbt.class),
                Map.entry("minecraft:copy_state", CopyState.class),
                Map.entry("minecraft:enchant_randomly", EnchantRandomly.class),
                Map.entry("minecraft:enchant_with_levels", EnchantWithLevels.class),
                Map.entry("minecraft:exploration_map", ExplorationMap.class),
                Map.entry("minecraft:explosion_decay", ExplosionDecay.class),
                Map.entry("minecraft:fill_player_head", FillPlayerHead.class),
                Map.entry("minecraft:furnace_smelt", FurnaceSmelt.class),
                Map.entry("minecraft:limit_count", LimitCount.class),
                Map.entry("minecraft:looting_enchant", LootingEnchant.class),
                Map.entry("minecraft:set_attributes", SetAttributes.class),
                Map.entry("minecraft:set_banner_pattern", SetBannerPattern.class),
                Map.entry("minecraft:set_contents", SetContents.class),
                Map.entry("minecraft:set_count", SetCount.class),
                Map.entry("minecraft:set_damage", SetDamage.class),
                Map.entry("minecraft:set_enchantments", SetEnchantments.class),
                Map.entry("minecraft:set_instrument", SetInstrument.class),
                Map.entry("minecraft:set_loot_table", SetLootTable.class),
                Map.entry("minecraft:set_lore", SetLore.class),
                Map.entry("minecraft:set_name", SetName.class),
                Map.entry("minecraft:set_nbt", SetNBT.class),
                Map.entry("minecraft:set_potion", SetPotion.class),
                Map.entry("minecraft:set_stew_effect", SetStewEffect.class)
        ));
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
