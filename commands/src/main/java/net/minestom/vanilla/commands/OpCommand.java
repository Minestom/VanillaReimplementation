package net.minestom.vanilla.commands;

import net.kyori.adventure.text.Component;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

public class OpCommand extends VanillaCommand {

    public OpCommand() {
        super("op", "minecraft.command.op");
        var target = ArgumentType.Entity("target").onlyPlayers(true);
        addConditionalSyntax(this::condition, this::execute, target);
    }

    @Override
    protected boolean condition(@NotNull CommandSender sender, String commandName) {
        return hasPermissionOrLevel(sender, 3);
    }

    @Override
    protected void usage(@NotNull CommandSender sender, @NotNull CommandContext context) {
        sender.sendMessage("/op <username>");
    }

    private void execute(@NotNull CommandSender sender, @NotNull CommandContext context) {
        Player target = context.get("target");
        target.setPermissionLevel(4);
        target.sendMessage(Component.text("You are now an operator"));
        sender.sendMessage(Component.text("Made " + target.getName() + " a server operator"));
    }
}
