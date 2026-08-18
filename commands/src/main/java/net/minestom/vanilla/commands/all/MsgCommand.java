package net.minestom.vanilla.commands.all;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.ConsoleSender;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.vanilla.commands.VanillaCommand;

import java.util.List;

public class MsgCommand extends VanillaCommand {
    public MsgCommand() {
        super("msg", "tell", "w");

        setCondition(permission(LEVEL_ALL));

        setDefaultExecutor(this::defaultor);

        ArgumentEntity targetArg = ArgumentType.Entity("targets").onlyPlayers(true);
        ArgumentString messageArg = ArgumentType.String("message");

        addSyntax((sender, context) -> {
            List<Entity> receivers = context.get(targetArg).find(sender);
            String message = context.get(messageArg);

            whisperTo(sender, receivers, message);

        }, targetArg, messageArg);


    }

    private void whisperTo(CommandSender sender, List<Entity> receivers, String message) {

        if (receivers.isEmpty()) {
            sender.sendMessage(Component.translatable("argument.entity.notfound.player", NamedTextColor.RED));
            return;
        }

        receivers.forEach(entity -> {
            if (!(entity instanceof Player player)) {
                return;
            }

            if (sender instanceof ConsoleSender) {
                player.sendMessage(Component.translatable("commands.message.display.incoming", Component.text("Server"), Component.text(message).decorate(TextDecoration.ITALIC).color(NamedTextColor.GRAY)));
                return;
            }

            String senderName = ((Player) sender).getUsername();
            sender.sendMessage(Component.translatable("commands.message.display.outgoing", Component.text(player.getUsername()), Component.text(message)).decorate(TextDecoration.ITALIC).color(NamedTextColor.GRAY));
            player.sendMessage(Component.translatable("commands.message.display.incoming", Component.text(senderName), Component.text(message)).decorate(TextDecoration.ITALIC).color(NamedTextColor.GRAY));
        });




    }

    @Override
    public Component usage(CommandSender sender, CommandContext context) {
        return Component.text("/msg <targets> <message>");
    }

    @Override
    protected void defaultor(CommandSender sender, CommandContext context) {
        sender.sendMessage(Component.translatable("command.unknown.command", NamedTextColor.RED)
                .appendNewline()
                .append(Component.text(context.getInput().replace(context.getCommandName() + " ",""), NamedTextColor.GRAY)
                        .append(Component.translatable("command.context.here", NamedTextColor.RED).decoration(TextDecoration.ITALIC,true))));
    }
}
