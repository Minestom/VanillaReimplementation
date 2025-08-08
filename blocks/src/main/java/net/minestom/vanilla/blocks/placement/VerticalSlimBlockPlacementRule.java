package net.minestom.vanilla.blocks.placement;

import java.util.Map;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.registry.RegistryTag;
import net.minestom.vanilla.blocks.placement.common.AbstractConnectingBlockPlacementRule;

import java.util.ArrayList;
import java.util.Set;
import net.minestom.vanilla.common.utils.BlockUtil;
import net.minestom.vanilla.common.utils.TagHelper;

/**
 * This file contains code ported from Kotlin to Java, adapted from the Blocks and Stuff project.
 * Original source: https://github.com/everbuild-org/blocks-and-stuff
 * <p>
 * Original authors: ChrisB, AEinNico, CreepyX
 * <p>
 * Ported from Kotlin to Java and adapted for use in this project with modifications.
 */
public class VerticalSlimBlockPlacementRule extends AbstractConnectingBlockPlacementRule {

    private final RegistryTag<Block> canConnect = RegistryTag.direct(
        new ArrayList<>() {{
            addAll(TagHelper.getInstance().getHashed("#walls"));
            addAll(BlockUtil.getGlassPanes());
        }}
    );

    public VerticalSlimBlockPlacementRule(Block block) {
        super(block);
    }

    @Override
    public boolean canConnect(Block.Getter instance, Point pos, BlockFace blockFace) {
        Block instanceBlock = instance.getBlock(pos);
        boolean isFaceFull = instanceBlock.registry().collisionShape().isFaceFull(blockFace);
        return (!cannotConnect.contains(instanceBlock) && isFaceFull) || canConnect.contains(instanceBlock) || instanceBlock.key().equals(this.block.key());
    }
}

