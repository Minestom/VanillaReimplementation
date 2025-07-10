package net.minestom.vanilla.blocks.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.registry.RegistryTag;
import net.minestom.server.utils.Direction;
import net.minestom.vanilla.blocks.placement.common.AbstractConnectingBlockPlacementRule;
import net.minestom.vanilla.blocks.placement.util.States;

public class FencePlacementRule extends AbstractConnectingBlockPlacementRule {
    private final RegistryTag<Block> fences = tagManager.getTag(Key.key("minecraft:fences"));
    private final RegistryTag<Block> woodenFences = tagManager.getTag(Key.key("minecraft:wooden_fences"));
    private final RegistryTag<Block> fenceGates = tagManager.getTag(Key.key("minecraft:fence_gates"));

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
