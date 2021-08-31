package net.minestom.vanilla.blocks.redstone.signal.info;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

/**
 * A redstone signal.
 *
 * If RedstoneSignalInfo is null, then the redstone signal is being removed
 */
public record RedstoneSignalTarget(
        @NotNull Instance instance,
        @NotNull Point blockPosition,
        @NotNull Block block
) {
}
