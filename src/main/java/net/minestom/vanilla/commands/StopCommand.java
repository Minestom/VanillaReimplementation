package net.minestom.vanilla.commands;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Arguments;
import net.minestom.server.command.builder.Command;

/**
 * Stops the server
 */
public class StopCommand extends Command<CommandSender> {
    public StopCommand() {
        super("stop");
        setCondition(this::condition);
        setDefaultExecutor(this::execute);
    }

    private boolean condition(CommandSender player) {
        return true; // TODO: permissions
    }

    private void execute(CommandSender player, Arguments arguments) {
        MinecraftServer.stopCleanly();
    }
}
