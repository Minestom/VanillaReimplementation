package net.minestom.vanilla;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;

import java.util.UUID;

public class Entities {

    /**
     * Spawns an entity at the given position with the specified type and default data.
     * @param type the type of the entity to spawn
     * @param pos the position where the entity should be spawned
     * @return the spawned entity
     */
    public static Entity spawnEntity(EntityType type, Instance instance, Pos pos) {
        return spawnEntity(type, instance, pos, CompoundBinaryTag.empty());
    }

    /**
     * Spawns an entity at the given position with the specified type and entity data.
     *
     * @param type the type of the entity to spawn
     * @param pos the position where the entity should be spawned
     * @param entityData the data to set on the entity, can be empty
     * @return the spawned entity
     */
    public static Entity spawnEntity(EntityType type, Instance instance, Pos pos, CompoundBinaryTag entityData) {
        //TODO: Properly handle entity data
        UUID uuid;
        int[] savedUUID = entityData.getIntArray("uuid");
        if (savedUUID.length == 4) {
            uuid = new UUID(
                    ((long) savedUUID[0] << 32) | (savedUUID[1] & 0xFFFFFFFFL),
                    ((long) savedUUID[2] << 32) | (savedUUID[3] & 0xFFFFFFFFL)
            );
        } else {
            uuid = UUID.randomUUID();
        }

        Entity entity = new Entity(type, uuid);
        entity.setInstance(instance, pos);

        return entity;
    }

}
