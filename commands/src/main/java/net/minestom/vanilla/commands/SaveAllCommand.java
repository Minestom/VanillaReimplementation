package net.minestom.vanilla.commands;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.vanilla.logging.Logger;

/**
 * Save the server
 */
public class SaveAllCommand extends Command {
    public SaveAllCommand() {
        super("save-all");
        setCondition(this::condition);
        setDefaultExecutor(this::execute);
    }

    private boolean condition(CommandSender player, String commandName) {
        return true; // TODO: permissions
    }

    private void execute(CommandSender player, CommandContext arguments) {
        MinecraftServer.getInstanceManager().getInstances().forEach(i -> {
            i.saveChunksToStorage();
            Logger.info("Saved dimension " + i.getDimensionType().name());
        });
    }
}
