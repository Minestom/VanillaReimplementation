package net.minestom.vanilla.commands.data;

import net.minestom.server.command.builder.arguments.Argument;

import static net.minestom.server.command.builder.arguments.ArgumentType.Enum;

public enum Swizzle {
    x, y, z,
    xy, xz, yx, yz, zx, zy,
    xyz, xzy, yxz, yzx, zxy, zyx;

    public final boolean has_x;
    public final boolean has_y;
    public final boolean has_z;

    Swizzle() {
        has_x = name().contains("x");
        has_y = name().contains("y");
        has_z = name().contains("z");
    }

    public static final Argument<Swizzle> ARGUMENT = Enum("swizzle", Swizzle.class).setDefaultValue(Swizzle.xyz);
}
