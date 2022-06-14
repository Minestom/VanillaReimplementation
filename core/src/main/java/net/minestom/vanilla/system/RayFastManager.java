package net.minestom.vanilla.system;

import dev.emortal.rayfast.area.area3d.Area3d;
import dev.emortal.rayfast.area.area3d.Area3dRectangularPrism;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.entity.Entity;

public class RayFastManager {

    @SuppressWarnings("UnstableApiUsage")
    public static void init() {
        Area3d.CONVERTER.register(Entity.class, box ->
                Area3dRectangularPrism.wrapper(
                        box,
                        entity -> entity.getBoundingBox().minX() + entity.getPosition().x(),
                        entity -> entity.getBoundingBox().minY() + entity.getPosition().y(),
                        entity -> entity.getBoundingBox().minZ() + entity.getPosition().z(),
                        entity -> entity.getBoundingBox().maxX() + entity.getPosition().x(),
                        entity -> entity.getBoundingBox().maxY() + entity.getPosition().y(),
                        entity -> entity.getBoundingBox().maxZ() + entity.getPosition().z()
                )
        );
    }
}
