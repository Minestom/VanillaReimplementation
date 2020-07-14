package net.minestom.vanilla.commands;

import net.minestom.server.command.builder.Arguments;
import net.minestom.server.command.builder.Command;

import java.util.Optional;
import java.util.stream.Stream;

import net.minestom.server.MinecraftServer;
import net.minestom.server.chat.ColoredText;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.player.PlayerUtils;
import net.minestom.server.world.Difficulty;

/**
 * Command that allows messaging of other players
 */
public class GiveCommand extends Command<Player> {
    public GiveCommand(String alias) {
        super(alias);
        setDefaultExecutor(this::usage);

        Argument player = ArgumentType.Word("player");
        Argument message = ArgumentType.StringArray("array");

        addSyntax(this::execute, player, message);
    }

    private void usage(Player player, Arguments arguments) {
        player.sendMessage("Usage: /give <player> <message>");
    }

    private void execute(Player player, Arguments arguments) {
        String targetName = arguments.getWord("player");
        String[] Message = arguments.getStringArray("array");
        Optional<Player> target = player.getInstance().getPlayers().stream().filter(p -> p.getUsername().equalsIgnoreCase(targetName)).findFirst();
        if (target.isPresent()) {
        	// send item
        } else {
            player.sendMessage(ColoredText.ofFormat("{@argument.player.unknown}"));
        }
    }

    private boolean isAllowed(Player player) {
        return true; // TODO: permissions
    }
}

