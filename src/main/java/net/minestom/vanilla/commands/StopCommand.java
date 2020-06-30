package net.minestom.vanilla.commands;

import fr.themode.command.Arguments;
import fr.themode.command.Command;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
