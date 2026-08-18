package net.minestom.vanilla.commands.all;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.adventure.audience.Audiences;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentStringArray;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import net.minestom.vanilla.commands.VanillaCommand;

/**
 * Command that displays a player action
 */
public class MeCommand extends VanillaCommand {
    public MeCommand() {
        super("me");

        setCondition(permission(LEVEL_ALL));

        setDefaultExecutor(this::defaultor);

        ArgumentStringArray message = ArgumentType.StringArray("message");

        addSyntax(this::execute, message);
    }

    @Override
    public Component usage(CommandSender sender, CommandContext context) {
        return Component.text("/me <Action>");
    }

    @Override
    public void defaultor(CommandSender sender, CommandContext context) {
        sender.sendMessage(Component.translatable("command.unknown.command", NamedTextColor.RED)
                .appendNewline()
                .append(Component.text(context.getInput().replace(context.getCommandName() + " ",""), NamedTextColor.GRAY)
                        .append(Component.translatable("command.context.here", NamedTextColor.RED).decoration(TextDecoration.ITALIC,true))));
    }

    private void execute(CommandSender sender, CommandContext arguments) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.translatable("permissions.requires.player", NamedTextColor.RED));
            return;
        }
        String[] messageParts = arguments.get("message");

        TextComponent.Builder builder = Component.text();

        builder.append(Component.text(" * " + player.getUsername()));

        builder.append(Component.text(" "));
        builder.append(Component.text(messageParts[0]));

        for (int i = 1; i < messageParts.length; i++) {
            builder.append(Component.text(messageParts[i]));
        }

        Component message = builder.build();
        Audiences.all().sendMessage(message);
    }
}

