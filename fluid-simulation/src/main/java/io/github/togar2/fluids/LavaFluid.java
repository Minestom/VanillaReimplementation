package io.github.togar2.fluids;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import net.minestom.server.utils.Direction;

public class LavaFluid extends FlowableFluid {

    public LavaFluid() {
        super(Block.LAVA, Material.LAVA_BUCKET);
    }

    @Override
    protected boolean isInfinite() {
        return true;
    }

    @Override
    protected boolean onBreakingBlock(Instance instance, Point point, Block block) {
        LavaBlockBreakEvent event = new LavaBlockBreakEvent(instance, point, block);
        return !event.isCancelled();
    }

    @Override
    protected int getHoleRadius(Instance instance) {
        return instance.getDimensionType().isUltrawarm() ? 4 : 2;
    }

    @Override
    public int getLevelDecreasePerBlock(Instance instance) {
        return instance.getDimensionType().isUltrawarm() ? 1 : 2;
    }

    @Override
    public int getTickRate(Instance instance) {
        return instance.getDimensionType().isUltrawarm() ? 10 : 30;
    }

    @Override
    protected boolean canBeReplacedWith(Instance instance, Point point, Fluid other, Direction direction) {
        return direction == Direction.DOWN && this == other;
    }

    @Override
    protected double getBlastResistance() {
        return 100;
    }
}
