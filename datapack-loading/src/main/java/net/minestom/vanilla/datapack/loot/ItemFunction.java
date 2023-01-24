package net.minestom.vanilla.datapack.loot;

import net.kyori.adventure.text.Component;
import net.minestom.server.item.Enchantment;
import net.minestom.server.item.ItemStack;
import net.minestom.vanilla.datapack.EntryProvider;
import net.minestom.vanilla.datapack.number.NumberProvider;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.random.RandomGenerator;

public record ItemFunction(String function, List<ItemPredicate> conditions, InBuiltLogic logic) {
//    public ItemFunction(String function, List<ItemPredicate> conditions) {
//        this(function, conditions, InBuiltLogic.fromId(function));
//    }
//    ItemStack apply(Context context);

    interface InBuiltLogic {
        String nativeId();
    }

    interface Context {
        ItemStack stack();
        default int enchant(Enchantment enchantment) {
            return stack().meta().getEnchantmentMap().get(enchantment);
        }
        RandomGenerator random();
    }

    interface ItemCountProvider {
        int apply(Context context);
    }

    // Applies a predefined bonus formula to the count of the item stack.
    record ApplyBonus(ItemCountProvider formula) implements InBuiltLogic {
        @Override public String nativeId() { return "apply_bonus"; }

        record BinomialWithBonusCount(Enchantment enchantment, int extra, double probability) implements ItemCountProvider {
            @Override
            public int apply(Context context) {
                int enchantLevel = context.enchant(enchantment);
                double n = enchantLevel + extra;

                RandomGenerator random = context.random();
                double sum = 0;
                for (int i = 0; i < n; i++) {
                    if (random.nextDouble() < probability) {
                        sum++;
                    }
                }
                return (int) sum;
            }
        }

        record UniformBonusCount(Enchantment enchantment, double bonusMultiplier) implements ItemCountProvider {
            @Override
            public int apply(Context context) {
                double n = context.enchant(enchantment) * bonusMultiplier;
                int count = (int) context.random().nextDouble(n);;
                return count;
            }
        }

        record OreDrops(Enchantment enchantment) implements ItemCountProvider {
            @Override
            public int apply(Context context) {
                int itemCount = context.stack().amount() * (1 + context.random().nextInt(context.enchant(enchantment) + 2));
                return itemCount;
            }
        }
    }

    // Copies an entity's or a block entity's name tag into the item's "display.Name" tag.
    record CopyName(String source) implements InBuiltLogic {
        @Override public String nativeId() { return "copy_name"; }
    }

    // Copies NBT values from a specified block entity or entity, or from command storage to the item's "tag" tag.
    record CopyNbt(Source source) implements InBuiltLogic {
        @Override public String nativeId() { return "copy_nbt"; }
        public CopyNbt(String source) {
            this(new Source.Context(source));
        }
        interface Source {
            record Context(String target) implements Source {
            }
            record Storage(String source) implements Source {
            }
        }
    }

    // Copies block state properties provided by loot context to the item's "BlockStateTag" tag.
    record CopyState(String block, List<String> properties) implements InBuiltLogic {
        @Override public String nativeId() { return "copy_state"; }
    }

    // Enchants the item with one randomly-selected enchantment. The power of the enchantment, if applicable, is random.
    // A book will convert to an enchanted book when enchanted.
    record EnchantRandomly(List<Enchantment> enchantments) implements InBuiltLogic {
        @Override public String nativeId() { return "enchant_randomly"; }
    }

    // Enchants the item, with the specified enchantment level (roughly equivalent to using an enchantment table at that level). A book will convert to an enchanted book.
    record EnchantWithLevels(boolean treasure, NumberProvider.Int levels) implements InBuiltLogic {
        @Override public String nativeId() { return "enchant_with_levels"; }
        public EnchantWithLevels(NumberProvider.Int levels) {
            this(false, levels);
        }
    }

    // If the origin is provided by loot context, converts an empty map into an explorer map leading to a nearby generated structure.
    record ExplorationMap(String destination, String decoration, int zoom, int search_radius, boolean skip_existing_chunks) implements InBuiltLogic {
        @Override public String nativeId() { return "exploration_map"; }
    }

    // Removes some items from a stack, if the explosion radius is provided by loot context. Each item in the item stack
    // has a chance of 1/explosion radius to be lost.
    record ExplosionDecay() implements InBuiltLogic {
        @Override public String nativeId() { return "explosion_decay"; }
    }

    // Adds required item tags of a player head.
    record FillPlayerHead(String entity) implements InBuiltLogic {
        @Override public String nativeId() { return "fill_player_head"; }
    }

    // Smelts the item as it would be in a furnace without changing its count.
    record FurnaceSmelt() implements InBuiltLogic {
        @Override public String nativeId() { return "furnace_smelt"; }
    }

    // Limits the count of every item stack.
    record LimitCount(NumberProvider.Int min, NumberProvider.Int max) implements InBuiltLogic {
        @Override public String nativeId() { return "limit_count"; }
        public LimitCount(NumberProvider.Int value) {
            this(value, value);
        }
    }

    // Adjusts the stack size based on the level of the Looting enchantment on the killer entity provided by loot context.
    record LootingEnchant(NumberProvider.Int count, int limit) implements InBuiltLogic {
        @Override public String nativeId() { return "looting_enchant"; }
        public LootingEnchant(NumberProvider.Int count) {
            this(count, 0);
        }
    }

    // Add attribute modifiers to the item.
    record SetAttributes(List<AttributeModifier> modifiers) implements InBuiltLogic {
        @Override public String nativeId() { return "set_attributes"; }
        record AttributeModifier(String name, String attribute, String operation, NumberProvider.Double amount, @Nullable String id, List<String> slot) {
        }
    }

    // Adds or replaces banner patterns of a banner. Function successfully adds patterns into NBT tag even if invoked on a non-banner.
    record SetBannerPattern(List<Pattern> patterns, boolean required) implements InBuiltLogic {
        @Override public String nativeId() { return "set_banner_pattern"; }
        record Pattern(String pattern, String color) {
        }
    }

    // Sets the contents of a container block item to a list of entries.
    record SetContents(List<EntryProvider> entries, String type) implements InBuiltLogic {
        @Override public String nativeId() { return "set_contents"; }
    }

    // Sets the stack size.
    record SetCount(NumberProvider.Int count) implements InBuiltLogic {
        @Override public String nativeId() { return "set_count"; }
    }

    // Sets the item's damage value (durability).
    record SetDamage(NumberProvider.Double damage, boolean add) implements InBuiltLogic {
        @Override public String nativeId() { return "set_damage"; }
        public SetDamage(NumberProvider.Double damage) {
            this(damage, false);
        }
    }

    // Modifies the item's enchantments. A book will convert to an enchanted book.
    record SetEnchantments(Map<Enchantment, NumberProvider.Int> enchantments, boolean add) implements InBuiltLogic {
        @Override public String nativeId() { return "set_enchantments"; }
        public SetEnchantments(Map<Enchantment, NumberProvider.Int> enchantments) {
            this(enchantments, false);
        }
    }

    // Sets the item tags for instrument items to a random value from a tag.
    record SetInstrument(String options) implements InBuiltLogic {
        @Override public String nativeId() { return "set_instrument"; }
    }

    // Sets the loot table for a container block when placed and opened.
    record SetLootTable(String name_, long seed, String type) implements InBuiltLogic {
        @Override public String nativeId() { return "set_loot_table"; }
        public SetLootTable(String name, String type) {
            this(name, 0, type);
        }
    }

    // Adds or changes the item's lore.
    record SetLore(List<Component> lore, String entity, boolean replace) implements InBuiltLogic {
        @Override public String nativeId() { return "set_lore"; }
        public SetLore(List<Component> lore, String entity) {
            this(lore, entity, false);
        }
    }

    // Adds or changes the item's custom name.
    record SetName(Component name, String entity) implements InBuiltLogic {
        @Override public String nativeId() { return "set_name"; }
    }

    // Adds or changes NBT data of the item.
    record SetNbt(String tag) implements InBuiltLogic {
        @Override public String nativeId() { return "set_nbt"; }
    }

    // Sets the "Potion" tag of an item.
    record SetPotion(String id) implements InBuiltLogic {
        @Override public String nativeId() { return "set_potion"; }
    }

    // Sets the status effects for suspicious stew. Fails if invoked on an item that is not suspicious stew.
    record SetStewEffect(List<Effect> effects) implements InBuiltLogic {
        @Override public String nativeId() { return "set_stew_effect"; }
        record Effect(String type, NumberProvider.Int duration) {
        }
    }
}
