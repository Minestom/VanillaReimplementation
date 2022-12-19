package net.minestom.vanilla.commands;

import net.minestom.server.command.CommandManager;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.vanilla.VanillaReimplementation;
import net.minestom.vanilla.commands.execute.ExecuteCommand;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * All commands available in the vanilla reimplementation
 */
public enum VanillaCommands {

    FORCELOAD(ForceloadCommand::new),
    GAMEMODE(GamemodeCommand::new),
    GAMERULE(() -> {
        Command command = new Command("gamerule");
        command.addSyntax((sender, context) -> {
            int speed = context.get("speed");
            sender.sendMessage("Gamerule randomTickSpeed updated to " + speed);
            System.setProperty("vri.gamerule.randomtickspeed", String.valueOf(speed));
        }, ArgumentType.Literal("randomTickSpeed"), ArgumentType.Integer("speed"));
        return command;
    }),
    DIFFICULTY(DifficultyCommand::new),
    EXECUTE(ExecuteCommand::new),
    ME(MeCommand::new),
    STOP(StopCommand::new),
    HELP(HelpCommand::new),
    SAVE_ALL(SaveAllCommand::new),
    ;

    private final Function<VanillaReimplementation, Command> commandCreator;

    VanillaCommands(Function<VanillaReimplementation, Command> commandCreator) {
        this.commandCreator = commandCreator;
    }

    VanillaCommands(Supplier<Command> commandCreator) {
        this.commandCreator = vri -> commandCreator.get();
    }

    /**
     * Register all vanilla commands into the given manager
     *
     * @param manager the command manager to register commands on
     */
    public static void registerAll(@NotNull CommandManager manager, @NotNull VanillaReimplementation vri) {
        for (VanillaCommands vanillaCommand : values()) {
            Command command = vanillaCommand.commandCreator.apply(vri);
            manager.register(command);
        }
    }
}
