package net.minestom.vanilla.blocks.placement;

import java.util.Set;
import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.registry.RegistryTag;
import net.minestom.server.utils.Direction;
import net.minestom.vanilla.blocks.placement.common.AbstractConnectingBlockPlacementRule;
import net.minestom.vanilla.blocks.placement.util.States;
import net.minestom.vanilla.common.utils.TagHelper;

/**
 * This file contains code ported from Kotlin to Java, adapted from the Blocks and Stuff project.
 * Original source: https://github.com/everbuild-org/blocks-and-stuff
 * <p>
 * Original authors: ChrisB, AEinNico, CreepyX
 * <p>
 * Ported from Kotlin to Java and adapted for use in this project with modifications.
 */
public class FencePlacementRule extends AbstractConnectingBlockPlacementRule {

    private final Set<Block> fences = TagHelper.getInstance().getTaggedWith("minecraft:fences");
    private final Set<Block> woodenFences = TagHelper.getInstance().getTaggedWith("minecraft:wooden_fences");
    private final Set<Block> fenceGates = TagHelper.getInstance().getTaggedWith("minecraft:fence_gates");

    public FencePlacementRule(Block block) {
        super(block);
    }

    @Override
    public boolean canConnect(Block.Getter instance, Point pos, BlockFace blockFace) {
        Block instanceBlock = instance.getBlock(pos);
        boolean isBlockNetherBrickFence = block.name().endsWith("_brick_fence");
        boolean isInstanceBlockNetherBrickFence = instanceBlock.name().endsWith("_brick_fence");
        boolean canConnectToFence = canConnectToFence(instanceBlock);

        Direction blockFaceDirection = blockFace.toDirection();
        Direction rotatedDirection = States.rotateYClockwise(blockFaceDirection);

        boolean canFenceGateConnect = fenceGates.contains(instanceBlock) &&
            States.getAxis(States.getFacing(instanceBlock).toDirection()) ==
            States.getAxis(rotatedDirection);

        boolean isFaceFull = instanceBlock.registry().collisionShape().isFaceFull(blockFace);

        return (!cannotConnect.contains(instanceBlock) && isFaceFull) ||
               (canConnectToFence && !isBlockNetherBrickFence) ||
               canFenceGateConnect ||
               (isBlockNetherBrickFence && isInstanceBlockNetherBrickFence);
    }

    private boolean canConnectToFence(Block block) {
        boolean isFence = fences.contains(block);
        boolean isWoodenFence = woodenFences.contains(block);
        return isFence && isWoodenFence;
    }
}
