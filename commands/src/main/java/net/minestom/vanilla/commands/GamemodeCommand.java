package net.minestom.vanilla.commands;

import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.command.builder.condition.Conditions;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.entity.EntityFinder;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;

/**
 * Command that make a player change gamemode, made in
 * the style of the vanilla /gamemode command.
 *
 * @see <a href="https://minecraft.fandom.com/wiki/Commands/gamemode">Wiki</a>
 */
public class GamemodeCommand extends VanillaCommand {

    public GamemodeCommand() {
        super("gamemode", 2);

        //GameMode parameter
        var gamemode = gamemode();
        ArgumentEntity targets = ArgumentType.Entity("targets").onlyPlayers(true);;

        //Command Syntax for /gamemode <gamemode>
        addSyntax((sender, context) -> {
            if (Conditions.playerOnly(sender, context.getCommandName())) {
                Player player = (Player) sender;
                GameMode mode = context.get(gamemode);
                executeSelf(player, mode);
            }
        }, gamemode);

        addSyntax((sender, context) -> {
            EntityFinder finder = context.get(targets);
            GameMode mode = context.get(gamemode);
            executeOthers(sender, mode, finder);
        }, gamemode, targets);
    }

    @Override
    protected String usage() {
        return "/gamemode <gamemode> [targets]";
    }

    private void executeOthers(CommandSender sender, GameMode mode, EntityFinder finder) {
        List<Player> players = finder.find(sender).stream().map(c -> (Player) c).toList();

        if (players.isEmpty()) {
            sendTranslatable(sender, "argument.entity.notfound.player", NamedTextColor.RED);
            return;
        }

        for (Player player : players) {
            if (player != sender) {
                player.setGameMode(mode);

                String gamemodeString = "gameMode." + mode.name().toLowerCase(Locale.ROOT);
                Component gamemodeComponent = Component.translatable(gamemodeString);
                Component playerName = Component.text(player.getUsername());

                sendTranslatable(sender, "gameMode.changed", gamemodeComponent);
                sendTranslatable(sender, "commands.gamemode.success.other", playerName, gamemodeComponent);
                continue;
            }
            executeSelf(player, mode);
        }
    }

    private void executeSelf(Player player, GameMode mode) {
        if (player.getGameMode() == mode) {
            return;
        }

        player.setGameMode(mode);
        String gamemodeString = "gameMode." + mode.name().toLowerCase(Locale.ROOT);
        sendTranslatable(player, "commands.gamemode.success.self", Component.translatable(gamemodeString));
    }

    private ArgumentEnum<GameMode> gamemode() {
        var arg = ArgumentType.Enum("gamemode", GameMode.class).setFormat(ArgumentEnum.Format.LOWER_CASED);
        arg.setCallback((sender, exception) -> sendTranslatable(sender, "command.unknown.argument"));
        return arg;
    }
}
