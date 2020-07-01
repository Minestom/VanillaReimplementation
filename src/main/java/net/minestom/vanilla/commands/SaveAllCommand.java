package net.minestom.vanilla.commands;

import fr.themode.command.Arguments;
import fr.themode.command.Command;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;

/**
 * Save the server
 */
public class SaveAllCommand extends Command<CommandSender> {
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
            i.saveChunksToStorageFolder(() -> System.out.println("Saved dimension "+i.getDimension().name()));
        });
    }
}
