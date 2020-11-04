package net.minestom.vanilla.commands;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Arguments;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.world.Difficulty;

/**
 * Command that make an instance change difficulty
 */
public class DifficultyCommand extends Command {
    public DifficultyCommand() {
        super("difficulty");

        setCondition(this::isAllowed);

        setDefaultExecutor(this::usage);

        Argument difficulty = ArgumentType.Word("difficulty").from("peaceful", "easy", "normal", "hard");

        setArgumentCallback(this::difficultyCallback, difficulty);

        addSyntax(this::execute, difficulty);
    }

    private void usage(CommandSender player, Arguments arguments) {
        player.sendMessage("Usage: /difficulty (peaceful|easy|normal|hard)");
    }

    private void execute(CommandSender player, Arguments arguments) {
        String difficultyName = arguments.getWord("difficulty");
        Difficulty difficulty = Difficulty.valueOf(difficultyName.toUpperCase());
        assert difficulty != null; // difficulty is not supposed to be null, because difficultyName will be valid
        MinecraftServer.setDifficulty(difficulty);
        player.sendMessage("You are now playing in " + difficultyName);
    }

    private void difficultyCallback(CommandSender player, String difficulty, int error) {
        player.sendMessage("'" + difficulty + "' is not a valid difficulty!");
    }

    private boolean isAllowed(CommandSender player, String commandName) {
        return true; // TODO: permissions
    }
}

