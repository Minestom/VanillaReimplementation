package net.minestom.vanilla.blocks.placement;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.registry.RegistryTag;
import net.minestom.vanilla.blocks.placement.common.AbstractConnectingBlockPlacementRule;
import net.minestom.vanilla.common.tag.BlockTags;

import java.util.ArrayList;
import java.util.Set;

/**
 * This file contains code ported from Kotlin to Java, adapted from the Blocks and Stuff project.
 * Original source: https://github.com/everbuild-org/blocks-and-stuff
 * <p>
 * Original authors: ChrisB, AEinNico, CreepyX
 * <p>
 * Ported from Kotlin to Java and adapted for use in this project with modifications.
 */
public class VerticalSlimBlockPlacementRule extends AbstractConnectingBlockPlacementRule {

    private final Set<Block> glassPanes = BlockTags.getInstance().getTaggedWith("vri:glass_panes");

    private final RegistryTag<Block> canConnect = RegistryTag.direct(
        new ArrayList<>() {{
            addAll(Block.values().stream().filter(it -> it.name().endsWith("_wall")).toList());
            addAll(glassPanes);
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

