package net.minestom.vanilla.datapack;

import com.squareup.moshi.JsonReader;
import io.github.pesto.files.ByteArray;
import io.github.pesto.files.FileSystem;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.world.DimensionType;
import net.minestom.vanilla.crafting.VanillaRecipe;
import net.minestom.vanilla.datapack.advancement.Advancement;
import net.minestom.vanilla.datapack.json.JsonUtils;
import net.minestom.vanilla.datapack.loot.LootFunction;
import net.minestom.vanilla.datapack.loot.Predicate;
import net.minestom.vanilla.datapack.number.NumberProvider;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface Datapack {

    Map<NamespaceID, NamespacedData> namespacedData();

    static Datapack loadPrimitiveByteArray(FileSystem<byte[]> source) {
        return loadByteArray(source.map(ByteArray::wrap));
    }

    static Datapack loadInputStream(FileSystem<InputStream> source) {
        return loadPrimitiveByteArray(source.map(stream -> {
            try {
                return stream.readAllBytes();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }));
    }

    static Datapack loadByteArray(FileSystem<ByteArray> source) {
        return new DatapackLoader(source.cache()).load();
    }

    record McMeta(Pack pack, Filter filter) {

        public McMeta() { // Default
            this(new Pack(6, "Minestom Vanilla Datapack"), new Filter(List.of()));
        }

        record Pack(int pack_format, String description) {
        }

        record Filter(List<Pattern> block) {
            record Pattern(String namespace, String path) {
            }
        }
    }

    record NamespacedData(FileSystem<Advancement> advancements,
                          FileSystem<McFunction> functions,
                          FileSystem<LootFunction> item_modifiers,
                          FileSystem<LootTable> loot_tables,
                          FileSystem<Predicate> predicates,
                          FileSystem<VanillaRecipe> recipes,
                          FileSystem<Structure> structures,
                          FileSystem<ChatType> chat_type,
                          FileSystem<DamageType> damage_type,
                          Tags tags,
                          FileSystem<Dimension> dimensions,
                          FileSystem<DimensionType> dimension_type,
                          WorldGen world_gen) {
    }

    record McFunction(String source) {
        public static McFunction fromString(String source) {
            return new McFunction(source);
        }
    }

    record LootTable(@Nullable String type, List<LootFunction> functions, List<Pool> pools) {
        public record Pool(List<Predicate> conditions,
                    List<LootFunction> functions,
                    NumberProvider rolls,
                    NumberProvider bonus_rolls,
                    List<Entry> entries) {

            public sealed interface Entry {
                List<Predicate> conditions();

                NamespaceID type();

                static Entry fromJson(JsonReader reader) throws IOException {
                    return JsonUtils.unionMapType(reader, "type", JsonReader::nextString, Map.of(
                        "minecraft:item", DatapackLoader.moshi(Item.class),
                        "minecraft:tag", DatapackLoader.moshi(Tag.class),
                        "minecraft:loot_table", DatapackLoader.moshi(LootTableNested.class),
                        "minecraft:dynamic", DatapackLoader.moshi(Dynamic.class),
                        "minecraft:empty", DatapackLoader.moshi(Empty.class),
                        "minecraft:group", DatapackLoader.moshi(Group.class),
                        "minecraft:alternatives", DatapackLoader.moshi(Alternatives.class),
                        "minecraft:sequence", DatapackLoader.moshi(Sequence.class)
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
                            @Nullable Integer count) implements Entry {

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
                           boolean expand) implements Entry {

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
                                       NamespaceID name) implements Entry {

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
                               String name) implements Entry {

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
                             NumberProvider quality) implements Entry {

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
                             List<Entry> children) implements Entry {

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
                                    List<Entry> children) implements Entry {

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
                                List<Entry> children) implements Entry {

                    @Override
                    public NamespaceID type() {
                        return NamespaceID.from("minecraft:sequence");
                    }
                }
            }
        }
    }

    record Structure() {
        public static Structure fromInput(ByteArray content) {
            return null;
        }

    }

    record ChatType() {

    }

    record DamageType() {

    }

    record Tags(@Nullable Boolean replace, List<TagValue> values) {

        public static Tags from(FileSystem<String> tags) {
            return null;
        }

        sealed interface ReferenceTag extends TagValue {
        }

        public sealed interface TagValue {
            record ObjectReference(NamespaceID tag) implements ReferenceTag {
            }

            record TagReference(NamespaceID tag) implements ReferenceTag {
            }

            record TagEntry(ReferenceTag id, @Nullable Boolean required) implements TagValue {
            }
        }

    }

    record Dimension() {
    }

    record WorldGen() {
        public static WorldGen from(FileSystem<String> worldgen) {
            return null;
        }
    }
}
