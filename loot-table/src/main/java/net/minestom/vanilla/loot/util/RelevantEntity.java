package net.minestom.vanilla.loot.util;

import net.minestom.server.codec.Codec;
import net.minestom.server.entity.Entity;
import net.minestom.vanilla.loot.LootContext;
import org.jetbrains.annotations.NotNull;

public enum RelevantEntity {
    THIS("this", LootContext.THIS_ENTITY),
    ATTACKER("attacker", LootContext.ATTACKING_ENTITY),
    DIRECT_ATTACKER("direct_attacker", LootContext.DIRECT_ATTACKING_ENTITY),
    LAST_PLAYER_DAMAGE("killer_player", LootContext.LAST_DAMAGE_PLAYER);

    @SuppressWarnings("UnstableApiUsage")
    public static final @NotNull Codec<RelevantEntity> CODEC = Codec.Enum(RelevantEntity.class); // Relies on the enum names themselves being accurate

    private final @NotNull String id;
    private final @NotNull LootContext.Key<? extends Entity> key;

    RelevantEntity(@NotNull String id, @NotNull LootContext.Key<? extends Entity> key) {
        this.id = id;
        this.key = key;
    }

    public @NotNull String id() {
        return id;
    }

    public @NotNull LootContext.Key<? extends Entity> key() {
        return key;
    }

}
