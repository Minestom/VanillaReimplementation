package net.minestom.vanilla.datapack.loot;

import com.squareup.moshi.JsonReader;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.datapack.DatapackLoader;
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
    NamespaceID function();

    /**
     * Applies the function to the item stack.
     *
     * @param context the function context
     * @return the modified item stack
     */
    ItemStack apply(Context context);

    static LootFunction fromJson(JsonReader reader) throws IOException {
        return JsonUtils.unionNamespaceStringType(reader, "function", Map.ofEntries(
                Map.entry("minecraft:apply_bonus", DatapackLoader.moshi(ApplyBonus.class)),
                Map.entry("minecraft:copy_name", DatapackLoader.moshi(CopyName.class)),
                Map.entry("minecraft:copy_nbt", DatapackLoader.moshi(CopyNbt.class)),
                Map.entry("minecraft:copy_state", DatapackLoader.moshi(CopyState.class)),
                Map.entry("minecraft:enchant_randomly", DatapackLoader.moshi(EnchantRandomly.class)),
                Map.entry("minecraft:enchant_with_levels", DatapackLoader.moshi(EnchantWithLevels.class)),
                Map.entry("minecraft:exploration_map", DatapackLoader.moshi(ExplorationMap.class)),
                Map.entry("minecraft:explosion_decay", DatapackLoader.moshi(ExplosionDecay.class)),
                Map.entry("minecraft:fill_player_head", DatapackLoader.moshi(FillPlayerHead.class)),
                Map.entry("minecraft:furnace_smelt", DatapackLoader.moshi(FurnaceSmelt.class)),
                Map.entry("minecraft:limit_count", DatapackLoader.moshi(LimitCount.class)),
                Map.entry("minecraft:looting_enchant", DatapackLoader.moshi(LootingEnchant.class)),
                Map.entry("minecraft:set_attributes", DatapackLoader.moshi(SetAttributes.class)),
                Map.entry("minecraft:set_banner_pattern", DatapackLoader.moshi(SetBannerPattern.class)),
                Map.entry("minecraft:set_contents", DatapackLoader.moshi(SetContents.class)),
                Map.entry("minecraft:set_count", DatapackLoader.moshi(SetCount.class)),
                Map.entry("minecraft:set_damage", DatapackLoader.moshi(SetDamage.class)),
                Map.entry("minecraft:set_enchantments", DatapackLoader.moshi(SetEnchantments.class)),
                Map.entry("minecraft:set_instrument", DatapackLoader.moshi(SetInstrument.class)),
                Map.entry("minecraft:set_loot_table", DatapackLoader.moshi(SetLootTable.class)),
                Map.entry("minecraft:set_lore", DatapackLoader.moshi(SetLore.class)),
                Map.entry("minecraft:set_name", DatapackLoader.moshi(SetName.class)),
                Map.entry("minecraft:set_nbt", DatapackLoader.moshi(SetNBT.class)),
                Map.entry("minecraft:set_potion", DatapackLoader.moshi(SetPotion.class)),
                Map.entry("minecraft:set_stew_effect", DatapackLoader.moshi(SetStewEffect.class))
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
