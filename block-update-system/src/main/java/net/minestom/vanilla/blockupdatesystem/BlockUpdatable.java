package net.minestom.vanilla.blockupdatesystem;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

public interface BlockUpdatable {
    /**
     * Called when a block is updated.
     *
     * @param info The block update info.
     */
    void blockUpdate(@NotNull Instance instance, @NotNull Point pos, @NotNull BlockUpdateInfo info);
}
