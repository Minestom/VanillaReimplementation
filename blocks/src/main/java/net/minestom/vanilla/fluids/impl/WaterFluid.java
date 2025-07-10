package net.minestom.vanilla.fluids.impl;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import net.minestom.server.utils.Direction;
import net.minestom.vanilla.fluids.FluidUtils;

/**
 * This file contains code ported from Kotlin to Java, adapted from the Blocks and Stuff project.
 * Original source: https://github.com/everbuild-org/blocks-and-stuff
 * <p>
 * Original authors: ChrisB, AEinNico, CreepyX
 * <p>
 * Ported from Kotlin to Java and adapted for use in this project with modifications.
 */
public class WaterFluid extends FlowableFluid {

    public WaterFluid(Block defaultBlock, Material bucket) {
        super(defaultBlock, bucket);
    }

    @Override
    protected boolean isInfinite() {
        return true;
    }

    @Override
    public int getNextTickDelay(Instance instance, Point point, Block block) {
        return FluidUtils.getRelativeTicks(5);
    }

    @Override
    protected int getHoleRadius(Instance instance) {
        return 4;
    }

    @Override
    protected int getLevelDecreasePerBlock(Instance instance) {
        return 1;
    }

    @Override
    public double getHeight(Block block, Instance instance, Point point) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    protected double getBlastResistance() {
        return 100.0;
    }

    @Override
    public boolean canBeReplacedWith(Instance instance, Point point, Fluid other, Direction direction) {
        return direction == Direction.DOWN && this == other;
    }

    @Override
    public boolean isInTile(Block block) {
        return super.isInTile(block) || "true".equals(block.getProperty("waterlogged"));
    }
}
