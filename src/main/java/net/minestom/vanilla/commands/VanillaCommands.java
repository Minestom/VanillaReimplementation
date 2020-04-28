package net.minestom.vanilla.commands;

import fr.themode.command.Command;
import net.minestom.server.command.CommandManager;
import net.minestom.server.entity.Player;

import java.util.function.Supplier;

/**
 * All commands available in the vanilla reimplementation
 */
public enum VanillaCommands {

    GAMEMODE(GamemodeCommand::new),
    HELP(HelpCommand::new);

    private final Supplier<Command<Player>> commandCreator;

    private VanillaCommands(Supplier<Command<Player>> commandCreator) {
        this.commandCreator = commandCreator;
    }

    /**
     * Register all vanilla commands into the given manager
     * @param manager
     */
    public static void registerAll(CommandManager manager) {
        for(VanillaCommands vanillaCommand : values()) {
            Command<Player> command = vanillaCommand.commandCreator.get();
            manager.register(command);
        }
    }
}
