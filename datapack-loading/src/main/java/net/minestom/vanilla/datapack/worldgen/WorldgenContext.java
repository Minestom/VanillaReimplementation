package net.minestom.vanilla.datapack.worldgen;

import net.minestom.server.world.DimensionType;

public interface WorldgenContext {

    static WorldgenContext create(DimensionType dimension) {
        return () -> dimension;
    }

    default int minY() {
        return dimension().getMinY();
    }
    default int maxY() {
        return dimension().getMaxY();
    }

    DimensionType dimension();
}
