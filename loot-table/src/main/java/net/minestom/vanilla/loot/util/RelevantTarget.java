package net.minestom.vanilla.loot.util;

import net.minestom.server.codec.Codec;
import net.minestom.vanilla.loot.LootContext;
import org.jetbrains.annotations.NotNull;

public enum RelevantTarget {
    THIS("this", LootContext.THIS_ENTITY),
    ATTACKING_ENTITY("attacking_entity", LootContext.ATTACKING_ENTITY),
    LAST_DAMAGE_PLAYER("last_damage_player", LootContext.LAST_DAMAGE_PLAYER),
    BLOCK_ENTITY("block_entity", LootContext.BLOCK_STATE);

    @SuppressWarnings("UnstableApiUsage")
    public static final Codec<RelevantTarget> CODEC = Codec.Enum(RelevantTarget.class); // Relies on the enum names themselves being accurate

    private final @NotNull String id;
    private final @NotNull LootContext.Key<?> key;

    RelevantTarget(@NotNull String id, @NotNull LootContext.Key<?> key) {
        this.id = id;
        this.key = key;
    }

    public @NotNull String id() {
        return id;
    }

    public @NotNull LootContext.Key<?> key() {
        return key;
    }
}