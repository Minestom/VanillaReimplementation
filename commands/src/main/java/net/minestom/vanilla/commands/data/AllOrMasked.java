package net.minestom.vanilla.commands.data;

import net.minestom.server.command.builder.arguments.Argument;

import static net.minestom.server.command.builder.arguments.ArgumentType.Enum;

public enum AllOrMasked {
    all, masked,
    ;
    public static final Argument<AllOrMasked> ARGUMENT = Enum("allOrMasked", AllOrMasked.class)
            .setDefaultValue(AllOrMasked.all);
}
