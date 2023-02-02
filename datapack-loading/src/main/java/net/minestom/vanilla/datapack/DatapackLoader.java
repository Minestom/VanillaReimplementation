package net.minestom.vanilla.datapack;

import com.google.gson.JsonElement;
import io.github.pesto.files.ByteArray;
import io.github.pesto.files.FileSystem;
import net.kyori.adventure.text.Component;
import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.datapack.loot.ItemFunction;
import net.minestom.vanilla.datapack.loot.ItemPredicate;
import net.minestom.vanilla.datapack.number.NumberProvider;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatapackLoader {

    private final String mcmeta;
    private final ByteArray pack_png;
    final Map<NamespaceID, NamespacedData> namespace2data;

    public DatapackLoader(FileSystem<ByteArray> source) {
        if (!source.hasFile("pack.mcmeta")) throw new IllegalArgumentException("pack.mcmeta is missing from " + source);
        if (!source.hasFile("pack.png")) throw new IllegalArgumentException("pack.png is missing from " + source);

        this.mcmeta = source.map(ByteArray::toCharacterString).file("pack.mcmeta");
        this.pack_png = source.file("pack.png");

        FileSystem<ByteArray> dataFolder = source.folder("data");
        {
            Map<NamespaceID, NamespacedData> namespace2data = new HashMap<>();
            for (String namespace : dataFolder.folders()) {
                NamespaceID namespaceID = NamespaceID.from(namespace);
                NamespacedData data = new NamespacedData(dataFolder.folder(namespace));
                namespace2data.put(namespaceID, data);
            }
            this.namespace2data = Map.copyOf(namespace2data);
        }
    }

    record NamespacedData(FileSystem<AdvancementData> advancements,
                          FileSystem<LootTableData> loot_tables,
                          FileSystem<RecipeData> recipes,
                          FileSystem<StructureData> structures,
                          FileSystem<Tag> tags) {
        public NamespacedData(FileSystem<ByteArray> source) {
            this(source.folder("advancements").map(FileSystem.BYTES_TO_JSON).map(AdvancementData::fromJson),
                    source.folder("loot_tables").map(FileSystem.BYTES_TO_JSON).map(LootTableData::fromJson),
                    source.folder("recipes").map(FileSystem.BYTES_TO_JSON).map(RecipeData::fromJson),
                    source.folder("structures").map(StructureData::fromInput),
                    source.folder("tags").map(FileSystem.BYTES_TO_JSON).map(Tag::fromJson));
        }
    }

    public Datapack load() {
        return null;
    }

    record AdvancementData(Display display,
                           String parent,
                           Criteria criteria,
                           List<List<String>> requirements,
                           Map<String, Reward> rewards) {


        record Display(Icon icon, Component title, @Nullable String frame, String background,
                       Component description, @Nullable Boolean showToast, @Nullable Boolean announceToChat, @Nullable Boolean hidden) {
            record Icon(NamespaceID item, String snbt) {
            }
        }

        // TODO: Conditions
        record Criteria(String trigger, Map<String, Condition<?>> conditions) {
            interface Condition<P> {
                P player();
                Map<String, Object> extraContents();
            }
        }

        record Reward(List<String> recipes, List<String> loot, int experience, String function) {
        }

        public static AdvancementData fromJson(JsonElement content) {
            return null;
        }
    }
    record LootTableData(@Nullable String type, List<ItemFunction> functions, List<Pool> pools) {
        public static LootTableData fromJson(JsonElement content) {
            return null;
        }

        record Pool(List<ItemPredicate> conditions,
                    List<ItemFunction> functions,
                    NumberProvider.Int rolls,
                    NumberProvider.Double bonus_rolls,
                    List<Entry> entries) {

            record Entry(List<ItemPredicate> conditions, String type) {
            }
        }

    }
    record RecipeData() {
        public static RecipeData fromJson(JsonElement content) {
            return null;
        }

    }
    record StructureData() {
        public static StructureData fromInput(ByteArray content) {
            return null;
        }

    }
    record Tag() {
        public static Tag fromJson(JsonElement content) {
            return null;
        }

    }
}
