package net.minestom.vanilla.commands;

import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.entity.EntityFinder;
import org.jetbrains.annotations.NotNull;

public class OpCommand extends VanillaCommand {

    public OpCommand() {
        super("op", 3);
        var target = argPlayer("target");
        addSyntax(this::execute, target);
    }

    @Override
    protected String usage() {
        return "/op <username>";
    }

    private void execute(@NotNull CommandSender sender, @NotNull CommandContext context) {
        Player target = ((EntityFinder) context.get("target")).findFirstPlayer(sender);
        if (target != null) {

            int level = target.getPermissionLevel();

            // Use server.properties value
            if (level != 4) {
                target.setPermissionLevel(4);
                sendTranslatable(sender, "commands.op.success", target.getUsername());
                target.refreshCommands();
            } else
                sendTranslatable(sender, "commands.op.failed");
        } else
            sendTranslatable(sender, "argument.entity.notfound.player", NamedTextColor.RED);
    }
}
