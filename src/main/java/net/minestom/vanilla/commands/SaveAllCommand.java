package net.minestom.vanilla.commands;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Arguments;
import net.minestom.server.command.builder.Command;

/**
 * Save the server
 */
public class SaveAllCommand extends Command {
    public SaveAllCommand() {
        super("save-all");
        setCondition(this::condition);
        setDefaultExecutor(this::execute);
    }

    private boolean condition(CommandSender player) {
        return true; // TODO: permissions
    }

    private void execute(CommandSender player, Arguments arguments) {
        MinecraftServer.getInstanceManager().getInstances().forEach(i -> {
            i.saveChunksToStorage(() -> System.out.println("Saved dimension "+i.getDimensionType().getName()));
        });
    }
}
