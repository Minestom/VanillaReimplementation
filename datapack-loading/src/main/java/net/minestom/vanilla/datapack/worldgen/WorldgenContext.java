package net.minestom.vanilla.datapack.worldgen;

import net.minestom.server.world.DimensionType;

public interface WorldgenContext {

    static WorldgenContext create(DimensionType dimension) {
        return () -> dimension;
    }

    default int minY() {
        return dimension().minY();
    }
    default int maxY() {
        return dimension().maxY();
    }

    DimensionType dimension();
}
