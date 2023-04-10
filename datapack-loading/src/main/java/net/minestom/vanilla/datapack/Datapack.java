package net.minestom.vanilla.datapack;

import io.github.pesto.files.ByteArray;
import io.github.pesto.files.FileSystem;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.math.FloatRange;
import net.minestom.server.world.DimensionType;
import net.minestom.vanilla.datapack.advancement.Advancement;
import net.minestom.vanilla.datapack.json.JsonUtils;
import net.minestom.vanilla.datapack.loot.LootTable;
import net.minestom.vanilla.datapack.loot.function.LootFunction;
import net.minestom.vanilla.datapack.loot.function.Predicate;
import net.minestom.vanilla.datapack.recipe.Recipe;
import net.minestom.vanilla.datapack.worldgen.BlockState;
import net.minestom.vanilla.datapack.worldgen.Structure;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

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
                          FileSystem<Recipe> recipes,
                          FileSystem<Structure> structures,
                          FileSystem<ChatType> chat_type,
                          FileSystem<DamageType> damage_type,
                          Tags tags,
                          FileSystem<Dimension> dimensions,
                          FileSystem<DimensionType> dimension_type,
                          WorldGen world_gen) {

        NamespacedData cache() {
            return new NamespacedData(
                    advancements.cache(),
                    functions.cache(),
                    item_modifiers.cache(),
                    loot_tables.cache(),
                    predicates.cache(),
                    recipes.cache(),
                    structures.cache(),
                    chat_type.cache(),
                    damage_type.cache(),
                    tags,
                    dimensions.cache(),
                    dimension_type.cache(),
                    world_gen
            );
        }
    }

    record McFunction(String source) {
        public static McFunction fromString(String source) {
            return new McFunction(source);
        }
    }

    record ChatType() {
        // Undocumented? Couldn't find any info on this
    }

    record DamageType(String message_id, float exhaustion, String scaling, @Nullable String effects, @Nullable String death_message_type) {
    }

    record Tags(@Nullable Boolean replace, List<TagValue> values) {

        public static Tags from(FileSystem<String> tags) {
            return new Tags(null, List.of());
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

    record Dimension(NamespaceID type) {

    }

    record WorldGen() {
        public static WorldGen from(FileSystem<String> worldgen) {
            return new WorldGen();
        }
    }
}
