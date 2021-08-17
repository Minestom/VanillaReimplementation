package net.minestom.vanilla.entity;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ZombieEntity extends LivingEntity {

    public ZombieEntity() {
        this(UUID.randomUUID());
    }

    public ZombieEntity(@NotNull UUID uuid) {
        super(EntityType.ZOMBIE, uuid);
    }
}
