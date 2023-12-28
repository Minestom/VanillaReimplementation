package net.minestom.vanilla.datapack.loot;

import com.squareup.moshi.JsonReader;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.datapack.Datapack;
import net.minestom.vanilla.datapack.DatapackUtils;
import net.minestom.vanilla.datapack.json.JsonUtils;
import net.minestom.vanilla.datapack.loot.context.LootContext;
import net.minestom.vanilla.datapack.loot.function.LootFunction;
import net.minestom.vanilla.datapack.loot.function.Predicate;
import net.minestom.vanilla.datapack.number.NumberProvider;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record LootTable(@Nullable String type, @Nullable List<LootFunction> functions, List<Pool> pools) {
    public record Pool(List<Predicate> conditions,
                       @Nullable List<LootFunction> functions,
                       NumberProvider rolls,
                       NumberProvider bonus_rolls,
                       List<Pool.Entry> entries) {

        public sealed interface Entry {
            @Nullable List<Predicate> conditions();

            NamespaceID type();

            static Pool.Entry fromJson(JsonReader reader) throws IOException {
                return JsonUtils.unionStringTypeAdapted(reader, "type", type -> switch(type) {
                    case "minecraft:item" -> Pool.Entry.Item.class;
                    case "minecraft:tag" -> Pool.Entry.Tag.class;
                    case "minecraft:loot_table" -> Pool.Entry.LootTableNested.class;
                    case "minecraft:dynamic" -> Pool.Entry.Dynamic.class;
                    case "minecraft:empty" -> Pool.Entry.Empty.class;
                    case "minecraft:group" -> Pool.Entry.Group.class;
                    case "minecraft:alternatives" -> Pool.Entry.Alternatives.class;
                    case "minecraft:sequence" -> Pool.Entry.Sequence.class;
                    default -> null;
                });
            }

            sealed interface ItemGenerator extends Entry {
                @Nullable List<LootFunction> functions();
                @Nullable NumberProvider weight();
                NumberProvider quality();

                /**
                 * Each element in this list is a singular loot entry.
                 */
                List<List<ItemStack>> apply(Datapack datapack, LootContext context);
            }

            /**
             * item -> Provides a loot entry that drops a single item stack.
             * • functions: Invokes item functions to the item stack(s).
             * - An item function. The JSON structure of this object is described on the Item modifiers page.
             * • weight: Determines how often the loot entry is chosen out of all the entries in the pool. Entries with higher weights are used more often. The chance of an entry being chosen is [this entry's weight ÷ total of all considered entries' weights].
             * • quality: Modifies the loot entry's weight based on the luck attribute of the killer_entity for loot context type entity or the this entity for all other loot table types. Formula is floor(weight + (quality × generic.luck)).
             * • name: The resource location of the item to be generated, e.g. minecraft:diamond. The default, if not changed by item functions, is a stack of 1 of the default instance of the item.
             */
            record Item(List<Predicate> conditions,
                        List<LootFunction> functions,
                        NumberProvider weight,
                        NumberProvider quality,
                        NamespaceID name,
                        @Nullable Integer count) implements ItemGenerator {

                @Override
                public NamespaceID type() {
                    return NamespaceID.from("minecraft:item");
                }

                @Override
                public List<List<ItemStack>> apply(Datapack datapack, LootContext context) {
                    return List.of(List.of(
                            ItemStack.of(Objects.requireNonNull(Material.fromNamespaceId(name)), count == null ? 1 : count)
                    ));
                }
            }

            /**
             * tag -> Provides a loot entry that generates all item in an item tag, or multiple loot entries (each entry generates a single item in the item tag).
             * • functions: Invokes item functions to the item stack(s).
             * - An item function. The JSON structure of this object is described on the Item modifiers page.
             * • weight: Determines how often a loot entry is chosen out of all the entries in the pool. Entries with higher weights are used more often. The chance of an entry being chosen is [this entry's weight ÷ total of all considered entries' weights].
             * • quality: Modifies the loot entry's weight based on the luck attribute of the killer_entity for loot context type entity or the this entity for all other loot table types. Formula is floor(weight + (quality × generic.luck)).
             * • name: The resource location of the item tag to query, e.g. minecraft:arrows.
             * • expand: If set to true, provides one loot entry per item in the tag with the same weight and quality, and each entry generates one item. If false, provides a single loot entry that generates all items (each with count of 1) in the tag.
             */
            record Tag(List<Predicate> conditions,
                       List<LootFunction> functions,
                       NumberProvider weight,
                       NumberProvider quality,
                       NamespaceID name,
                       boolean expand) implements ItemGenerator {

                @Override
                public NamespaceID type() {
                    return NamespaceID.from("minecraft:tag");
                }

                @Override
                public List<List<ItemStack>> apply(Datapack datapack, LootContext context) {
                    List<List<ItemStack>> result = new ArrayList<>();

                    var itemTags = DatapackUtils.findTags(datapack, "items", name);

                    var items = itemTags.stream()
                            .map(Material::fromNamespaceId)
                            .filter(Objects::nonNull)
                            .map(material -> ItemStack.of(material, 1))
                            .toList();

                    if (expand) {
                        for (var item : items) {
                            result.add(List.of(item));
                        }
                    } else {
                        result.add(items);
                    }

                    return List.copyOf(result);
                }
            }

            /**
             * loot_table -> Provides another loot table as a loot entry.
             * • functions: Invokes item functions to the item stack(s).
             * - An item function. The JSON structure of this object is described on the Item modifiers page.
             * • weight: Determines how often the loot entry is chosen out of all the entries in the pool. Entries with higher weights are used more often. The chance of an entry being chosen is [this entry's weight ÷ total of all considered entries' weights].
             * • quality: Modifies the loot entry's weight based on the luck attribute of the killer_entity for loot context type entity or the this entity for all other loot table types. Formula is floor(weight + (quality × generic.luck)).
             * • name: The resource location of the loot table to be used, e.g. minecraft:gameplay/fishing/junk.
             */
            record LootTableNested(List<Predicate> conditions,
                                   List<LootFunction> functions,
                                   NumberProvider weight,
                                   NumberProvider quality,
                                   NamespaceID name) implements Pool.Entry {

                @Override
                public NamespaceID type() {
                    return NamespaceID.from("minecraft:loot_table");
                }
            }

            /**
             * dynamic -> Provides a loot entry that generates block-specific drops.
             * • functions: Invokes item functions to the item stack(s).
             * - An item function. The JSON structure of this object is described on the Item modifiers page.
             * • weight: Determines how often the loot entry is chosen out of all the entries in the pool. Entries with higher weights are used more often. The chance of an entry being chosen is [this entry's weight ÷ total of all considered entries' weights].
             * • quality: Modifies the loot entry's weight based on the luck attribute of the killer_entity for loot context type entity or the this entity for all other loot table types. Formula is floor(weight + (quality × generic.luck)).
             * • name: Can be contents to drop block entity contents.
             */
            record Dynamic(List<Predicate> conditions,
                           List<LootFunction> functions,
                           NumberProvider weight,
                           NumberProvider quality,
                           String name) implements ItemGenerator {

                @Override
                public NamespaceID type() {
                    return NamespaceID.from("minecraft:dynamic");
                }

                @Override
                public List<List<ItemStack>> apply(Datapack datapack, LootContext context) {
                    Block blockEntity = context.get(LootContext.BLOCK_ENTITY);
                    // TODO: Drop chest contents
                    return List.of(List.of());
                }
            }

            /**
             * empty -> Provides a loot entry that generates nothing into the loot pool.
             * • functions: Invokes item functions to the item stack(s).
             * - An item function. The JSON structure of this object is described on the Item modifiers page.
             * • weight: Determines how often the loot entry is chosen out of all the entries in the pool. Entries with higher weights are used more often. The chance of an entry being chosen is [this entry's weight ÷ total of all considered entries' weights].
             * • quality: Modifies the loot entry's weight based on the luck attribute of the killer_entity for loot context type entity or the this entity for all other loot table types. Formula is floor(weight + (quality × generic.luck)).
             */
            record Empty(List<Predicate> conditions,
                         List<LootFunction> functions,
                         NumberProvider weight,
                         NumberProvider quality) implements ItemGenerator {

                @Override
                public NamespaceID type() {
                    return NamespaceID.from("minecraft:empty");
                }

                @Override
                public List<List<ItemStack>> apply(Datapack datapack, LootContext context) {
                    return List.of(List.of());
                }
            }

            /**
             * group -> All entry providers in the children list is applied into the loot pool. Can be used for convenience, e.g. if one condition applies for multiple entries.
             * • children: The list of entry providers.
             * - An entry provider.
             */
            record Group(List<Predicate> conditions,
                         List<Pool.Entry> children) implements Pool.Entry {

                @Override
                public NamespaceID type() {
                    return NamespaceID.from("minecraft:group");
                }
            }

            /**
             * alternatives -> Only the first successful (conditions are met) entry provider, in order, is applied to the loot pool.
             * • children: The list of entry providers.
             * - An entry provider.
             */
            record Alternatives(List<Predicate> conditions,
                                List<Pool.Entry> children) implements Pool.Entry {

                @Override
                public NamespaceID type() {
                    return NamespaceID.from("minecraft:alternatives");
                }
            }

            /**
             * sequence -> The child entry providers are applied to the loot pool in sequential order, continuing until an entry provider's conditions are not met, then applying no more entry providers from the children.
             * • children: The list of entry providers.
             * - An entry provider.
             */
            record Sequence(List<Predicate> conditions,
                            List<Pool.Entry> children) implements Pool.Entry {

                @Override
                public NamespaceID type() {
                    return NamespaceID.from("minecraft:sequence");
                }
            }
        }
    }
}
