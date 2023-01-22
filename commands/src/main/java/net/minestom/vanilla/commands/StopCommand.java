package net.minestom.vanilla.commands;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.adventure.audience.Audiences;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.CommandContext;
import org.jetbrains.annotations.NotNull;

/**
 * Stops the server
 */
public class StopCommand extends VanillaCommand {

    public StopCommand() {
        super("stop", 4);
        setDefaultExecutor(this::execute);
    }

    @Override
    protected String usage() {
        return "/stop";
    }

    private void execute(CommandSender player, CommandContext context) {
        Audiences.console().sendMessage(Component.text("Stopping server..."));
        MinecraftServer.stopCleanly();
    }
}
