package net.minestom.vanilla.commands;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Returns the list of all available commands
 */
public class HelpCommand extends Command {
    public HelpCommand() {
        super("help");

        setDefaultExecutor(this::execute);
    }

    private void execute(CommandSender sender, CommandContext context) {
        sender.sendMessage("=== Help ===");

        List<VanillaCommands> commands = new ArrayList<>();

        Collections.addAll(commands, VanillaCommands.values());

        commands.sort(this::compareCommands);

        commands.forEach(command -> {
            sender.sendMessage("/"+command.name().toLowerCase());
        });

        sender.sendMessage("============");
    }

    private int compareCommands(VanillaCommands a, VanillaCommands b) {
        return a.name().compareTo(b.name());
    }
}
