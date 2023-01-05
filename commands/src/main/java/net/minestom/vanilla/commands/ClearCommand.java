package net.minestom.vanilla.commands;

import net.kyori.adventure.text.Component;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Command that clears a player's inventory
 */
public class ClearCommand extends VanillaCommand {

    public ClearCommand() {
        super("clear", "minecraft.command.clear");
        setCondition(this::condition);

        var target = ArgumentType.Entity("target").onlyPlayers(true);
        addSyntax(this::targetedSyntax, target);
        addSyntax(this::nonTargetedSyntax);
    }

    @Override
    public boolean condition(@NotNull CommandSender sender, String command) {
        return hasPermissionOrLevel(sender, 2);
    }

    @Override
    protected void usage(@NotNull CommandSender sender, @NotNull CommandContext context) {
        sender.sendMessage(Component.text("/clear [username]"));
    }

    public void nonTargetedSyntax(CommandSender sender, CommandContext context) {
        Player player = (Player) sender;
        player.getInventory().clear();
    }

    public void targetedSyntax(CommandSender sender, CommandContext context) {
        Player player = context.get("target");
        player.getInventory().clear();
    }
}