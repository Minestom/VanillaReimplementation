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
                        entity -> entity.getBoundingBox().minX(),
                        entity -> entity.getBoundingBox().minY(),
                        entity -> entity.getBoundingBox().minZ(),
                        entity -> entity.getBoundingBox().maxX(),
                        entity -> entity.getBoundingBox().maxY(),
                        entity -> entity.getBoundingBox().maxZ()
                )
        );
    }
}
