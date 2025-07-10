package net.minestom.vanilla.fluids.impl;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import net.minestom.server.utils.Direction;
import net.minestom.vanilla.fluids.FluidUtils;

public class LavaFluid extends FlowableFluid {

    public LavaFluid(Block defaultBlock, Material bucket) {
        super(defaultBlock, bucket);
    }

    @Override
    protected boolean isInfinite() {
        return false;
    }

    @Override
    protected int getHoleRadius(Instance instance) {
        return 4;
    }

    @Override
    protected int getLevelDecreasePerBlock(Instance instance) {
        return 2;
    }

    @Override
    public int getNextTickDelay(Instance instance, Point point, Block block) {
        boolean isUltrawarm = MinecraftServer.getDimensionTypeRegistry().get(instance.getDimensionType()).ultrawarm();
        return isUltrawarm ?
            FluidUtils.getRelativeTicks(10) :
            FluidUtils.getRelativeTicks(15);
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
}
