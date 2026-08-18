package net.minestom.vanilla.commands;

import net.minestom.server.command.CommandManager;
import net.minestom.vanilla.commands.admin.OpCommand;
import net.minestom.vanilla.commands.all.HelpCommand;
import net.minestom.vanilla.commands.all.ListCommand;
import net.minestom.vanilla.commands.all.MeCommand;
import net.minestom.vanilla.commands.all.MsgCommand;
import net.minestom.vanilla.commands.gamemaster.DifficultyCommand;
import net.minestom.vanilla.commands.gamemaster.ForceloadCommand;
import net.minestom.vanilla.commands.gamemaster.GamemodeCommand;
import net.minestom.vanilla.commands.gamemaster.SayCommand;
import net.minestom.vanilla.commands.owner.SaveAllCommand;
import net.minestom.vanilla.commands.owner.StopCommand;
import net.minestom.vanilla.logging.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles the registration and management of all built-in (vanilla) commands.
 * Allows for easy bulk or custom command registration into Minestom's {@link CommandManager}.
 */
public final class VanillaCommandsFeature {

    /**
     * A map of all registered vanilla commands by name.
     */
    private static final Map<String, VanillaCommand> registeredCommands = new HashMap<>();

    /**
     * List of all default vanilla command instances that should be registered by default.
     */
    private static final List<VanillaCommand> VANILLA_COMMANDS = List.of(
            new HelpCommand(),
            new ListCommand(),
            new MeCommand(),
            new DifficultyCommand(),
            new ForceloadCommand(),
            new GamemodeCommand(),
            new SayCommand(),
            new SaveAllCommand(),
            new StopCommand(),
            new OpCommand(),
            new MsgCommand()
    );

    /**
     * Registers a single vanilla command to the command manager and stores it for reference.
     * Logs if the command is not part of the default vanilla command list.
     *
     * @param manager the command manager to register with
     * @param command the command to register
     */
    public static void registerCommand(CommandManager manager, VanillaCommand command) {
        manager.register(command);
        registeredCommands.put(command.getName(), command);

        if (!getVanillaCommands().contains(command)) {
            Logger.info("Registered custom vanilla command " + command.getName());
        }
    }

    /**
     * Registers all default vanilla commands to the given command manager.
     *
     * @param manager the command manager to register all commands to
     */
    public static void registerAll(CommandManager manager) {
        VANILLA_COMMANDS.forEach(command -> registerCommand(manager, command));
        Logger.info("Registered " + VANILLA_COMMANDS.size() + " vanilla commands");
    }

    /**
     * Returns a map of all vanilla commands that have been registered, including custom ones.
     *
     * @return a map of command name to {@link VanillaCommand} instance
     */
    public static Map<String, VanillaCommand> getRegisteredCommands() {
        return registeredCommands;
    }

    /**
     * Returns the list of default vanilla command instances (before registration).
     *
     * @return a list of default {@link VanillaCommand} instances
     */
    public static List<VanillaCommand> getVanillaCommands() {
        return VANILLA_COMMANDS;
    }
}
