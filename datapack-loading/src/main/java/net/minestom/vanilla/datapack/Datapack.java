package net.minestom.vanilla.datapack;

import com.squareup.moshi.JsonReader;
import net.minestom.vanilla.datapack.dimension.DimensionType;
import net.minestom.vanilla.datapack.json.JsonUtils;
import net.minestom.vanilla.datapack.json.Optional;
import net.minestom.vanilla.datapack.trims.TrimMaterial;
import net.minestom.vanilla.datapack.trims.TrimPattern;
import net.minestom.vanilla.datapack.worldgen.*;
import net.minestom.vanilla.datapack.worldgen.noise.Noise;
import net.minestom.vanilla.files.ByteArray;
import net.minestom.vanilla.files.FileSystem;
import net.kyori.adventure.key.Key;
import net.minestom.vanilla.datapack.advancement.Advancement;
import net.minestom.vanilla.datapack.loot.LootTable;
import net.minestom.vanilla.datapack.loot.function.LootFunction;
import net.minestom.vanilla.datapack.loot.function.Predicate;
import net.minestom.vanilla.datapack.recipe.Recipe;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public interface Datapack {

    Map<String, NamespacedData> namespacedData();

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
        return new DatapackLoader().load(source.cache());
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
                          FileSystem<Datapack.Tag> tags,
                          FileSystem<Dimension> dimensions,
                          FileSystem<DimensionType> dimension_type,
                          FileSystem<TrimPattern> trim_pattern,
                          FileSystem<TrimMaterial> trim_material,
                          WorldGen world_gen) {

        /**
         * Performs a deep cache on all of the data.
         * This helps us load all density functions while in the loading context.
         */
        NamespacedData cache() {
            return new NamespacedData(
                    advancements.cache(),
                    functions.cache(),
                    item_modifiers.cache(),
                    loot_tables.cache(),
                    predicates.cache(),
                    recipes.cache(),
                    structures.lazy(), // structures may be large, so we don't want to cache them immediately
                    chat_type.cache(),
                    damage_type.cache(),
                    tags.cache(),
                    dimensions.cache(),
                    dimension_type.cache(),
                    trim_pattern.cache(),
                    trim_material.cache(),
                    world_gen.cache()
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

    record Tag(@Nullable Boolean replace, List<TagValue> values) {

        public Tag {
            values = List.copyOf(values);
        }

        public sealed interface TagValue {

            static TagValue fromJson(JsonReader reader) throws IOException {
                return JsonUtils.typeMap(reader, token -> switch (token) {
                    case STRING -> DatapackLoader.moshi(ObjectOrTagReference.class);
                    case BEGIN_OBJECT -> DatapackLoader.moshi(TagEntry.class);
                    default -> null;
                });
            }

            record ObjectOrTagReference(Key tag) implements TagValue {
                public static ObjectOrTagReference fromJson(JsonReader reader) throws IOException {
                    return JsonUtils.typeMapMapped(reader, Map.of(
                            JsonReader.Token.STRING, json -> new ObjectOrTagReference(DatapackLoader.jsonAdaptor(Key.class).fromJson(json))
                    ));
                }
            }

            /**
             * @param required defaults to true
             */
            record TagEntry(ObjectOrTagReference id, @Optional Boolean required) implements TagValue {
            }
        }
    }

    record Dimension(Key type) {
    }

    record WorldGen(
            FileSystem<Biome> biome,
            FileSystem<Carver> configured_carver,
            FileSystem<ByteArray> configured_feature,
            FileSystem<DensityFunction> density_function,
            FileSystem<ByteArray> flat_level_generator_preset,
            FileSystem<ByteArray> multi_noise_biome_source_parameter_list,
            FileSystem<Noise> noise,
            FileSystem<NoiseSettings> noise_settings,
            FileSystem<ByteArray> placed_feature,
            FileSystem<ByteArray> processor_list,
            FileSystem<ByteArray> structure,
            FileSystem<ByteArray> structure_set,
            FileSystem<ByteArray> template_pool,
            FileSystem<ByteArray> world_preset
            ) {
        public static WorldGen from(FileSystem<ByteArray> worldgen) {
            return new WorldGen(
                    DatapackLoader.parseJsonFolder(worldgen, "biome", DatapackLoader.adaptor(Biome.class)),
                    DatapackLoader.parseJsonFolder(worldgen, "configured_carver", DatapackLoader.adaptor(Carver.class)),
                    worldgen.folder("configured_feature"),
                    DatapackLoader.parseJsonFolder(worldgen, "density_function", DatapackLoader.adaptor(DensityFunction.class)),
                    worldgen.folder("flat_level_generator_preset"),
                    worldgen.folder("multi_noise_biome_source_parameter_list"),
                    DatapackLoader.parseJsonFolder(worldgen, "noise", DatapackLoader.adaptor(Noise.class)),
                    DatapackLoader.parseJsonFolder(worldgen, "noise_settings", DatapackLoader.adaptor(NoiseSettings.class)),
                    worldgen.folder("placed_feature"),
                    worldgen.folder("processor_list"),
                    worldgen.folder("structure"),
                    worldgen.folder("structure_set"),
                    worldgen.folder("template_pool"),
                    worldgen.folder("world_preset")
            );
        }

        public WorldGen cache() {
            return new WorldGen(
                    biome.cache(),
                    configured_carver.cache(),
                    configured_feature.cache(),
                    density_function.cache(),
                    flat_level_generator_preset.cache(),
                    multi_noise_biome_source_parameter_list.cache(),
                    noise.cache(),
                    noise_settings.cache(),
                    placed_feature.cache(),
                    processor_list.cache(),
                    structure.cache(),
                    structure_set.cache(),
                    template_pool.cache(),
                    world_preset.cache()
            );
        }
    }
}
