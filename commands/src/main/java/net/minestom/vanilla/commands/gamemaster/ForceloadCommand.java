package net.minestom.vanilla.commands.gamemaster;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.coordinate.CoordConversion;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.location.RelativeVec;
import net.minestom.vanilla.commands.VanillaCommand;
import net.minestom.vanilla.instancemeta.tickets.TicketManager;
import net.minestom.vanilla.instancemeta.tickets.TicketUtils;

import java.util.List;

public class ForceloadCommand extends VanillaCommand {

    public ForceloadCommand() {
        super("forceload");

        setCondition(permission(LEVEL_GAMEMASTER));

        setDefaultExecutor(this::defaultor);

        // forceload add <from> [<to>]
        //    Forces the chunk at the <from> position (through to <to> if set) in the dimension of the command's execution to be loaded constantly.
        this.addSyntax(
                this::usageAddFrom,
                ArgumentType.Literal("add"),
                ArgumentType.RelativeVec2("from")
        );
        this.addSyntax(
                this::usageAddFromTo,
                ArgumentType.Literal("add"),
                ArgumentType.RelativeVec2("from"),
                ArgumentType.RelativeVec2("to")
        );

        // forceload remove <from> [<to>]
        //    Unforces the chunk at the <from> position (through to <to> if set) in the dimension of the command's execution to be loaded constantly.
        this.addSyntax(
                this::usageRemoveFrom,
                ArgumentType.Literal("remove"),
                ArgumentType.RelativeVec2("from")
        );
        this.addSyntax(
                this::usageRemoveFromTo,
                ArgumentType.Literal("remove"),
                ArgumentType.RelativeVec2("from"),
                ArgumentType.RelativeVec2("to")
        );
    }

    private void addForceLoad(Instance instance, int chunkX, int chunkZ) {
        addForceLoad(instance, CoordConversion.chunkIndex(chunkX, chunkZ));
    }

    private void addForceLoad(Instance instance, long chunkIndex) {
        TicketManager.Ticket ticketToAdd = TicketManager.Ticket.from(TicketManager.FORCED_TICKET, chunkIndex);
        TicketUtils.waitingTickets(instance, List.of(ticketToAdd));
    }

    private void removeForceLoad(Instance instance, int chunkX, int chunkZ) {
        removeForceLoad(instance, CoordConversion.chunkIndex(chunkX, chunkZ));
    }

    private void removeForceLoad(Instance instance, long chunkIndex) {

        TicketManager.Ticket ticketToRemove = TicketManager.Ticket.from(TicketManager.FORCED_TICKET, chunkIndex);
        TicketUtils.removingTickets(instance, List.of(ticketToRemove));
    }

    private void usageAddFrom(CommandSender sender, CommandContext context) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.translatable("permissions.requires.player", NamedTextColor.RED));
            return;
        }
        RelativeVec fromVec = context.get("from");
        Vec position = fromVec.from(player.getPosition());

        // Get chunk position
        int chunkX = CoordConversion.globalToChunk(position.x());
        int chunkZ = CoordConversion.globalToChunk(position.z());

        // Add the force load
        Instance instance = player.getInstance();
        addForceLoad(instance, chunkX, chunkZ);
    }

    private void usageAddFromTo(CommandSender sender, CommandContext context) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.translatable("permissions.requires.player", NamedTextColor.RED));
            return;
        }
        RelativeVec fromVec = context.get("from");
        RelativeVec toVec = context.get("to");
        Vec from = fromVec.from(player.getPosition());
        Vec to = toVec.from(player.getPosition());

        int startX = Math.min(from.blockX(), to.blockX());
        int endX = Math.max(from.blockX(), to.blockX());
        int startZ = Math.min(from.blockZ(), to.blockZ());
        int endZ = Math.max(from.blockZ(), to.blockZ());

        Instance instance = player.getInstance();

        for (int offX = startX; offX < endX; offX += 16) {
            for (int offZ = startZ; offZ < endZ; offZ += 16) {
                // Get chunk position
                int chunkX = CoordConversion.globalToChunk(offX);
                int chunkZ = CoordConversion.globalToChunk(offZ);
                removeForceLoad(instance, chunkX, chunkZ);
            }
        }
    }

    private void usageRemoveFrom(CommandSender sender, CommandContext context) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.translatable("permissions.requires.player", NamedTextColor.RED));
            return;
        }
        RelativeVec fromVec = context.get("from");
        Vec position = fromVec.from(player.getPosition());

        // Get chunk position
        int chunkX = CoordConversion.globalToChunk(position.x());
        int chunkZ = CoordConversion.globalToChunk(position.z());

        // Remove force load
        Instance instance = player.getInstance();

        removeForceLoad(instance, chunkX, chunkZ);
    }

    private void usageRemoveFromTo(CommandSender sender, CommandContext context) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.translatable("permissions.requires.player", NamedTextColor.RED));
            return;
        }
        RelativeVec fromVec = context.get("from");
        RelativeVec toVec = context.get("to");
        Vec from = fromVec.from(player.getPosition());
        Vec to = toVec.from(player.getPosition());

        int minX = Math.min(from.blockX(), to.blockX());
        int maxX = Math.max(from.blockX(), to.blockX());
        int minZ = Math.min(from.blockZ(), to.blockZ());
        int maxZ = Math.max(from.blockZ(), to.blockZ());

        Instance instance = player.getInstance();

        for (int offX = minX; offX <= maxX; offX += 16) {
            for (int offZ = minZ; offZ <= maxZ; offZ += 16) {
                // Get chunk position
                int chunkX = CoordConversion.globalToChunk(offX);
                int chunkZ = CoordConversion.globalToChunk(offZ);

                // Remove the force load
                removeForceLoad(instance, chunkX, chunkZ);
            }
        }
    }

    @Override
    public Component usage(CommandSender sender, CommandContext context) {
        return Component.text("/forceload add <from> [<to>]")
                .appendNewline()
                .append(Component.text("/forceload remove (<from>|all)"))
                .appendNewline()
                .append(Component.text("/forceload query [<pos>]"));
    }

    @Override
    public void defaultor(CommandSender sender, CommandContext context) {
        sender.sendMessage(Component.translatable("command.unknown.command", NamedTextColor.RED)
                .appendNewline()
                .append(Component.text(context.getInput().replace(context.getCommandName() + " ",""), NamedTextColor.GRAY)
                        .append(Component.translatable("command.context.here", NamedTextColor.RED).decoration(TextDecoration.ITALIC,true))));
    }
}
