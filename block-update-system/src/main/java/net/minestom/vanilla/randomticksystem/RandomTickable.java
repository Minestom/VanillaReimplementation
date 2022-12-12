package net.minestom.vanilla.randomticksystem;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

public interface RandomTickable {

    void randomTick(@NotNull RandomTick randomTick);

    interface RandomTick {
        @NotNull Instance instance();
        @NotNull Point position();
        @NotNull Block block();
    }
}
