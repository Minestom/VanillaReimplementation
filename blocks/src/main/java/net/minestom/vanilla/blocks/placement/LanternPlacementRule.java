package net.minestom.vanilla.blocks.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.vanilla.common.item.DroppedItemFactory;

import java.util.HashSet;
import java.util.Set;

public class LanternPlacementRule extends BlockPlacementRule {
    private static final Set<String> SPECIAL_SUPPORT_BLOCKS = new HashSet<>();

    static {
        SPECIAL_SUPPORT_BLOCKS.add("minecraft:chain");
        SPECIAL_SUPPORT_BLOCKS.add("minecraft:iron_bars");
    }

    public LanternPlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        Block blockBelow = placementState.instance().getBlock(placementState.placePosition().add(0.0, -1.0, 0.0));
        Block blockAbove = placementState.instance().getBlock(placementState.placePosition().add(0.0, 1.0, 0.0));
        boolean canStandOnBlock = canSupport(blockBelow, BlockFace.TOP);
        boolean canHangFromBlock = canSupport(blockAbove, BlockFace.BOTTOM);

        if (canHangFromBlock && (placementState.blockFace() == BlockFace.BOTTOM)) {
            return placementState.block().withProperty("hanging", "true");
        } else if (canStandOnBlock) {
            return placementState.block().withProperty("hanging", "false");
        } else if (canHangFromBlock) {
            return placementState.block().withProperty("hanging", "true");
        } else {
            return null;
        }
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {
        Block currentBlock = updateState.currentBlock();
        String hangingProperty = currentBlock.getProperty("hanging");
        boolean isHanging = hangingProperty != null && hangingProperty.equals("true");

        Block supportBlock;
        if (isHanging) {
            supportBlock = updateState.instance().getBlock(updateState.blockPosition().add(0.0, 1.0, 0.0));
        } else {
            supportBlock = updateState.instance().getBlock(updateState.blockPosition().add(0.0, -1.0, 0.0));
        }

        BlockFace requiredFace = isHanging ? BlockFace.BOTTOM : BlockFace.TOP;
        if (!canSupport(supportBlock, requiredFace)) {
            DroppedItemFactory.maybeDrop(updateState);
            return Block.AIR;
        }
        return currentBlock;
    }

    private boolean canSupport(Block supportBlock, BlockFace requiredFace) {
        if (supportBlock.registry().collisionShape().isFaceFull(requiredFace)) {
            return true;
        }

        String blockName = supportBlock.name();
        if ("minecraft:chain".equals(blockName)) {
            return "y".equals(supportBlock.getProperty("axis"));
        } else if ("minecraft:iron_bars".equals(blockName)) {
            return true;
        } else {
            return blockName.contains("glass_pane");
        }
    }

    private static boolean isSpecialSupportBlock(String blockName) {
        return SPECIAL_SUPPORT_BLOCKS.contains(blockName) || blockName.contains("glass_pane");
    }
}
