package net.minestom.vanilla.instancemeta.tickets;

import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class TicketUtils {

    public static @NotNull List<TicketManager.Ticket> waitingTickets(@NotNull Instance instance) {
        return instance.getTag(TicketManager.WAITING_TICKETS_TAG);
    }

    public static void waitingTickets(@NotNull Instance instance, @NotNull Collection<TicketManager.Ticket> ticketsToAdd) {
        List<TicketManager.Ticket> newWaitingTickets = Stream.concat(
                waitingTickets(instance).stream(),
                ticketsToAdd.stream()
            ).toList();
        instance.setTag(TicketManager.WAITING_TICKETS_TAG, newWaitingTickets);
    }

    public static @NotNull List<TicketManager.Ticket> removingTickets(Instance instance) {
        return instance.getTag(TicketManager.REMOVING_TICKETS_TAG);
    }

    public static void removingTickets(Instance instance, @NotNull Collection<TicketManager.Ticket> from) {
        List<TicketManager.Ticket> newRemovingTickets = Stream.concat(
                removingTickets(instance).stream(),
                from.stream()
            ).toList();
        instance.setTag(TicketManager.REMOVING_TICKETS_TAG, newRemovingTickets);
    }
}
