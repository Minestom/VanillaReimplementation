package net.minestom.vanilla.commands.data;

import net.minestom.server.command.builder.arguments.Argument;

import static net.minestom.server.command.builder.arguments.ArgumentType.Enum;

public enum Anchor {
    feet, eyes,
    ;

    public static final Anchor DEFAULT = feet;
    public static final Argument<Anchor> ARGUMENT = Enum("anchor", Anchor.class).setDefaultValue(DEFAULT);
}
