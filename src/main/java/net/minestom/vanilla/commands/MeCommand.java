package net.minestom.vanilla.commands;

import fr.themode.command.Arguments;
import fr.themode.command.Command;
import fr.themode.command.arguments.Argument;
import fr.themode.command.arguments.ArgumentType;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.entity.Player;
import net.minestom.server.world.Difficulty;

/**
 * Command that displays a player action
 */
public class MeCommand extends Command<Player> {
    public MeCommand() {
        super("me");

        setDefaultExecutor(this::usage);

        Argument message = ArgumentType.StringArray("message");

        addSyntax(this::execute, message);
    }

    private void usage(Player player, Arguments arguments) {
        player.sendMessage("Usage: /me <message>");
    }

    private void execute(Player player, Arguments arguments) {
        String[] messageParts = arguments.getStringArray("message");
        StringBuilder builder = new StringBuilder();
        for(int i = 0;i < messageParts.length;i++) {
            if(i != 0) {
                builder.append(" ");
            }
            builder.append(messageParts[i]);
        }
        String message = builder.toString();
        MinecraftServer.getConnectionManager().getOnlinePlayers().forEach(p -> {
            p.sendMessage(" * "+player.getUsername()+" "+message);
        });
    }

    private boolean isAllowed(Player player) {
        return true; // TODO: permissions
    }
}

