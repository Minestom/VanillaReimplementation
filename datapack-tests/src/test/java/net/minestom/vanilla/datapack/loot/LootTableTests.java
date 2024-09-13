package net.minestom.vanilla.datapack.loot;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.JsonOps;
import io.github.pesto.MojangDataFeature;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.TagStringIO;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minestom.server.MinecraftServer;
import net.minestom.server.item.ItemStack;
import net.minestom.vanilla.VanillaReimplementation;
import net.minestom.vanilla.blocks.VanillaBlockLoot;
import net.minestom.vanilla.datapack.Datapack;
import net.minestom.vanilla.datapack.DatapackLoader;
import net.minestom.vanilla.datapack.DatapackLoadingFeature;
import net.minestom.vanilla.datapack.loot.context.LootContext;
import net.minestom.vanilla.files.ByteArray;
import net.minestom.vanilla.files.FileSystem;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class LootTableTests {

    private static VanillaReimplementation vri;
    private static Datapack datapack;
    private static FileSystem<LootTable> loot_tables;
    private static FileSystem<String> loot_table_files;

    @BeforeAll
    public static void init() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();

        MinecraftServer.init();
        vri = VanillaReimplementation.hook(MinecraftServer.process());

        FileSystem<ByteArray> data = vri.feature(MojangDataFeature.class).latestAssets();
        FileSystem<String> fs = data.map(byteArray -> byteArray.toCharacterString());
        fs = fs.folder("minecraft", "loot_tables");

        DatapackLoadingFeature feature = vri.feature(DatapackLoadingFeature.class);
        datapack = feature.current();
        loot_tables = datapack.namespacedData().get("minecraft").loot_tables();
        loot_table_files = fs;
    }

//    @Test
//    public void testMinestomBlocks() {
//        FileSystem<String> blocks = loot_table_files.folder("blocks");
//
//        for (String file : blocks.files().stream().sorted().toList()) {
//            String src = blocks.file(file);
//            Table table = table(src);
//            String filename = file.substring(0, file.length() - ".json".length());
//            List<ItemStack> expected = LootTableTestData.EXPECTED_RESULTS.get(filename);
//
//            List<Item> minestom;
//            try {
//                Map<LootContext.Trait<?>, Object> traits = LootTableTestData.TRAITS.getOrDefault(filename, Map.of());
//                minestom = table.minestomLoot(traits);
//            } catch (Exception e) {
//                if (e instanceof IllegalStateException && e.getMessage().contains("LootContext does not have trait ")) {
//                    String trait = e.getMessage().substring("LootContext does not have trait ".length());
//                    System.out.println("Skipping test for " + filename + " because trait " + trait + " is missing");
//                    continue;
//                }
//                throw new RuntimeException("Failed to run LootTable: " + file, e);
//            }
//
//            if (expected == null) {
//                String testDataValue = "List.of(" + minestom.stream()
//                        .map(Item::minestom)
//                        .map(stack -> "ItemStack.of(Material." + stack.material().namespace().value().toUpperCase(Locale.ROOT) + ", " + stack.amount() + ")")
//                        .collect(Collectors.joining(", ")) + ")";
//                String testDataLine = "expected.put(\"" + filename + "\", " + testDataValue + ");";
//                System.out.println(testDataLine);
//                continue;
//            }
//            assertItems(filename, minestom, expected);
//        }
//    }

    private void assertItems(String label, List<Item> item, ItemStack... expected) {
        assertItems(label, item, List.of(expected));
    }

    private void assertItems(String label, List<Item> item, List<ItemStack> expected) {
        List<ItemStack> items = item.stream().map(Item::minestom).toList();
        assertEquals(expected.size(), items.size(), () -> label + " size mismatch");
        for (int i = 0; i < items.size(); i++) {
            ItemStack actual = items.get(i);
            ItemStack expectedItem = expected.get(i);
            int finalI = i;
            assertEquals(expectedItem.material(), actual.material(), () -> label + " material mismatch at index " + finalI);
            assertEquals(expectedItem.amount(), actual.amount(), () -> label + " amount mismatch at index " + finalI);

            // TODO: nbt comparison
            // this is non-trivial as '{0=>CustomData[nbt=BinaryTagType[CompoundBinaryTag 10]{tags={"tag"=BinaryTagType[CompoundBinaryTag 10]{tags={}}}}]}' must equal '{}' (empty compound tag)
//            assertEquals(expectedItem.nbt(), actual.nbt());
        }
    }

    record Table(
            net.minecraft.world.level.storage.loot.LootTable vanilla,
            LootTable minestom
    ) {
        @SuppressWarnings("DataFlowIssue")
        public List<Item> vanillaLoot() {
            // we somehow need to acquire a "ServerLevel" instance. Hopefully we don't need to actually run a vanilla server.
            // check out net.minecraft.server.Main if we need to run a server
            ServerLevel level = new ServerLevel(null, null, null, null, null, null, null, false, 0, List.of(), false, null);
            LootParams params = new LootParams(level, Map.of(), Map.of(), 0.0f);
            var items = vanilla.getRandomItems(params);
            return items.stream().map(LootTableTests::item).toList();
        }

        public List<Item> minestomLoot(Map<LootContext.Trait<?>, Object> traits) {
            LootContext context = new LootContext() {
                @Override
                public <T> @Nullable T get(Trait<T> trait) {
                    //noinspection unchecked
                    return (T) traits.get(trait);
                }
            };
            VanillaBlockLoot loot = new VanillaBlockLoot(vri, datapack);

            var items = loot.getLoot(minestom, context);
            return items.stream().map(LootTableTests::item).toList();
        }
    }

    private Table table(String src) {
        Gson gson = new Gson();
        JsonElement json = gson.fromJson(src, JsonElement.class);
        net.minecraft.world.level.storage.loot.LootTable vanilla = net.minecraft.world.level.storage.loot.LootTable.CODEC.parse(JsonOps.INSTANCE, json).result().orElseThrow();
        LootTable minestom = DatapackLoader.adaptor(LootTable.class).apply(src);
        return new Table(vanilla, minestom);
    }

    interface Item {
        net.minecraft.world.item.ItemStack vanilla();
        net.minestom.server.item.ItemStack minestom();
    }

    private static Item item(ItemStack minestom) {
        try {
            String snbt = TagStringIO.get().asString(minestom.toItemNBT());
            return new SNBTItem(snbt);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Item item(net.minecraft.world.item.ItemStack vanilla) {
        CompoundTag tag = vanilla.save(new CompoundTag());
        return new SNBTItem(tag.getAsString());
    }
}

record SNBTItem(String item) implements LootTableTests.Item {
    @Override
    public net.minecraft.world.item.ItemStack vanilla() {
        try {
            CompoundTag tag = TagParser.parseTag(item);
            return net.minecraft.world.item.ItemStack.of(tag);
        } catch (CommandSyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public net.minestom.server.item.ItemStack minestom() {
        try {
            CompoundBinaryTag tag = TagStringIO.get().asCompound(item);
            return ItemStack.fromItemNBT(tag);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
