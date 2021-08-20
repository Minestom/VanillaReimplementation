package net.minestom.vanilla.system;

import dev.emortal.rayfast.area.area3d.Area3d;
import dev.emortal.rayfast.area.area3d.Area3dRectangularPrism;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.entity.Entity;

public class RayFastManager {
    public static void init() {
        Area3d.CONVERTER.register(BoundingBox.class, box ->
                new Area3dRectangularPrism(
                        box.getMinX(), box.getMinY(), box.getMinZ(),
                        box.getMaxX(), box.getMaxY(), box.getMaxZ()
                )
        );

        Area3d.CONVERTER.register(Entity.class, entity ->
                Area3d.CONVERTER.from(entity.getBoundingBox())
        );
    }
}
