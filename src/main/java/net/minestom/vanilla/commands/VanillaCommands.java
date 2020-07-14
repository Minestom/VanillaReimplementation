package net.minestom.vanilla.commands;

import net.minestom.server.command.CommandManager;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.instance.block.Block;
import net.minestom.vanilla.blocks.ShulkerBoxBlock;

import java.util.function.Supplier;

/**
 * All commands available in the vanilla reimplementation
 */
public enum VanillaCommands {

    GAMEMODE(GamemodeCommand::new),
    DIFFICULTY(DifficultyCommand::new),
    ME(MeCommand::new),
    STOP(StopCommand::new),
    HELP(HelpCommand::new),
    SAVE_ALL(SaveAllCommand::new),
    WHISPER(() -> new WhisperCommand("whisper")),
    MSG(() -> new WhisperCommand("msg")),
    TELL(() -> new WhisperCommand("tell")),
    ;

    private final Supplier<Command<? extends CommandSender>> commandCreator;

    private VanillaCommands(Supplier<Command<? extends CommandSender>> commandCreator) {
        this.commandCreator = commandCreator;
    }

    /**
     * Register all vanilla commands into the given manager
     * @param manager
     */
    @SuppressWarnings("unchecked")
	public static void registerAll(CommandManager manager) {
        for(VanillaCommands vanillaCommand : values()) {
            Command<? extends CommandSender> command = vanillaCommand.commandCreator.get();
            manager.register((Command<CommandSender>) command);
        }
    }
}
