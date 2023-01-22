package net.minestom.vanilla.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.minestom.server.adventure.audience.Audiences;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentStringArray;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.condition.Conditions;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Displays a message about yourself.
 *
 * @see <a href=https://minecraft.fandom.com/wiki/Commands/me>Source</a>
 */
public class MeCommand extends VanillaCommand {
    public MeCommand() {
        super("me", 0);
        ArgumentStringArray message = ArgumentType.StringArray("message");
        addSyntax(this::execute, message);
    }

    @Override
    protected String usage() {
        return "/me <action>";
    }

    @Override
    protected boolean condition(@NotNull CommandSender sender, String commandName) {
        return Conditions.playerOnly(sender, commandName) && super.condition(sender, commandName);
    }

    private void execute(CommandSender sender, CommandContext arguments) {
        if (!Conditions.playerOnly(sender, arguments.getCommandName()))
            return;

        final Player player = (Player) sender;
        String[] messageParts = arguments.get("message");

        TextComponent.Builder builder = Component.text()
                .append(Component.text(" * " + player.getUsername()))
                .append(Component.space())
                .append(Component.text(String.join(" ", messageParts)));
        Audiences.all().sendMessage(builder.build());
    }
}
