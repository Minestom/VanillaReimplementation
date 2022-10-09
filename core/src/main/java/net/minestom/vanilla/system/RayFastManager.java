package net.minestom.vanilla.system;

import dev.emortal.rayfast.area.area3d.Area3d;
import dev.emortal.rayfast.area.area3d.Area3dRectangularPrism;
import net.minestom.server.entity.Entity;

public class RayFastManager {

    @SuppressWarnings("UnstableApiUsage")
    public static void init() {
        Area3d.CONVERTER.register(Entity.class, entity ->
                Area3dRectangularPrism.wrapper(
                        entity,
                        ignored -> entity.getBoundingBox().minX() + entity.getPosition().x(),
                        ignored -> entity.getBoundingBox().minY() + entity.getPosition().y(),
                        ignored -> entity.getBoundingBox().minZ() + entity.getPosition().z(),
                        ignored -> entity.getBoundingBox().maxX() + entity.getPosition().x(),
                        ignored -> entity.getBoundingBox().maxY() + entity.getPosition().y(),
                        ignored -> entity.getBoundingBox().maxZ() + entity.getPosition().z()
                )
        );
    }
}
