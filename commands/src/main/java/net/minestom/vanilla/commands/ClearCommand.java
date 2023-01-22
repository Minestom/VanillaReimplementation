package net.minestom.vanilla.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.condition.Conditions;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.utils.entity.EntityFinder;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Clears items from player inventory, including items being dragged by the player.
 *
 * @see <a href=https://minecraft.fandom.com/wiki/Commands/clear>Source</a>
 */
public class ClearCommand extends VanillaCommand {

    public ClearCommand() {
        super("clear", 2);
        var targets = ArgumentType.Entity("targets").onlyPlayers(true);
        addSyntax(this::executeOthers, targets);
        addSyntax(this::executeSelf);
    }

    @Override
    protected String usage() {
        return "/clear [<targets>] [<item>]";
    }

    private void clearSingleInventory(@NotNull CommandSender sender, @NotNull Player player) {
        final PlayerInventory inv = player.getInventory();
        final int items = inv.getItemStacks().length;

        if (items > 0) {
            inv.clear();
            sender.sendMessage("Removed " + items + " items from " + player.getUsername());
        } else {
            sender.sendMessage(Component.text("No items were found on " + player.getUsername(), NamedTextColor.RED));
        }
    }

    private void clearMultipleInventory(@NotNull CommandSender sender, List<Player> players) {
        int totalItems = 0;
        int totalCleared = 0;

        for (Player player : players) {
            final PlayerInventory inv = player.getInventory();
            final int items = inv.getItemStacks().length;

            if (items > 0) {
                totalItems += items;
                totalCleared++;
                inv.clear();
            }
        }

        if (totalCleared == 0) {
            sender.sendMessage("No items found on " + players.size() + " players");
        } else {
            sender.sendMessage("Removed " + totalItems + " items from " + totalCleared + " players");
        }
    }

    public void executeSelf(CommandSender sender, CommandContext context) {
        if (Conditions.playerOnly(sender, context.getCommandName()))
            clearSingleInventory(sender, (Player) sender);
    }

    public void executeOthers(CommandSender sender, CommandContext context) {
        EntityFinder finder = context.get("targets");
        List<Player> targets = finder.find(sender).stream().map(e -> (Player) e).toList();

        switch (targets.size()) {
            case 0 -> sender.sendMessage(Component.text("No players found", NamedTextColor.RED));
            case 1 -> clearSingleInventory(sender, targets.get(0));
            default -> clearMultipleInventory(sender, targets);
        }
    }
}