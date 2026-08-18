package net.minestom.vanilla.commands.owner;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.vanilla.commands.VanillaCommand;

/**
 * Stops the server
 */
public class StopCommand extends VanillaCommand {
    public StopCommand() {
        super("stop");
        setCondition(permission(LEVEL_OWNER));
        setDefaultExecutor(this::defaultor);
    }

    @Override
    public Component usage(CommandSender sender, CommandContext context) {
        return null;
    }

    @Override
    public void defaultor(CommandSender sender, CommandContext context) {
        MinecraftServer.stopCleanly();
    }
}
