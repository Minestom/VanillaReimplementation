package net.minestom.plugins;

import net.minestom.server.command.CommandManager;
import net.minestom.server.gamedata.loottables.LootTableManager;
import net.minestom.vanilla.commands.VanillaCommands;
import net.minestom.vanilla.gamedata.loottables.VanillaLootTables;

public class PluginLootTables {

	public static void register(LootTableManager lootTableManager) {
		VanillaLootTables.register(lootTableManager);
	}
}