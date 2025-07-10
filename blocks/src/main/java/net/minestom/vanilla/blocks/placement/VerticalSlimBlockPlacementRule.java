package net.minestom.vanilla.blocks.placement;

import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.registry.Registry;
import net.minestom.server.registry.RegistryTag;
import net.minestom.vanilla.blocks.placement.common.AbstractConnectingBlockPlacementRule;
import net.minestom.vanilla.common.tag.BlockTags;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class VerticalSlimBlockPlacementRule extends AbstractConnectingBlockPlacementRule {

    private final Registry<Block> tagManager = Block.staticRegistry();
    private final List<Block> walls = toList(tagManager.getTag(Key.key("minecraft:walls")));
    private final Set<Block> glassPanes = BlockTags.getInstance().getTaggedWith("blocksandstuff:glass_panes");
    private final RegistryTag<Block> canConnect = RegistryTag.direct(
        new ArrayList<>() {{
            addAll(walls);
            addAll(glassPanes);
        }}
    );

    private static List<Block> toList(RegistryTag<Block> tag) {
        List<Block> list = new ArrayList<>();
        if (tag != null) {
            tag.forEach(it -> list.add(it.asValue()));
        }
        return list;
    }

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

