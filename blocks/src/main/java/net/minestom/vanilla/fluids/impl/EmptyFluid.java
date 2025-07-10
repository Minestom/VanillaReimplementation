package net.minestom.vanilla.fluids.impl;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import net.minestom.server.utils.Direction;

/**
 * This file contains code ported from Kotlin to Java, adapted from the Blocks and Stuff project.
 * Original source: https://github.com/everbuild-org/blocks-and-stuff
 * <p>
 * Original authors: ChrisB, AEinNico, CreepyX
 * <p>
 * Ported from Kotlin to Java and adapted for use in this project with modifications.
 */
public class EmptyFluid extends Fluid {

    public EmptyFluid() {
        super(Block.AIR, Material.BUCKET);
    }

    @Override
    public boolean canBeReplacedWith(
        Instance instance,
        Point point,
        Fluid other,
        Direction direction
    ) {
        return true;
    }

    @Override
    public int getNextTickDelay(
        Instance instance,
        Point point,
        Block block
    ) {
        return -1;
    }

    @Override
    protected boolean isEmpty() {
        return true;
    }

    @Override
    protected double getBlastResistance() {
        return 0.0;
    }

    @Override
    public double getHeight(Block block, Instance instance, Point point) {
        return 0.0;
    }

    @Override
    public double getHeight(Block block) {
        return 0.0;
    }

    @Override
    public boolean isInTile(Block block) {
        return false;
    }
}
