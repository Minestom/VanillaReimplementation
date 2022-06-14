package net.minestom.vanilla.blocks.redstone.signal.info;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

public record RedstoneSignalTarget(Instance instance, Point target, Block block) {
}
