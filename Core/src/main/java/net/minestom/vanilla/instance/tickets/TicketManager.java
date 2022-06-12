package net.minestom.vanilla.instance.tickets;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ShortMap;
import it.unimi.dsi.fastutil.longs.Long2ShortOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ShortMap;
import it.unimi.dsi.fastutil.objects.Object2ShortOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2IntMap;
import it.unimi.dsi.fastutil.shorts.Short2IntOpenHashMap;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.instance.InstanceChunkLoadEvent;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

/*
Tickets are used to choose when & how to load chunks, when and how to tick entities, and when and how to tick
BlockHandlers.

Tickets range:
Inaccessible: 0 and below -> No game aspects are active, but world generation still occurs.
Border: 1 -> Only some game aspects are active (Redstone and command blocks do not work).
Ticking: 2 -> All game aspects are active except that entities are not processed (do not move) and chunk ticks aren't either.
Entity Ticking: 3 and above -> All game aspects are active.

Vanilla Ticket Value -> vri Ticket Value:
vriTicketValue = 34 - vanillaTicketValue
 */

/**
 * A utility class used to manage instance's tickets
 */
@SuppressWarnings("UnstableApiUsage")
public class TicketManager {

    // TODO: Make tickets objects (trackable interfaces perhaps?)

    // Vanilla ticket values
    public static final short PLAYER_TICKET = 34 - 31;
    public static final short FORCED_TICKET = 34 - 31;
    public static final short START_TICKET = 34 - 22;
    public static final short PORTAL_TICKET = 34 - 30;
    public static final short DRAGON_TICKET = 34 - 24;
    public static final short POST_TELEPORT_TICKET = 34 - 33;
    public static final short SPREAD_PLAYERS_TICKET = 34 - 33;
    public static final short END_PORTAL_TICKET = 34 - 33;
    public static final short TEMPORARY_TICKET = 34 - 33;

    private static final Map<Instance, TicketManager> instance2TicketManager = new WeakHashMap<>();

    /**
     * The first Long is the target chunk
     * The second Long is the source chunk
     * The resulting Short is the value for this chunk
     */
    public final Object2ShortMap<Source2Target> externalTicketValues = new Object2ShortOpenHashMap<>();
    public final Long2ObjectMap<Short2IntMap> internalTicketValues = new Long2ObjectOpenHashMap<>();
    public final Long2ShortMap currentTicketValue = new Long2ShortOpenHashMap();

    private record Source2Target(long source, long target) {
    }


    public static void init(EventNode<Event> eventNode) {
        eventNode.addListener(
            EventListener.of(
                InstanceChunkLoadEvent.class, TicketManager::handleInstanceChunkLoadEvent
            )
        );
    }

    private TicketManager() {
    }

    public static @NotNull TicketManager of(Instance instance) {
        TicketManager manager = instance2TicketManager.get(instance);

        if (manager != null) {
            return manager;
        }

        manager = new TicketManager();

        for (Chunk chunk : instance.getChunks()) {
            manager.prepareChunk(ChunkUtils.getChunkIndex(chunk));
        }

        instance2TicketManager.put(instance, manager);
        return manager;
    }

    public static boolean remove(Instance instance) {
        return instance2TicketManager.remove(instance) != null;
    }

    // Ticket methods

    /**
     * Adds a ticket to the specified chunk and updates surrounding chunks.
     *
     * @param chunk the chunk index of the chunk to add the ticket to
     * @param value the value of the ticket
     */
    public void addTicket(long chunk, short value) {

        // Add value to internal
        Short2IntMap internalValues = internalTicketValues.get(chunk);
        internalValues.putIfAbsent(value, 0);
        internalValues.put(value, internalValues.get(value) + 1);

        externalTicketValues.put(new Source2Target(chunk, chunk), value);
        recalculateChunkValue(chunk);

        // Add values to surrounding external
        short currentSourceValue = value;
        int originX = ChunkUtils.getChunkCoordX(chunk);
        int originZ = ChunkUtils.getChunkCoordZ(chunk);

        while (currentSourceValue > 1) {
            // Find starting positions + starting external value
            int halfWidth = currentSourceValue - 1;
            short externalValue = (short) (value - currentSourceValue + 1);

            // Left starting position
            int leftX = originX - halfWidth;
            int leftZ = originZ + halfWidth;

            // Top starting position
            int topX = originX - halfWidth;
            int topZ = originZ - halfWidth;

            // Right starting position
            int rightX = originX + halfWidth;
            int rightZ = originZ - halfWidth;

            // Down starting position
            int downX = originX + halfWidth;
            int downZ = originZ + halfWidth;

            // Do all squares
            for (int offset = 0; offset < ((halfWidth * 2) + 1); offset++) {

                { // Left side
                    long chunkIndex = ChunkUtils.getChunkIndex(leftX, leftZ - offset);
                    externalTicketValues.put(new Source2Target(chunkIndex, chunk), externalValue);
                    recalculateChunkValue(chunkIndex);
                }

                { // Top side
                    long chunkIndex = ChunkUtils.getChunkIndex(topX + offset, topZ);
                    externalTicketValues.put(new Source2Target(chunkIndex, chunk), externalValue);
                    recalculateChunkValue(chunkIndex);
                }

                { // Right side
                    long chunkIndex = ChunkUtils.getChunkIndex(rightX, rightZ + offset);
                    externalTicketValues.put(new Source2Target(chunkIndex, chunk), externalValue);
                    recalculateChunkValue(chunkIndex);
                }

                { // Downside
                    long chunkIndex = ChunkUtils.getChunkIndex(downX - offset, downZ);
                    externalTicketValues.put(new Source2Target(chunkIndex, chunk), externalValue);
                    recalculateChunkValue(chunkIndex);
                }
            }

            currentSourceValue--;
        }
    }

    /**
     * Removes a ticket from this chunk and updates the surrounding chunks
     * @param chunk the chunk index of the chunk to remove the ticket from
     * @param value the value of the ticket being removed
     */
    public void removeTicket(long chunk, short value) {

        // Remove value from internal
        Short2IntMap internalValues = internalTicketValues.get(chunk);

        if (internalValues.containsKey(value)) {
            int current = internalValues.get(value);

            if (current == 1) {
                internalValues.remove(value);
            } else {
                internalValues.put(value, current - 1);
            }
            return;
        }

        // Remove value from external access into this chunk
        externalTicketValues.removeShort(new Source2Target(chunk, chunk));
        recalculateChunkValue(chunk);

        // Remove values from surrounding external
        short highestInternalValue = 0;

        for (short internalValue : internalValues.keySet()) {
            if (internalValue > highestInternalValue) {
                highestInternalValue = internalValue;
            }
        }

        if (highestInternalValue > 0) {
            externalTicketValues.put(new Source2Target(chunk, chunk), highestInternalValue);
        }

        short previousSourceValue = value;
        int originX = ChunkUtils.getChunkCoordX(chunk);
        int originZ = ChunkUtils.getChunkCoordZ(chunk);

        while (previousSourceValue > 1) {
            // Find starting positions + starting external value
            int halfWidth = previousSourceValue - 1;

            // Left starting position
            int leftX = originX - halfWidth;
            int leftZ = originZ + halfWidth;

            // Top starting position
            int topX = originX - halfWidth;
            int topZ = originZ - halfWidth;

            // Right starting position
            int rightX = originX + halfWidth;
            int rightZ = originZ - halfWidth;

            // Down starting position
            int downX = originX + halfWidth;
            int downZ = originZ + halfWidth;

            // Do all squares
            for (int offset = 0; offset < ((halfWidth * 2) + 1); offset++) {

                { // Left side
                    long chunkIndex = ChunkUtils.getChunkIndex(leftX, leftZ - offset);
                    if (highestInternalValue <= 0) {
                        externalTicketValues.removeShort(new Source2Target(chunkIndex, chunk));
                    } else {
                        externalTicketValues.put(new Source2Target(chunkIndex, chunk), highestInternalValue);
                    }
                    recalculateChunkValue(chunkIndex);
                }

                { // Top side
                    long chunkIndex = ChunkUtils.getChunkIndex(topX + offset, topZ);
                    if (highestInternalValue <= 0) {
                        externalTicketValues.removeShort(new Source2Target(chunkIndex, chunk));
                    } else {
                        externalTicketValues.put(new Source2Target(chunkIndex, chunk), highestInternalValue);
                    }
                    recalculateChunkValue(chunkIndex);
                }

                { // Right side
                    long chunkIndex = ChunkUtils.getChunkIndex(rightX, rightZ + offset);
                    if (highestInternalValue <= 0) {
                        externalTicketValues.removeShort(new Source2Target(chunkIndex, chunk));
                    } else {
                        externalTicketValues.put(new Source2Target(chunkIndex, chunk), highestInternalValue);
                    }
                    recalculateChunkValue(chunkIndex);
                }

                { // Downside
                    long chunkIndex = ChunkUtils.getChunkIndex(downX - offset, downZ);
                    if (highestInternalValue <= 0) {
                        externalTicketValues.removeShort(new Source2Target(chunkIndex, chunk));
                    } else {
                        externalTicketValues.put(new Source2Target(chunkIndex, chunk), highestInternalValue);
                    }
                    recalculateChunkValue(chunkIndex);
                }
            }

            previousSourceValue--;
            highestInternalValue--;
        }
    }

    /**
     * Gets the ticket value of the specified chunk.
     *
     * @param chunkIndex the chunk index of the chunk to retrieve the ticket value from
     * @return the ticket value
     */
    public short getTicketValue(long chunkIndex) {
        return currentTicketValue.get(chunkIndex);
    }

    /**
     * Gets information on the tickets for this specified chunk
     *
     * @param chunkIndex the chunk index of the chunk to retrieve the ticket info from
     * @return the ticket value
     */
    public String getChunkInfo(long chunkIndex) {

        return "Current Value: " + currentTicketValue.get(chunkIndex) + "\n"
             + "Internal Tickets: " + internalTicketValues.get(chunkIndex) + "\n"
             + "External Tickets: " + " (" + externalTicketValues.object2ShortEntrySet()
                .stream()
                .filter(entry -> entry.getKey().target() == chunkIndex)
                .map(String::valueOf)
                .collect(Collectors.joining(", ")) + " )";
    }

    private static void handleInstanceChunkLoadEvent(InstanceChunkLoadEvent event) {
        Instance instance = event.getInstance();
        int chunkX = event.getChunkX();
        int chunkZ = event.getChunkZ();
        long chunkIndex = ChunkUtils.getChunkIndex(chunkX, chunkZ);
        TicketManager ticketManager = TicketManager.of(instance);

        Chunk chunk = instance.getChunk(chunkX, chunkZ);

        if (chunk == null) {
            throw new IllegalStateException("Chunk coords passed to InstanceChunkLoadEvent did not have a loaded chunk.");
        }

        // Ensure chunk has ticket value set & tickets cache initialised
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (ticketManager) {
            ticketManager.prepareChunk(chunkIndex);
        }
    }

    private void prepareChunk(long chunk) {
        currentTicketValue.computeIfAbsent(chunk, ignored -> (short) 0);
        internalTicketValues.computeIfAbsent(chunk, k -> new Short2IntOpenHashMap());
    }

    private static class MutableShort {
        private short value;
    }

    private void recalculateChunkValue(long chunkIndex) {
        prepareChunk(chunkIndex);

        MutableShort highest = new MutableShort();
        highest.value = 0;

        externalTicketValues.forEach((source2Target, value) -> {
            if (source2Target.target() == chunkIndex) {
                if (value > highest.value) {
                    highest.value = value;
                }
            }
        });

        // Set new value
        this.currentTicketValue.put(chunkIndex, highest.value);
    }
}