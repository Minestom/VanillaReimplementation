package net.minestom.vanilla.datapack.loot;

import com.squareup.moshi.JsonReader;
import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.datapack.DatapackLoader;
import net.minestom.vanilla.datapack.json.JsonUtils;
import net.minestom.vanilla.datapack.loot.function.LootFunction;
import net.minestom.vanilla.datapack.loot.function.Predicate;
import net.minestom.vanilla.datapack.number.NumberProvider;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public record LootTable(@Nullable String type, List<LootFunction> functions, List<Pool> pools) {
    public record Pool(List<Predicate> conditions,
                       List<LootFunction> functions,
                       NumberProvider rolls,
                       NumberProvider bonus_rolls,
                       List<Pool.Entry> entries) {

        public sealed interface Entry {
            List<Predicate> conditions();

            NamespaceID type();

            static Pool.Entry fromJson(JsonReader reader) throws IOException {
                return JsonUtils.unionMapType(reader, "type", JsonReader::nextString, Map.of(
                        "minecraft:item", DatapackLoader.moshi(Pool.Entry.Item.class),
                        "minecraft:tag", DatapackLoader.moshi(Pool.Entry.Tag.class),
                        "minecraft:loot_table", DatapackLoader.moshi(Pool.Entry.LootTableNested.class),
                        "minecraft:dynamic", DatapackLoader.moshi(Pool.Entry.Dynamic.class),
                        "minecraft:empty", DatapackLoader.moshi(Pool.Entry.Empty.class),
                        "minecraft:group", DatapackLoader.moshi(Pool.Entry.Group.class),
                        "minecraft:alternatives", DatapackLoader.moshi(Pool.Entry.Alternatives.class),
                        "minecraft:sequence", DatapackLoader.moshi(Pool.Entry.Sequence.class)
                ));
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
                        @Nullable Integer count) implements Pool.Entry {

                @Override
                public NamespaceID type() {
                    return NamespaceID.from("minecraft:item");
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
                       boolean expand) implements Pool.Entry {

                @Override
                public NamespaceID type() {
                    return NamespaceID.from("minecraft:tag");
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
                           String name) implements Pool.Entry {

                @Override
                public NamespaceID type() {
                    return NamespaceID.from("minecraft:dynamic");
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
                         NumberProvider quality) implements Pool.Entry {

                @Override
                public NamespaceID type() {
                    return NamespaceID.from("minecraft:empty");
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
