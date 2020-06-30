package net.minestom.vanilla.commands;

import fr.themode.command.Arguments;
import fr.themode.command.Command;
import net.minestom.server.command.CommandSender;
import net.minestom.server.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Returns the list of all available commands
 */
public class HelpCommand extends Command<CommandSender> {
    public HelpCommand() {
        super("help");

        setDefaultExecutor(this::execute);
    }

    private void execute(CommandSender player, Arguments arguments) {
        player.sendMessage("=== Help ===");
        List<VanillaCommands> commands = new ArrayList<>();
        for(VanillaCommands command : VanillaCommands.values()) {
            commands.add(command);
        }
        Collections.sort(commands, this::compareCommands);

        commands.forEach(command -> {
            player.sendMessage("/"+command.name().toLowerCase());
        });
        player.sendMessage("============");
    }

    private int compareCommands(VanillaCommands a, VanillaCommands b) {
        return a.name().compareTo(b.name());
    }
}
