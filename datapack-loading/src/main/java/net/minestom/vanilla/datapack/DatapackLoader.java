package net.minestom.vanilla.datapack;

import com.squareup.moshi.*;
import net.minestom.vanilla.datapack.worldgen.random.WorldgenRandom;
import net.minestom.vanilla.files.ByteArray;
import net.minestom.vanilla.files.FileSystem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Enchantment;
import net.minestom.server.item.Material;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.world.DimensionType;
import net.minestom.vanilla.datapack.advancement.Advancement;
import net.minestom.vanilla.datapack.json.JsonUtils;
import net.minestom.vanilla.datapack.loot.LootTable;
import net.minestom.vanilla.datapack.loot.context.LootContext;
import net.minestom.vanilla.datapack.loot.function.LootFunction;
import net.minestom.vanilla.datapack.loot.function.Predicate;
import net.minestom.vanilla.datapack.number.NumberProvider;
import net.minestom.vanilla.datapack.recipe.Recipe;
import net.minestom.vanilla.datapack.worldgen.Structure;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTException;
import org.jglrxavpok.hephaistos.parser.SNBTParser;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static net.minestom.vanilla.datapack.Datapack.*;

public class DatapackLoader {

    private final McMeta mcmeta;
    private final @Nullable ByteArray pack_png;
    private static final Moshi moshi = createMoshiWithAdaptors();
    final Map<NamespaceID, NamespacedData> namespace2data;

    DatapackLoader(FileSystem<ByteArray> source) {
        if (source.hasFile("pack.mcmeta")) {
            this.mcmeta = source.map(FileSystem.BYTES_TO_STRING).map(recordJson(McMeta.class)).file("pack.mcmeta");
        } else {
            // Default
            this.mcmeta = new McMeta();
        }
        if (source.hasFile("pack.png")) {
            this.pack_png = source.file("pack.png");
        } else {
            this.pack_png = null;
        }

        // Refresh the loadingRandom, so that datapack loads are deterministic
        DatapackLoader.loadingRandom = WorldgenRandom.standard(0);

        {
            Map<NamespaceID, NamespacedData> namespace2data = new HashMap<>();

            for (String namespace : source.folders()) {
                FileSystem<ByteArray> dataFolder = source.folder(namespace).inMemory();
                NamespaceID namespaceID = NamespaceID.from(namespace);

                FileSystem<Advancement> advancements = parseJsonFolder(dataFolder, "advancements", recordJson(Advancement.class));
                FileSystem<McFunction> functions = parseJsonFolder(dataFolder, "functions", McFunction::fromString);
                FileSystem<LootFunction> item_modifiers = parseJsonFolder(dataFolder, "item_modifiers", adaptor(LootFunction.class));
                FileSystem<LootTable> loot_tables = parseJsonFolder(dataFolder, "loot_tables", recordJson(LootTable.class));
                FileSystem<Predicate> predicates = parseJsonFolder(dataFolder, "predicates", adaptor(Predicate.class));
                FileSystem<Recipe> recipes = parseJsonFolder(dataFolder, "recipes", adaptor(Recipe.class));
                FileSystem<Structure> structures = dataFolder.folder("structures").map(Structure::fromInput);
                FileSystem<ChatType> chat_type = parseJsonFolder(dataFolder, "chat_type", recordJson(ChatType.class));
                FileSystem<DamageType> damage_type = parseJsonFolder(dataFolder, "damage_type", recordJson(DamageType.class));
                Tags tags = Tags.from(source.folder("tags", FileSystem.BYTES_TO_STRING));
                FileSystem<Dimension> dimensions = parseJsonFolder(dataFolder, "dimension", recordJson(Dimension.class));
                FileSystem<DimensionType> dimension_type = parseJsonFolder(dataFolder, "dimension_type", adaptor(DimensionType.class));
                Datapack.WorldGen world_gen = Datapack.WorldGen.from(source.folder("worldgen"));

                NamespacedData data = new NamespacedData(advancements, functions, item_modifiers, loot_tables, predicates, recipes, structures, chat_type, damage_type, tags, dimensions, dimension_type, world_gen);
                namespace2data.put(namespaceID, data);
            }

            this.namespace2data = Map.copyOf(namespace2data);
        }
    }

    private static Moshi createMoshiWithAdaptors() {
        Moshi.Builder builder = new Moshi.Builder();

        // Native
        register(builder, UUID.class, DatapackLoader::uuidFromJson);

        // json utils
        builder.add((type, annotations, moshi) -> {
            if (typeDoesntMatch(type, JsonUtils.SingleOrList.class)) return null;
            Type elementType = Types.collectionElementType(type, Collection.class);
            return new JsonAdapter<JsonUtils.SingleOrList<?>>() {
                @Override
                public JsonUtils.SingleOrList<?> fromJson(JsonReader reader) throws IOException {
                    return JsonUtils.SingleOrList.fromJson(elementType, reader);
                }

                @Override
                public void toJson(JsonWriter writer, JsonUtils.SingleOrList<?> value) throws IOException {
                }
            };
        });

        // Minestom
        register(builder, NBTCompound.class, DatapackLoader::nbtCompoundFromJson);
        register(builder, Block.class, DatapackLoader::blockFromJson);
        register(builder, Enchantment.class, DatapackLoader::enchantmentFromJson);
        register(builder, EntityType.class, DatapackLoader::entityTypeFromJson);
        register(builder, Material.class, DatapackLoader::materialFromJson);
        register(builder, Component.class, reader -> {
            GsonComponentSerializer serializer = GsonComponentSerializer.gson();
            return serializer.deserialize(reader.nextSource().readUtf8());
        });
        register(builder, NamespaceID.class, reader -> NamespaceID.from(reader.nextString()));

        // TODO: Implement all of these readers
        register(builder, Advancement.Trigger.class, Advancement.Trigger::fromJson);
        register(builder, LootContext.Trait.class, LootContext.Trait::fromJson);
        register(builder, LootFunction.class, LootFunction::fromJson);
        register(builder, Predicate.class, Predicate::fromJson);
        register(builder, Predicate.BlockStateProperty.Property.class, Predicate.BlockStateProperty.Property::fromJson);
        register(builder, Predicate.EntityScores.Score.class, Predicate.EntityScores.Score::fromJson);
        register(builder, Predicate.TimeCheck.Value.class, Predicate.TimeCheck.Value::fromJson);
        register(builder, Predicate.ValueCheck.Range.class, Predicate.ValueCheck.Range::fromJson);
        register(builder, NumberProvider.class, NumberProvider.Double::fromJson);
        register(builder, NumberProvider.Double.class, NumberProvider.Double::fromJson);
        register(builder, NumberProvider.Int.class, NumberProvider.Int::fromJson);
        register(builder, LootTable.Pool.Entry.class, LootTable.Pool.Entry::fromJson);
        register(builder, LootFunction.ApplyBonus.class, LootFunction.ApplyBonus::fromJson);
        register(builder, LootFunction.CopyNbt.Source.class, LootFunction.CopyNbt.Source::fromJson);
        register(builder, LootFunction.CopyNbt.Operation.class, LootFunction.CopyNbt.Operation::fromJson);
        register(builder, LootFunction.LimitCount.Limit.class, LootFunction.LimitCount.Limit::fromJson);
        register(builder, Recipe.class, Recipe::fromJson);
        register(builder, Recipe.Ingredient.class, Recipe.Ingredient::fromJson);
        register(builder, Recipe.Ingredient.Single.class, Recipe.Ingredient.Single::fromJson);

        return builder.build();
    }

    static <T> FileSystem<T> parseJsonFolder(FileSystem<ByteArray> source, String path, Function<String, T> converter) {
        return source.hasFolder(path) ?
                source.folder(path, FileSystem.BYTES_TO_STRING).map(converter) :
                FileSystem.empty();
    }

    private static <T> Function<String, T> adaptor(Class<T> clazz) {
        return str -> {
            try {
                return moshi.adapter(clazz).fromJson(str);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    static <T extends Record> Function<String, T> recordJson(Class<T> clazz) {
        return str -> {
            try {
                return moshi.adapter(clazz).fromJson(str);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    private static WorldgenRandom loadingRandom = WorldgenRandom.standard(0);

    public static WorldgenRandom loadingRandom() {
        return loadingRandom;
    }

    public Datapack load() {
        var copy = namespace2data.entrySet().stream()
                .map(entry -> Map.entry(entry.getKey(), entry.getValue().cache()))
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
        return new Datapack() {
            @Override
            public Map<NamespaceID, NamespacedData> namespacedData() {
                return copy;
            }

            @Override
            public String toString() {
                return "Datapack{" +
                        "namespace2data=" + copy +
                        '}';
            }
        };
    }

    private static <T> void register(Moshi.Builder builder, Class<T> clazz, JsonAdapter<T> adapter) {
        builder.add((type, annotations, moshi) -> {
            if (typeDoesntMatch(type, clazz)) return null;
            return adapter;
        });
    }

    private static <T> void register(Moshi.Builder builder, Class<T> clazz, IoFunction<JsonReader, T> reader) {
        register(builder, clazz, new JsonAdapter<>() {
            @Override
            public T fromJson(JsonReader jsonReader) throws IOException {
                return reader.apply(jsonReader);
            }

            @Override
            public void toJson(JsonWriter writer, T value) throws IOException {
            }
        });
    }

    public interface IoFunction<T, R> {
        R apply(T t) throws IOException;
    }

    public static Moshi moshi() {
        return moshi;
    }

    public static <T> JsonUtils.IoFunction<JsonReader, T> moshi(Class<? extends T> clazz) {
        return moshi().adapter(clazz)::fromJson;
    }

    public static <T> JsonUtils.IoFunction<JsonReader, T> moshi(Type type) {
        JsonUtils.IoFunction<JsonReader, Object> function = moshi().adapter(type)::fromJson;
        //noinspection unchecked
        return (JsonUtils.IoFunction<JsonReader, T>) function;
    }

    private static NBTCompound nbtCompoundFromJson(JsonReader reader) throws IOException {
        String json = reader.nextSource().readUtf8();
        SNBTParser parser = new SNBTParser(new StringReader(json));
        try {
            return (NBTCompound) parser.parse();
        } catch (NBTException e) {
            throw new RuntimeException(e);
        }
    }

    private static NamespaceID namespaceFromJson(JsonReader reader) throws IOException {
        return NamespaceID.from(reader.nextString());
    }

    private static UUID uuidFromJson(JsonReader reader) throws IOException {
        return null;
    }

    private static Block blockFromJson(JsonReader reader) throws IOException {
        return JsonUtils.typeMap(reader, Map.of(
                JsonReader.Token.STRING, json -> Block.fromNamespaceId(json.nextString())
                // TODO: Add support for block states?
        ));
    }

    private static Enchantment enchantmentFromJson(JsonReader reader) throws IOException {
        return Enchantment.fromNamespaceId(Objects.requireNonNull(namespaceFromJson(reader)));
    }

    private static EntityType entityTypeFromJson(JsonReader reader) throws IOException {
        return EntityType.fromNamespaceId(Objects.requireNonNull(namespaceFromJson(reader)));
    }

    private static Material materialFromJson(JsonReader reader) throws IOException {
        return Material.fromNamespaceId(Objects.requireNonNull(namespaceFromJson(reader)));
    }

    private static final Pattern GENERICS = Pattern.compile("<.*>");

    private static boolean typeDoesntMatch(Type type, Class<?> clazz) {
        String typeName = GENERICS.matcher(type.getTypeName()).replaceAll("");
        String clazzName = GENERICS.matcher(clazz.getTypeName()).replaceAll("");
        return !typeName.equals(clazzName);
    }

}
