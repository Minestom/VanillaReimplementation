package net.minestom.vanilla.commands.admin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.ConsoleSender;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.vanilla.commands.VanillaCommand;

import java.util.List;

public class OpCommand extends VanillaCommand {
    public OpCommand() {
        super("op");
        //TODO set correct permission Level. But due a lack of console to otherwise run op, we place it for all.
        setCondition(permission(LEVEL_ALL));

        ArgumentEntity target = ArgumentType.Entity("target").onlyPlayers(true);
        setDefaultExecutor(this::defaultor);

        addSyntax((sender, context) -> {
            List<Entity> entities = context.get(target).find(sender);

            setOp(sender, context, entities);
        }, target);
    }

    private void setOp(CommandSender sender, CommandContext context, List<Entity> entities) {
        if (entities.isEmpty()) {
            sender.sendMessage(Component.translatable("argument.entity.notfound.player",NamedTextColor.RED));
            return;
        }

        for (Entity entity : entities) {

            final Player player = (Player) entity;

            if (player.getPermissionLevel() >= LEVEL_GAMEMASTER) {
                player.sendMessage(Component.translatable("commands.op.failed").color(NamedTextColor.RED));
                return;
            }
            // Todo make so permission level is based on server config (https://minecraft.wiki/w/Permission_level#Java_Edition_2)
            player.setPermissionLevel(LEVEL_OWNER);
            player.refreshCommands();

            if (sender instanceof ConsoleSender) {
                MinecraftServer.getConnectionManager().getOnlinePlayers().stream()
                        .filter(p -> player.getPermissionLevel() >= LEVEL_GAMEMASTER) // Filter for players with operator permission
                        .forEach(p -> player.sendMessage(Component.text("[Server: ").append(Component.translatable("commands.op.success", Component.text(player.getUsername()))).color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, true)));
                return;
            }

            MinecraftServer.getConnectionManager().getOnlinePlayers().stream()
                    .filter(p -> player.getPermissionLevel() >= LEVEL_GAMEMASTER) // Filter for players with operator permission
                    .forEach(p -> player.sendMessage(Component.translatable("commands.op.success", Component.text(player.getUsername()))));

        }
    }


    @Override
    public Component usage(CommandSender sender, CommandContext context) {
        return Component.text("/op [<targets>]");
    }

    @Override
    public void defaultor(CommandSender sender, CommandContext context) {
        sender.sendMessage(Component.translatable("command.unknown.argument").color(NamedTextColor.RED)
                .appendNewline()
                .append(Component.text(context.getCommandName()).color(NamedTextColor.RED).decoration(TextDecoration.UNDERLINED, true))
                .append(Component.translatable("command.context.here", NamedTextColor.RED).decoration(TextDecoration.ITALIC,true)));
    }
}
