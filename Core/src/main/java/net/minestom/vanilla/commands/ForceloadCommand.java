package net.minestom.vanilla.commands;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.utils.location.RelativeVec;
import net.minestom.vanilla.instance.tickets.TicketManager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static net.minestom.server.command.builder.arguments.ArgumentType.Literal;
import static net.minestom.server.command.builder.arguments.ArgumentType.RelativeVec2;

/**
 * 	"forceload":
 * 		Description: "Forces chunks to constantly be loaded or not. "
 * 		BE: false
 * 		EE: false
 * 		JE: true
 * 		OP_Level: 2
 * 		BE_EE_OP_Level: 0
 * 		MP_Only: false
 */
@SuppressWarnings("UnstableApiUsage")
public class ForceloadCommand extends Command {

    private final Map<Instance, Set<Long>> forceLoadedChunks = new HashMap<>();

    public ForceloadCommand() {
        super("forceload");

        // forceload add <from> [<to>]
        //    Forces the chunk at the <from> position (through to <to> if set) in the dimension of the command's execution to be loaded constantly.
        this.addSyntax(
                this::usageAddFrom,
                Literal("add"),
                RelativeVec2("from")
        );
        this.addSyntax(
                this::usageAddFromTo,
                Literal("add"),
                RelativeVec2("from"),
                RelativeVec2("to")
        );



        // forceload remove <from> [<to>]
        //    Unforces the chunk at the <from> position (through to <to> if set) in the dimension of the command's execution to be loaded constantly.
        this.addSyntax(
                this::usageRemoveFrom,
                Literal("remove"),
                RelativeVec2("from")
        );
        this.addSyntax(
                this::usageRemoveFromTo,
                Literal("remove"),
                RelativeVec2("from"),
                RelativeVec2("to")
        );

        // forceload remove all
        //    Unforces all chunks in the dimension of the command's execution to be loaded constantly.
        this.addSyntax(
                this::usageRemoveAll,
                Literal("remove"),
                Literal("all")
        );
    }

    private void addForceLoad(Instance instance, TicketManager manager, int chunkX, int chunkZ) {
        addForceLoad(instance, manager, ChunkUtils.getChunkIndex(chunkX, chunkZ));
    }

    private void addForceLoad(Instance instance, TicketManager manager, long chunkIndex) {
        Set<Long> forceLoadedChunks = this.forceLoadedChunks.computeIfAbsent(instance, k -> new HashSet<>());

        if (forceLoadedChunks.contains(chunkIndex)) {
            return;
        }

        forceLoadedChunks.add(chunkIndex);
        manager.addTicket(chunkIndex, TicketManager.FORCED_TICKET);
    }

    private void removeForceLoad(Instance instance, TicketManager manager, int chunkX, int chunkZ) {
        removeForceLoad(instance, manager, ChunkUtils.getChunkIndex(chunkX, chunkZ));
    }

    private void removeForceLoad(Instance instance, TicketManager manager, long chunkIndex) {
        Set<Long> forceLoadedChunks = this.forceLoadedChunks.computeIfAbsent(instance, k -> new HashSet<>());

        if (!forceLoadedChunks.contains(chunkIndex)) {
            return;
        }

        forceLoadedChunks.remove(chunkIndex);
        manager.removeTicket(chunkIndex, TicketManager.FORCED_TICKET);
    }

    private void usageAddFrom(CommandSender sender, CommandContext context) {
        Player player = sender.asPlayer();
        RelativeVec fromVec = context.get("from");
        Vec position = fromVec.from(player.getPosition());

        // Get chunk position
        int chunkX = ChunkUtils.getChunkCoordinate(position.x());
        int chunkZ = ChunkUtils.getChunkCoordinate(position.z());

        // Add the force load
        Instance instance = player.getInstance();
        addForceLoad(instance, TicketManager.of(instance), chunkX, chunkZ);
    }

    private void usageAddFromTo(CommandSender sender, CommandContext context) {
        Player player = sender.asPlayer();
        RelativeVec fromVec = context.get("from");
        RelativeVec toVec = context.get("to");
        Vec from = fromVec.from(player.getPosition());
        Vec to = toVec.from(player.getPosition());

        int startX = Math.min(from.blockX(), to.blockX());
        int endX = Math.max(from.blockX(), to.blockX());
        int startZ = Math.min(from.blockZ(), to.blockZ());
        int endZ = Math.max(from.blockZ(), to.blockZ());

        Instance instance = player.getInstance();
        TicketManager ticketManager = TicketManager.of(instance);

        for (int offX = startX; offX < endX; offX += 16) {
            for (int offZ = startZ; offZ < endZ; offZ += 16) {
                // Get chunk position
                int chunkX = ChunkUtils.getChunkCoordinate(offX);
                int chunkZ = ChunkUtils.getChunkCoordinate(offZ);
                removeForceLoad(instance, ticketManager, chunkX, chunkZ);
            }
        }
    }

    private void usageRemoveFrom(CommandSender sender, CommandContext context) {
        Player player = sender.asPlayer();
        RelativeVec fromVec = context.get("from");
        Vec position = fromVec.from(player.getPosition());

        // Get chunk position
        int chunkX = ChunkUtils.getChunkCoordinate(position.x());
        int chunkZ = ChunkUtils.getChunkCoordinate(position.z());

        // Remove force load
        Instance instance = player.getInstance();
        TicketManager ticketManager = TicketManager.of(instance);

        removeForceLoad(instance, ticketManager, chunkX, chunkZ);
    }

    private void usageRemoveFromTo(CommandSender sender, CommandContext context) {
        Player player = sender.asPlayer();
        RelativeVec fromVec = context.get("from");
        RelativeVec toVec = context.get("to");
        Vec from = fromVec.from(player.getPosition());
        Vec to = toVec.from(player.getPosition());

        int minX = Math.min(from.blockX(), to.blockX());
        int maxX = Math.max(from.blockX(), to.blockX());
        int minZ = Math.min(from.blockZ(), to.blockZ());
        int maxZ = Math.max(from.blockZ(), to.blockZ());

        Instance instance = player.getInstance();
        TicketManager ticketManager = TicketManager.of(instance);

        for (int offX = minX; offX <= maxX; offX += 16) {
            for (int offZ = minZ; offZ <= maxZ; offZ += 16) {
                // Get chunk position
                int chunkX = ChunkUtils.getChunkCoordinate(offX);
                int chunkZ = ChunkUtils.getChunkCoordinate(offZ);

                // Remove the force load
                removeForceLoad(instance, ticketManager, chunkX, chunkZ);
            }
        }
    }

    private void usageRemoveAll(CommandSender sender, CommandContext context) {
        Player player = sender.asPlayer();
        Instance instance = player.getInstance();
        TicketManager ticketManager = TicketManager.of(instance);

        Set<Long> forceLoadedChunks = this.forceLoadedChunks.get(instance);

        if (forceLoadedChunks == null) {
            return;
        }

        for (Long chunk : forceLoadedChunks.toArray(Long[]::new)) { // TODO: Optimize this
            removeForceLoad(instance, ticketManager, chunk);
        }
    }
}
