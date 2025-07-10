package net.minestom.vanilla.common.utils;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;

public class EntityUtils {

    public static Pos eyePosition(Entity entity) {
        if (entity.isSneaking()) {
            return entity.getPosition().add(0.0, 1.23, 0.0);
        }
        return entity.getPosition().add(0.0, 1.53, 0.0);
    }
}
