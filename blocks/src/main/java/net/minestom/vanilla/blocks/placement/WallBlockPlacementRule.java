package net.minestom.vanilla.blocks.placement;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.registry.RegistryTag;
import net.minestom.vanilla.blocks.placement.common.AbstractConnectingBlockPlacementRule;
import net.minestom.vanilla.common.tag.BlockTags;
import net.minestom.vanilla.common.utils.FluidUtils;

import java.util.ArrayList;
import java.util.Set;

public class WallBlockPlacementRule extends AbstractConnectingBlockPlacementRule {

    private final Set<Block> glassPanes = BlockTags.getInstance().getTaggedWith("vri:glass_panes");
    private final Set<Block> fenceGates = BlockTags.getInstance().getTaggedWith("minecraft:fence_gates");
    private final RegistryTag<Block> canConnect = RegistryTag.direct(
        new ArrayList<>() {{
            addAll(Block.values().stream().filter(it -> it.name().endsWith("_wall")).toList());
            addAll(glassPanes);
            addAll(fenceGates);
        }}
    );

    public WallBlockPlacementRule(Block block) {
        super(block);
    }

    @Override
    public boolean canConnect(Block.Getter instance, Point pos, BlockFace blockFace) {
        Block instanceBlock = instance.getBlock(pos);
        boolean isFaceFull = instanceBlock.registry().collisionShape().isFaceFull(blockFace);
        return (!cannotConnect.contains(instanceBlock) && isFaceFull) || canConnect.contains(instanceBlock) || instanceBlock.key().equals(this.block.key());
    }

    @Override
    public String stringify(boolean connect, Block.Getter instance, Point pos, BlockFace direction) {
        if (!connect) return "none";
        Block above = instance.getBlock(pos.add(0.0, 1.0, 0.0));
        if (!above.isAir()) return "tall";
        return "low";
    }

    @Override
    public Block transmute(Block.Getter instance, Point pos, Block block) {
        Block instanceBlock = instance.getBlock(pos);
        boolean north = canConnect(instance, pos.add(BlockFace.NORTH.toDirection().vec()), BlockFace.NORTH);
        boolean east = canConnect(instance, pos.add(BlockFace.EAST.toDirection().vec()), BlockFace.EAST);
        boolean south = canConnect(instance, pos.add(BlockFace.SOUTH.toDirection().vec()), BlockFace.SOUTH);
        boolean west = canConnect(instance, pos.add(BlockFace.WEST.toDirection().vec()), BlockFace.WEST);

        boolean axis1 = north && south;
        boolean axis2 = east && west;

        boolean hasPillar = !((!axis1 && axis2) || (!axis2 && axis1))
          || !(north || east || south || west)
          || (north && south && east && west);

        Block blockAbove = instance.getBlock(pos.add(0.0, 1.0, 0.0));
        boolean blockAboveConnect = !blockAbove.isAir() || (blockAbove.key().equals(block.key()) && "false".equals(blockAbove.getProperty("up")));

        return block
          .withProperty("waterlogged", String.valueOf(FluidUtils.isWater(instanceBlock)))
          .withProperty("up", String.valueOf(hasPillar || blockAboveConnect));
    }
}