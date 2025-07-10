package net.minestom.vanilla.blocks.placement;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;

import java.util.HashMap;
import java.util.Map;

public class MushroomPlacementRule extends BlockPlacementRule {
    private final Map<String, BlockFace> faceMap = new HashMap<>();

    public MushroomPlacementRule(Block block) {
        super(block);
        faceMap.put("north", BlockFace.NORTH);
        faceMap.put("south", BlockFace.SOUTH);
        faceMap.put("east", BlockFace.EAST);
        faceMap.put("west", BlockFace.WEST);
        faceMap.put("up", BlockFace.TOP);
        faceMap.put("down", BlockFace.BOTTOM);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        return getState(placementState.instance(), placementState.placePosition(), placementState.block(), true);
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {
        return getState(updateState.instance(), updateState.blockPosition(), updateState.currentBlock(), false);
    }

    private Block getState(Block.Getter instance, Point position, Block currentBlock, boolean isPlacement) {
        Block newBlock = currentBlock;
        for (Map.Entry<String, BlockFace> entry : faceMap.entrySet()) {
            String facePropertyName = entry.getKey();
            BlockFace face = entry.getValue();

            if (isPlacement || "true".equals(newBlock.getProperty(facePropertyName))) {
                Point neighborPos = position.relative(face);
                Block neighborBlock = instance.getBlock(neighborPos);
                boolean shouldConnect = canConnect(currentBlock, neighborBlock);
                String propertyValue = shouldConnect ? "false" : "true";
                newBlock = newBlock.withProperty(facePropertyName, propertyValue);
            }
        }
        return newBlock;
    }

    private boolean canConnect(Block currentBlock, Block neighborBlock) {
        if (neighborBlock.compare(Block.MUSHROOM_STEM)) {
            return true;
        }

        boolean isCurrentBrown = currentBlock.compare(Block.BROWN_MUSHROOM_BLOCK);
        boolean isNeighborBrown = neighborBlock.compare(Block.BROWN_MUSHROOM_BLOCK);
        if (isCurrentBrown) {
            return isNeighborBrown;
        }

        boolean isCurrentRed = currentBlock.compare(Block.RED_MUSHROOM_BLOCK);
        boolean isNeighborRed = neighborBlock.compare(Block.RED_MUSHROOM_BLOCK);
        if (isCurrentRed) {
            return isNeighborRed;
        }
        return false;
    }
}
