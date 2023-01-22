package net.minestom.vanilla.commands;

import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentType;
import org.jetbrains.annotations.NotNull;

/**
 * Returns the list of all available commands
 *
 * @see <a href=https://minecraft.fandom.com/wiki/Commands/help>Source<a/>
 */
public class HelpCommand extends VanillaCommand {

    public HelpCommand() {
        super("help", 0);
        var command = ArgumentType.Enum("command", VanillaCommands.class).setFormat(ArgumentEnum.Format.LOWER_CASED);
        addSyntax(this::defaultHelp);
        addSyntax(this::help, command);
    }

    @Override
    protected String usage() {
        return "/help [<command>]";
    }

    public void defaultHelp(@NotNull CommandSender sender, @NotNull CommandContext context) {
        VanillaCommands.USAGES.forEach(sender::sendMessage);
    }

    public void help(@NotNull CommandSender sender, @NotNull CommandContext context) {
        VanillaCommands entry = context.get("command");
        if (entry != null) {
            String help = entry.getCommand().usage();
            sender.sendMessage(help);
            return;
        }
        sendTranslatable(sender, "commands.help.failed", NamedTextColor.RED);
    }
}