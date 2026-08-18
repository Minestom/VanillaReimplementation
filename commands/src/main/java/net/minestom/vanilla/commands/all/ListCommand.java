package net.minestom.vanilla.commands.all;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentCommand;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import net.minestom.vanilla.commands.VanillaCommand;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ListCommand extends VanillaCommand {
    public ListCommand() {
        super("list");

        setCondition(permission(LEVEL_ALL));

        setDefaultExecutor(this::defaultor);

        ArgumentCommand arg = ArgumentType.Command("uuids");

        addSyntax((sender, context) -> listPlayers(sender, context, true), arg);

    }

    @Override
    public Component usage(CommandSender sender, CommandContext context) {
        return Component.text("/list [uuids]");
    }

    @Override
    public void defaultor(CommandSender sender, CommandContext context) {
        listPlayers(sender, context, false);
    }

    private void listPlayers(CommandSender sender, CommandContext context, boolean uuid) {
        int onlinePlayersCount = MinecraftServer.getConnectionManager().getOnlinePlayerCount();
        String maxPlayersCount = "???";
        //TODO add maxPlayerCount thingy from sever config / properties i think

        Collection<Player> onlinePlayers = MinecraftServer.getConnectionManager().getOnlinePlayers();

        List<Component> playerComponents = onlinePlayers.stream()
                .map(player -> {
                    // Start with the player's username
                    Component playerUsername = Component.text(player.getUsername());
                    Component playerUUID = Component.text(player.getUuid().toString());
                    if (uuid) {
                        // If UUIDs are requested, append "(UUID)" to the username
                        playerUsername = Component.translatable("commands.list.nameAndId", playerUsername, playerUUID);
                    }
                    return playerUsername;
                })
                .collect(Collectors.toList());

        Component playerListComponent = Component.join(
                JoinConfiguration.builder().separator(Component.text(", ")).build(),
                playerComponents
        );

        Component headerComponent = Component.translatable("commands.list.players", Component.text(onlinePlayersCount), Component.text(maxPlayersCount), Component.text());

        Component fullMessage = headerComponent.append(playerListComponent);

        sender.sendMessage(fullMessage);
    }
}
