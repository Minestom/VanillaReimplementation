package net.minestom.vanilla.commands.gamemaster;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.vanilla.commands.VanillaCommand;

import java.util.List;

/**
 * Command that make a player change gamemode, made in
 * the style of the vanilla /gamemode command.
 *
 * @see <a href="https://minecraft.wiki/w/Commands/gamemode">...</a>
 */
public class GamemodeCommand extends VanillaCommand {

    public GamemodeCommand() {
        super("gamemode");

        setCondition(permission(LEVEL_GAMEMASTER));

        ArgumentEnum<GameMode> gamemode = ArgumentType.Enum("gamemode", GameMode.class).setFormat(ArgumentEnum.Format.LOWER_CASED);

        ArgumentEntity target = ArgumentType.Entity("targets").onlyPlayers(true);

        setDefaultExecutor(this::defaultor);

        //Command Syntax for /gamemode <gamemode>
        addSyntax((sender, context) -> {
            GameMode mode = context.get(gamemode);

            setGamemodeSelf(sender, mode);

        }, gamemode);

        //Command Syntax for /gamemode <gamemode> [targets]
        addSyntax((sender, context) -> {
            GameMode mode = context.get(gamemode);
            List<Entity> entities = context.get(target).find(sender);
            //Set the gamemode for the targets
            setGamemodeOther(sender, mode, entities);
        }, gamemode, target);
    }


    @Override
    public Component usage(CommandSender sender, CommandContext context) {
        return Component.text("/gamemode <gamemode> [<targets>]");
    }

    @Override
    public void defaultor(CommandSender sender, CommandContext context) {

        if (hasNoArguments(context)) {
            sender.sendMessage(Component.translatable("command.unknown.command", NamedTextColor.RED)
                    .appendNewline()
                    .append(Component.text(context.getInput().replace(context.getCommandName() + " ",""), NamedTextColor.GRAY)
                            .append(Component.translatable("command.context.here", NamedTextColor.RED).decoration(TextDecoration.ITALIC,true))));
            return;
        }

        // Get the part of the input that was supposed to be the gamemode
        String invalidInput = context.getInput().replace(context.getCommandName() + " ", "");

        // Correctly create a translatable component with the placeholder filled
        sender.sendMessage(Component.translatable("argument.gamemode.invalid",
                Component.text(invalidInput)
        ).color(NamedTextColor.RED));
    }

    private void setGamemodeSelf(CommandSender sender, GameMode gameMode) {
        if (!(sender instanceof final Player playerSender)) {
            sender.sendMessage(Component.translatable("permissions.requires.player", NamedTextColor.RED));
            return;
        }
        setGamemode(playerSender, gameMode);
    }

    private void setGamemodeOther(CommandSender sender, GameMode gameMode, List<Entity> entities) {
        if (entities.isEmpty()) {
            sender.sendMessage(Component.translatable("argument.entity.notfound.player", NamedTextColor.RED));
            return;
        }

        for (Entity entity : entities) {

            setGamemode(entity, gameMode);
        }
    }


    private void setGamemode(Entity entity, GameMode gamemode) {

        final Player player = (Player) entity;

        if (player.getGameMode().equals(gamemode)) return;

        player.setGameMode(gamemode);
        player.sendMessage(Component.translatable("gameMode.changed", Component.translatable("gameMode." + gamemode.name().toLowerCase())));
    }
}
