package net.minestom.vanilla.datapack;

import com.google.gson.JsonElement;
import net.kyori.adventure.text.Component;
import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.datapack.files.FileSystem;
import net.minestom.vanilla.datapack.files.FileSystemUtil;
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
    private final byte[] pack_png;
    final Map<NamespaceID, NamespacedData> namespace2data;

    public DatapackLoader(FileSystem<InputStream> source) {
        this.mcmeta = FileSystemUtil.toString(source).file("pack.mcmeta");
        this.pack_png = FileSystemUtil.toBytes(source).file("pack.png");

        FileSystem<InputStream> dataFolder = source.folder("data");
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
        public NamespacedData(FileSystem<InputStream> source) {
            this(FileSystemUtil.toJson(source.folder("advancements")).map(AdvancementData::fromJson),
                    FileSystemUtil.toJson(source.folder("loot_tables")).map(LootTableData::fromJson),
                    FileSystemUtil.toJson(source.folder("recipes")).map(RecipeData::fromJson),
                    source.folder("structures").map(StructureData::fromInput),
                    FileSystemUtil.toJson(source.folder("tags")).map(Tag::fromJson));
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
        public static StructureData fromInput(InputStream content) {
            return null;
        }

    }
    record Tag() {
        public static Tag fromJson(JsonElement content) {
            return null;
        }

    }
}
