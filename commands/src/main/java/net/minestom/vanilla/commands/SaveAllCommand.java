package net.minestom.vanilla.commands;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.CommandContext;
import org.jetbrains.annotations.NotNull;

/**
 * Save the server
 */
public class SaveAllCommand extends VanillaCommand {
    public SaveAllCommand() {
        super("save-all", 3);
        setDefaultExecutor(this::execute);
    }

    @Override
    protected String usage() {
        return "/save-all";
    }

    private void execute(CommandSender player, CommandContext arguments) {
        MinecraftServer.getInstanceManager().getInstances().forEach(i -> {
            i.saveChunksToStorage();
            System.out.println("Saved dimension " + i.getDimensionType().getName());
        });
    }
}
