package net.minestom.vanilla.blocks.placement;

import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.vanilla.common.item.DroppedItemFactory;
import net.minestom.vanilla.common.tag.BlockTags;

import java.util.Set;

public class BambooPlantPlacementRule extends BlockPlacementRule {
    private final Set<Block> plantableOn;
    private final Set<Block> bamboo;

    public BambooPlantPlacementRule(Block block) {
        super(block);
        this.plantableOn = BlockTags.getInstance().getTaggedWith("minecraft:bamboo_plantable_on");
        this.bamboo = BlockTags.getInstance().getTaggedWith("blocksandstuff:bamboo");
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        var positionBelow = placementState.placePosition().sub(0.0, 1.0, 0.0);
        var blockBelow = placementState.instance().getBlock(positionBelow);
        var instance = (Instance) placementState.instance();

        for (Block bambooPart : bamboo) {
            if (blockBelow.compare(bambooPart)) {
                if (blockBelow.compare(Block.BAMBOO_SAPLING)) {
                    instance.setBlock(positionBelow, Block.BAMBOO);
                }
                return placementState.block();
            }
        }

        for (Block plantable : plantableOn) {
            if (blockBelow.compare(plantable)) {
                return Block.BAMBOO_SAPLING;
            }
        }

        return null;
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {
        var below = updateState.instance().getBlock(updateState.blockPosition().sub(0.0, 1.0, 0.0));

        boolean canStay = false;
        for (Block plantable : plantableOn) {
            if (plantable.compare(below)) {
                canStay = true;
                break;
            }
        }

        if (!canStay) {
            DroppedItemFactory.maybeDrop(updateState);
            return Block.AIR;
        }

        return updateState.currentBlock();
    }

    @Override
    public int maxUpdateDistance() {
        return 500;
    }
}
