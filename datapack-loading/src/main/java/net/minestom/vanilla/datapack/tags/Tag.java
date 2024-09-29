package net.minestom.vanilla.datapack.tags;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

public record Tag(String namespace, String value) implements Key {
    public Tag(String string) {
        this(string.split(":")[0], string.split(":")[1]);
    }

    @Override
    public @NotNull String asString() {
        return namespace + ":" + value;
    }
}
