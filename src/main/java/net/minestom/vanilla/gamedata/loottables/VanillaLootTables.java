package net.minestom.vanilla.gamedata.loottables;

import net.minestom.server.gamedata.conditions.SurvivesExplosionCondition;
import net.minestom.server.gamedata.loottables.LootTableManager;
import net.minestom.server.gamedata.loottables.entries.*;
import net.minestom.server.gamedata.loottables.tabletypes.BlockType;
import net.minestom.server.utils.NamespaceID;

public final class VanillaLootTables {

    public static void register(LootTableManager tableManager) {
        tableManager.registerCondition(NamespaceID.from("minecraft:survives_explosion"), new SurvivesExplosionCondition());
        tableManager.registerTableType(NamespaceID.from("minecraft:block"), new BlockType());

        tableManager.registerEntryType(NamespaceID.from("minecraft:alternatives"), new AlternativesType());
        tableManager.registerEntryType(NamespaceID.from("minecraft:loot_table"), new AnotherLootTableType());
        tableManager.registerEntryType(NamespaceID.from("minecraft:dynamic"), new DynamicType());
        tableManager.registerEntryType(NamespaceID.from("minecraft:item"), new ItemType());
        tableManager.registerEntryType(NamespaceID.from("minecraft:group"), new GroupType());
        tableManager.registerEntryType(NamespaceID.from("minecraft:sequence"), new SequenceType());
        tableManager.registerEntryType(NamespaceID.from("minecraft:tag"), new TagType());
    }
}
