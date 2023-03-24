package net.minestom.vanilla.datapack;

import com.squareup.moshi.Moshi;
import io.github.pesto.files.ByteArray;
import io.github.pesto.files.FileSystem;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.world.DimensionType;
import net.minestom.vanilla.crafting.VanillaRecipe;
import net.minestom.vanilla.datapack.loot.LootFunction;
import net.minestom.vanilla.datapack.loot.Predicate;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static net.minestom.vanilla.datapack.Datapack.*;

public class DatapackLoader {

    private final McMeta mcmeta;
    private final @Nullable ByteArray pack_png;
    private static final Moshi moshi = new Moshi.Builder().build();
    final Map<NamespaceID, NamespacedData> namespace2data;

    public DatapackLoader(FileSystem<ByteArray> source) {
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

        {
            Map<NamespaceID, NamespacedData> namespace2data = new HashMap<>();

            for (String namespace : source.folders()) {
                FileSystem<ByteArray> dataFolder = source.folder(namespace);
                NamespaceID namespaceID = NamespaceID.from(namespace);

                FileSystem<Advancement> advancements = parseJsonFolder(dataFolder, "advancements", recordJson(Advancement.class));
                FileSystem<McFunction> functions = parseJsonFolder(dataFolder, "functions", McFunction::fromString);
                FileSystem<LootFunction> item_modifiers = parseJsonFolder(dataFolder, "item_modifiers", adaptor(LootFunction.class));
                FileSystem<LootTable> loot_tables = parseJsonFolder(dataFolder, "loot_tables", recordJson(LootTable.class));
                FileSystem<Predicate> predicates = parseJsonFolder(dataFolder, "predicates", adaptor(Predicate.class));
                FileSystem<VanillaRecipe> recipes = parseJsonFolder(dataFolder, "recipes", adaptor(VanillaRecipe.class));
                FileSystem<Structure> structures = dataFolder.folder("structures").map(Structure::fromInput);
                FileSystem<ChatType> chat_type = parseJsonFolder(dataFolder, "chat_type", recordJson(ChatType.class));
                FileSystem<DamageType> damage_type = parseJsonFolder(dataFolder, "damage_type", recordJson(DamageType.class));
                Tags tags = Tags.from(source.folder("tags", FileSystem.BYTES_TO_STRING));
                FileSystem<Dimension> dimensions = parseJsonFolder(dataFolder, "dimension", recordJson(Dimension.class));
                FileSystem<DimensionType> dimension_type = parseJsonFolder(dataFolder, "dimension_type", adaptor(DimensionType.class));
                WorldGen world_gen = WorldGen.from(source.folder("worldgen", FileSystem.BYTES_TO_STRING));

                NamespacedData data = new NamespacedData(advancements, functions, item_modifiers, loot_tables, predicates, recipes, structures, chat_type, damage_type, tags, dimensions, dimension_type, world_gen);
                namespace2data.put(namespaceID, data);
            }

            this.namespace2data = Map.copyOf(namespace2data);
        }
    }

    private static <T> FileSystem<T> parseJsonFolder(FileSystem<ByteArray> source, String path, Function<String, T> converter) {
        return source.hasFolder(path) ? source.folder(path, FileSystem.BYTES_TO_STRING).map(converter) : FileSystem.empty();
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

    private static <T extends Record> Function<String, T> recordJson(Class<T> clazz) {
        return str -> {
            try {
                return moshi.adapter(clazz).fromJson(str);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public Datapack load() {
        return null;
    }
}
