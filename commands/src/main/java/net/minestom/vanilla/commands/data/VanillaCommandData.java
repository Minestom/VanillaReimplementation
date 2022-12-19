package net.minestom.vanilla.commands.data;

import net.minestom.server.command.CommandSender;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

public record VanillaCommandData(@NotNull CommandSender commandSender, @NotNull Pos position,
                                 @NotNull Instance instance, @NotNull Anchor anchor) {

    public VanillaCommandData with(@NotNull CommandSender sender) {
        return new VanillaCommandData(sender, position, instance, anchor);
    }

    public Stream<VanillaCommandData> mul(@NotNull CommandSender... senders) {
        return Stream.of(senders).map(this::with).distinct(); // TODO: Should use distinct?
    }

    public VanillaCommandData with(@NotNull Pos position) {
        return new VanillaCommandData(commandSender, position, instance, anchor);
    }

    public Stream<VanillaCommandData> mul(@NotNull Pos... positions) {
        return Stream.of(positions).map(this::with).distinct(); // TODO: Should use distinct?
    }

    public VanillaCommandData with(@NotNull Instance instance) {
        return new VanillaCommandData(commandSender, position, instance, anchor);
    }

    public Stream<VanillaCommandData> mul(@NotNull Instance... instances) {
        return Stream.of(instances).map(this::with).distinct(); // TODO: Should use distinct?
    }

    public VanillaCommandData with(@NotNull Anchor anchor) {
        return new VanillaCommandData(commandSender, position, instance, anchor);
    }

    public Stream<VanillaCommandData> mul(@NotNull Anchor... anchors) {
        return Stream.of(anchors).map(this::with).distinct(); // TODO: Should use distinct?
    }

    public VanillaCommandData with(@NotNull Object... elements) {
        CommandSender sender = this.commandSender;
        Pos position = this.position;
        Instance instance = this.instance;
        Anchor anchor = this.anchor;
        for (Object element : elements) {
            if (element instanceof CommandSender) {
                sender = (CommandSender) element;
            } else if (element instanceof Pos) {
                position = (Pos) element;
            } else if (element instanceof Instance) {
                instance = (Instance) element;
            } else if (element instanceof Anchor) {
                anchor = (Anchor) element;
            } else {
                throw new IllegalArgumentException("Unknown element type: " + element.getClass());
            }
        }
        return new VanillaCommandData(sender, position, instance, anchor);
    }
}
