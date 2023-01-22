package net.minestom.vanilla.commands;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.condition.Conditions;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.utils.location.RelativeVec;
import net.minestom.vanilla.instancemeta.tickets.TicketManager;
import net.minestom.vanilla.instancemeta.tickets.TicketUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Force chunks to load constantly or not, {@code forceload add}, {@code forceload query} and
 * {@code forceload remove} are three separated commands
 *
 * @see <a href=https://minecraft.fandom.com/wiki/Commands/forceload>Source</a>
 *
 * TODO: Translatable messages
 */
public class ForceloadCommand extends VanillaCommand {

    public ForceloadCommand() {
        super("forceload", 2);

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
        this.addSyntax(this::usageRemoveFrom,
                ArgumentType.Literal("remove"),
                ArgumentType.RelativeVec2("from")
        );
        this.addSyntax(this::usageRemoveFromTo,
                ArgumentType.Literal("remove"),
                ArgumentType.RelativeVec2("from"),
                ArgumentType.RelativeVec2("to")
        );
    }

    @Override
    protected boolean condition(@NotNull CommandSender sender, String commandName) {
        return super.condition(sender, commandName) && Conditions.playerOnly(sender, commandName);
    }

    @Override
    protected String usage() {
        return """
                /forceload add <from> [<to>]
                /forceload remove (<from>|all)
                /forceload query [<pos>]""";
    }

    private void addForceLoad(Instance instance, int chunkX, int chunkZ) {
        addForceLoad(instance, ChunkUtils.getChunkIndex(chunkX, chunkZ));
    }

    private void addForceLoad(Instance instance, long chunkIndex) {
        TicketManager.Ticket ticketToAdd = TicketManager.Ticket.from(TicketManager.FORCED_TICKET, chunkIndex);
        TicketUtils.waitingTickets(instance, List.of(ticketToAdd));
    }

    private void removeForceLoad(Instance instance, int chunkX, int chunkZ) {
        removeForceLoad(instance, ChunkUtils.getChunkIndex(chunkX, chunkZ));
    }

    private void removeForceLoad(Instance instance, long chunkIndex) {

        TicketManager.Ticket ticketToRemove = TicketManager.Ticket.from(TicketManager.FORCED_TICKET, chunkIndex);
        TicketUtils.removingTickets(instance, List.of(ticketToRemove));
    }

    private void usageAddFrom(CommandSender sender, CommandContext context) {
        Player player = (Player) sender;
        RelativeVec fromVec = context.get("from");
        Vec position = fromVec.from(player.getPosition());

        // Get chunk position
        int chunkX = ChunkUtils.getChunkCoordinate(position.x());
        int chunkZ = ChunkUtils.getChunkCoordinate(position.z());

        // Add the force load
        Instance instance = player.getInstance();
        addForceLoad(instance, chunkX, chunkZ);
    }

    private void usageAddFromTo(CommandSender sender, CommandContext context) {
        Player player = (Player) sender;
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
                int chunkX = ChunkUtils.getChunkCoordinate(offX);
                int chunkZ = ChunkUtils.getChunkCoordinate(offZ);
                removeForceLoad(instance, chunkX, chunkZ);
            }
        }
    }

    private void usageRemoveFrom(CommandSender sender, CommandContext context) {
        Player player = (Player) sender;
        RelativeVec fromVec = context.get("from");
        Vec position = fromVec.from(player.getPosition());

        // Get chunk position
        int chunkX = ChunkUtils.getChunkCoordinate(position.x());
        int chunkZ = ChunkUtils.getChunkCoordinate(position.z());

        // Remove force load
        Instance instance = player.getInstance();

        removeForceLoad(instance, chunkX, chunkZ);
    }

    private void usageRemoveFromTo(CommandSender sender, CommandContext context) {
        Player player = (Player) sender;
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
                int chunkX = ChunkUtils.getChunkCoordinate(offX);
                int chunkZ = ChunkUtils.getChunkCoordinate(offZ);

                // Remove the force load
                removeForceLoad(instance, chunkX, chunkZ);
            }
        }
    }
}
