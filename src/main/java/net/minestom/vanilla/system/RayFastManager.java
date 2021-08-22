package net.minestom.vanilla.system;

import dev.emortal.rayfast.area.area3d.Area3d;
import dev.emortal.rayfast.area.area3d.Area3dRectangularPrism;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.entity.Entity;

public class RayFastManager {
    public static void init() {
        Area3d.CONVERTER.register(BoundingBox.class, box ->
                Area3dRectangularPrism.wrapper(
                        box,
                        BoundingBox::getMinX,
                        BoundingBox::getMinY,
                        BoundingBox::getMinZ,
                        BoundingBox::getMaxX,
                        BoundingBox::getMaxY,
                        BoundingBox::getMaxZ
                )
        );

        Area3d.CONVERTER.register(Entity.class, entity ->
                Area3d.CONVERTER.from(entity.getBoundingBox())
        );
    }
}
