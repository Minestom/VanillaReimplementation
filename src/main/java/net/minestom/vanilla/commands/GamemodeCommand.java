package net.minestom.vanilla.commands;

import club.thectm.minecraft.text.TextBuilder;
import fr.themode.command.Arguments;
import fr.themode.command.Command;
import fr.themode.command.arguments.Argument;
import fr.themode.command.arguments.ArgumentType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;

import java.util.Optional;

/**
 * Command that make a player change gamemode
 */
public class GamemodeCommand extends Command<Player> {
    public GamemodeCommand() {
        super("gamemode");

        setCondition(this::isAllowed);

        setDefaultExecutor(this::usage);

        Argument player = ArgumentType.Word("player");

        GameMode[] gameModes = GameMode.values();
        String[] names = new String[gameModes.length];
        for (int i = 0; i < gameModes.length; i++) {
            names[i] = gameModes[i].name().toLowerCase();
        }
        Argument mode = ArgumentType.Word("mode").from(names);

        addCallback(this::gameModeCallback, mode);

        addSyntax(this::executeOnSelf, mode);
        addSyntax(this::executeOnOther, player, mode);
    }

    private void usage(Player player, Arguments arguments) {
        player.sendMessage("Usage: /gamemode [player] <gamemode>");
    }

    private void executeOnSelf(Player player, Arguments arguments) {
        String gamemodeName = arguments.getWord("mode");
        GameMode mode = GameMode.valueOf(gamemodeName.toUpperCase());
        assert mode != null; // mode is not supposed to be null, because gamemodeName will be valid
        player.setGameMode(mode);
        player.sendMessage(TextBuilder.ofTranslation("commands.gamemode.success.self", TextBuilder.of(gamemodeName).build()).build());
    }

    private void executeOnOther(Player player, Arguments arguments) {
        String gamemodeName = arguments.getWord("mode");
        String targetName = arguments.getWord("player");
        GameMode mode = GameMode.valueOf(gamemodeName.toUpperCase());
        assert mode != null; // mode is not supposed to be null, because gamemodeName will be valid
        Optional<Player> target = player.getInstance().getPlayers().stream().filter(p -> p.getUsername().equalsIgnoreCase(targetName)).findFirst();
        if (target.isPresent()) {
            target.get().setGameMode(mode);
            player.sendMessage(TextBuilder.ofTranslation("commands.gamemode.success.other", TextBuilder.of(targetName).build(), TextBuilder.of(gamemodeName).build()).build());
        } else {
            player.sendMessage(TextBuilder.ofTranslation("argument.player.unknown").build());
        }
    }

    private void gameModeCallback(Player player, String gamemode, int error) {
        player.sendMessage("'" + gamemode + "' is not a valid gamemode!");
    }

    private boolean isAllowed(Player player) {
        return true; // TODO: permissions
    }
}

