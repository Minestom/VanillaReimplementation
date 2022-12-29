package net.minestom.vanilla.generation;

import net.minestom.vanilla.generation.noise.VerticalAnchor;

public interface WorldgenContext {
    int minY();

    int height();

    int maxY();

    static WorldgenContext create(int minY, int height) {
        return new WorldgenContext() {
            @Override
            public int minY() {
                return minY;
            }

            @Override
            public int height() {
                return height;
            }

            @Override
            public int maxY() {
                return minY + height - 1;
            }
        };
    }
}
