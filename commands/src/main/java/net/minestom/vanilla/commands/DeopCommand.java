package net.minestom.vanilla.commands;

import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.entity.EntityFinder;
import org.jetbrains.annotations.NotNull;

/**
 * Revokes operator status from a player.
 *
 * @see <a href=https://minecraft.fandom.com/wiki/Commands/deop>Source<a/>
 */
public class DeopCommand extends VanillaCommand {

    public DeopCommand() {
        super("deop", 3);
        var target = ArgumentType.Entity("target").onlyPlayers(true);;
        addSyntax(this::execute, target);
    }

    @Override
    protected String usage() {
        return "/deop <username>";
    }

    private void execute(@NotNull CommandSender sender, @NotNull CommandContext context) {
        Player target = ((EntityFinder) context.get("target")).findFirstPlayer(sender);

        if (target != null) {
            // Player has op so remove it
            if (target.getPermissionLevel() > 0) {
                target.setPermissionLevel(0);
                target.refreshCommands();
                sendTranslatable(sender, "commands.deop.success", target.getUsername());
                return;
            }
            sendTranslatable(sender, "commands.deop.failed", NamedTextColor.RED);
        }
    }
}