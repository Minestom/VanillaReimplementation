package net.minestom.vanilla.instance.tickets;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.instance.InstanceChunkLoadEvent;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;

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
    // Vanilla ticket values
    public static final Short PLAYER_TICKET = 34 - 31;
    public static final Short FORCED_TICKET = 34 - 31;
    public static final Short START_TICKET = 34 - 22;
    public static final Short PORTAL_TICKET = 34 - 30;
    public static final Short DRAGON_TICKET = 34 - 24;
    public static final Short POST_TELEPORT_TICKET = 34 - 33;
    public static final Short SPREAD_PLAYERS_TICKET = 34 - 33;
    public static final Short END_PORTAL_TICKET = 34 - 33;
    public static final Short TEMPORARY_TICKET = 34 - 33;


    // IDs
    public static final Tag<Long> ID = Tag.Long("minestom:ticket_manager_id");
    private static long nextID = 0;
    private static final Map<Long, TicketManager> blockUpdateManagerById = new WeakHashMap<>();


    /**
     * The first Long is the target chunk
     * The second Long is the source chunk
     * The resulting Short is the value for this chunk
     */
    public final Table<Long, Long, Short> externalTicketValues = HashBasedTable.create();
    public final Map<Long, List<Short>> internalTicketValues = new HashMap<>();
    public final Map<Long, Short> currentTicketValue = new HashMap<>();


    public static void init(EventNode<Event> eventNode) {
        eventNode.addListener(
                EventListener.of(
                        InstanceChunkLoadEvent.class, TicketManager::handleInstanceChunkLoadEvent
                )
        );
    }

    private TicketManager(@NotNull Instance instance) {
        long id = nextID++;
        instance.setTag(ID, id);
        blockUpdateManagerById.put(id, this);
    }

    public static @NotNull TicketManager of(Instance instance) {
        Long id = instance.getTag(ID);

        if (id == null) {
            return new TicketManager(instance);
        }

        return blockUpdateManagerById.get(id);
    }

    public static boolean remove(Instance instance) {
        Long id = instance.getTag(ID);

        if (id == null) {
            throw new IllegalArgumentException("Argument 'instance' passed to BlockUpdateManager#remove did not have an associated block update manager.");
        }

        instance.setTag(ID, null);

        return blockUpdateManagerById.remove(id) != null;
    }

    // Ticket methods

    /**
     * Adds a ticket to the specified chunk and updates surrounding chunks.
     *
     * @param chunk the chunk index of the chunk to add the ticket to
     * @param value the value of the ticket
     */
    public void addTicket(@NotNull Long chunk, @NotNull Short value) {

        // Add value to internal
        List<Short> internalValues = internalTicketValues.get(chunk);
        internalValues.add(value);
        externalTicketValues.put(chunk, chunk, value);
        recalculateChunkValue(chunk);

        // Add values to surrounding external
        short currentSourceValue = value;
        int originX = ChunkUtils.getChunkCoordX(chunk);
        int originZ = ChunkUtils.getChunkCoordZ(chunk);

        while (currentSourceValue > 1) {
            // Find starting positions + starting external value
            int halfWidth = currentSourceValue - 1;
            Short externalValue = (short) (value - currentSourceValue + 1);

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
                    externalTicketValues.put(chunkIndex, chunk, externalValue);
                    recalculateChunkValue(chunkIndex);
                }

                { // Top side
                    long chunkIndex = ChunkUtils.getChunkIndex(topX + offset, topZ);
                    externalTicketValues.put(chunkIndex, chunk, externalValue);
                    recalculateChunkValue(chunkIndex);
                }

                { // Right side
                    long chunkIndex = ChunkUtils.getChunkIndex(rightX, rightZ + offset);
                    externalTicketValues.put(chunkIndex, chunk, externalValue);
                    recalculateChunkValue(chunkIndex);
                }

                { // Downside
                    long chunkIndex = ChunkUtils.getChunkIndex(downX - offset, downZ);
                    externalTicketValues.put(chunkIndex, chunk, externalValue);
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
    public void removeTicket(@NotNull Long chunk, @NotNull Short value) {

        // Remove value from internal
        List<Short> internalValues = internalTicketValues.get(chunk);

        if (!internalValues.remove(value)) {
            return;
        }

        // Remove value from external access into this chunk
        externalTicketValues.remove(chunk, chunk);
        recalculateChunkValue(chunk);

        // Remove values from surrounding external
        Short highestInternalValue = 0;

        for (Short internalValue : internalValues) {
            if (internalValue > highestInternalValue) {
                highestInternalValue = internalValue;
            }
        }

        if (highestInternalValue > 0) {
            externalTicketValues.put(chunk, chunk, highestInternalValue);
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
                        externalTicketValues.remove(chunkIndex, chunk);
                    } else {
                        externalTicketValues.put(chunkIndex, chunk, highestInternalValue);
                    }
                    recalculateChunkValue(chunkIndex);
                }

                { // Top side
                    long chunkIndex = ChunkUtils.getChunkIndex(topX + offset, topZ);
                    if (highestInternalValue <= 0) {
                        externalTicketValues.remove(chunkIndex, chunk);
                    } else {
                        externalTicketValues.put(chunkIndex, chunk, highestInternalValue);
                    }
                    recalculateChunkValue(chunkIndex);
                }

                { // Right side
                    long chunkIndex = ChunkUtils.getChunkIndex(rightX, rightZ + offset);
                    if (highestInternalValue <= 0) {
                        externalTicketValues.remove(chunkIndex, chunk);
                    } else {
                        externalTicketValues.put(chunkIndex, chunk, highestInternalValue);
                    }
                    recalculateChunkValue(chunkIndex);
                }

                { // Downside
                    long chunkIndex = ChunkUtils.getChunkIndex(downX - offset, downZ);
                    if (highestInternalValue <= 0) {
                        externalTicketValues.remove(chunkIndex, chunk);
                    } else {
                        externalTicketValues.put(chunkIndex, chunk, highestInternalValue);
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
             + "External Tickets: " + externalTicketValues.row(chunkIndex);
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
        ticketManager.prepareChunk(chunkIndex);
    }

    private void prepareChunk(Long chunk) {
        currentTicketValue.computeIfAbsent(chunk, k -> (short) 0);
        internalTicketValues.computeIfAbsent(chunk, k -> new LinkedList<>());
    }

    private void recalculateChunkValue(long chunkIndex) {
        prepareChunk(chunkIndex);

        // Get all values of this chunk
        Map<Long, Short> externalValues = this.externalTicketValues.row(chunkIndex);

        // Find highest
        Short highest = 0;

        for (Short value : externalValues.values()) {
            if (value > highest) {
                highest = value;
            }
        }

        // Set new value
        this.currentTicketValue.put(chunkIndex, highest);
    }
}