package net.minestom.vanilla.fluids;

import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.coordinate.Point;

public class FluidPlacementRule extends BlockPlacementRule {

    public FluidPlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        if (placementState.blockFace() == null) {
            throw new IllegalStateException("BlockFace cannot be null");
        }

        Point placedPoint = placementState.placePosition(); // The actual placed position
        MinestomFluids.scheduleTick(
            (Instance) placementState.instance(),
            placedPoint,
            block
        ); // Schedule update for placed block

        System.out.println("Placing block at " + placedPoint);
        return block;
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {
        Instance instance = (Instance) updateState.instance();
        Point point = updateState.blockPosition();
        Block block = updateState.currentBlock();

        // Schedule update for the current block
        MinestomFluids.scheduleTick(instance, point, block);

        // Schedule updates for adjacent blocks (ensures fluid spreads properly)
        for (BlockFace face : BlockFace.values()) {
            Point neighbor = point.relative(face);
            Block neighborBlock = instance.getBlock(neighbor);
            if (MinestomFluids.getFluidOnBlock(neighborBlock) != MinestomFluids.EMPTY) {
                MinestomFluids.scheduleTick(instance, neighbor, neighborBlock);
            }
        }
        return block;
    }
}
