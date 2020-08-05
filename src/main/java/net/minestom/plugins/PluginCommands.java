package net.minestom.plugins;

import net.minestom.server.command.CommandManager;
import net.minestom.vanilla.commands.VanillaCommands;

public class PluginCommands {

	public static void registerAll(CommandManager commandManager) {
		VanillaCommands.registerAll(commandManager);
	}
}