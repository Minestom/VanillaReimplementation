package net.minestom.vanilla.commands;

import net.kyori.adventure.text.Component;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.ConsoleSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.condition.CommandCondition;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Abstract base class for Vanilla commands.
 * Includes permission level handling and shared utility methods.
 */
public abstract class VanillaCommand extends Command {

    public final int LEVEL_ALL = 0;
    public final int LEVEL_MODERATOR = 1;
    public final int LEVEL_GAMEMASTER = 2;
    public final int LEVEL_ADMIN = 3;
    public final int LEVEL_OWNER = 4;

    public VanillaCommand(@NotNull String name, @Nullable String... aliases) {
        super(name, aliases);
    }

    public VanillaCommand(@NotNull String name) {
        super(name);
    }

    /**
     * Returns the usage message for this command, required and used for /help command.
     *
     * @param sender  the command sender
     * @param context the command context
     * @return the usage message as a Component
     */
    public abstract Component usage(CommandSender sender, CommandContext context);

    protected abstract void defaultor(CommandSender sender, CommandContext context);

    /**
     * Creates a command condition that checks if the sender has the required permission level.
     * Console senders are always allowed.
     *
     * @param level the required permission level
     * @return the command condition
     */
    public CommandCondition permission(int level) {
        return (sender, commandName) -> {
            if (sender instanceof ConsoleSender) return true;
            if (sender instanceof Player player) return player.getPermissionLevel() >= level;
            return false;
        };
    }

    /**
     * Checks whether the command was invoked without any arguments.
     *
     * @param context the command context
     * @return true if no arguments were given, false otherwise
     */
    public boolean hasNoArguments(CommandContext context) {
        return context.getCommandName().equals(context.getInput());
    }
}
