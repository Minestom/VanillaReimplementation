package net.minestom.vanilla.commands;

import net.minestom.server.command.CommandManager;
import net.minestom.server.command.builder.Command;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

/**
 * All commands available in the vanilla reimplementation
 */
public enum VanillaCommands {

    FORCELOAD(ForceloadCommand::new),
    GAMEMODE(GamemodeCommand::new),
    DIFFICULTY(DifficultyCommand::new),
    ME(MeCommand::new),
    STOP(StopCommand::new),
    HELP(HelpCommand::new),
    SAVE_ALL(SaveAllCommand::new),
    CLEAR(ClearCommand::new),
    OP(OpCommand::new),
    DEOP(DeopCommand::new);

    private final Supplier<VanillaCommand> commandCreator;

    VanillaCommands(Supplier<VanillaCommand> commandCreator) {
        this.commandCreator = commandCreator;
    }

    public VanillaCommand getCommand() {
        return commandCreator.get();
    }

    /**
     * Register all vanilla commands into the given manager
     *
     * @param manager the command manager to register commands on
     */
    public static void registerAll(@NotNull CommandManager manager) {
        for (VanillaCommands vanillaCommand : values()) {
            Command command = vanillaCommand.commandCreator.get();
            manager.register(command);
        }
    }

    static final List<String> USAGES = Arrays.stream(VanillaCommands.values())
            .sorted(Comparator.comparing(o -> o.getCommand().getName()))
            .map(c -> c.getCommand().usage())
            .toList();


}
