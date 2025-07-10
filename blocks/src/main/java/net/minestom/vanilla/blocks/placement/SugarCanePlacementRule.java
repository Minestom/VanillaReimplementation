package net.minestom.vanilla.blocks.placement;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.registry.Registry;
import net.minestom.server.registry.RegistryTag;
import net.minestom.server.registry.TagKey;
import net.minestom.vanilla.common.item.DroppedItemFactory;
import net.minestom.vanilla.common.utils.FluidUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class SugarCanePlacementRule extends BlockPlacementRule {
    private final @NotNull Registry<Block> blockRegistry = Block.staticRegistry();

    private final List<Block> dirt = toList(blockRegistry.getTag(TagKey.ofHash("#minecraft:dirt")));
    private final List<Block> sand = toList(blockRegistry.getTag(TagKey.ofHash("#minecraft:sand")));

    private final RegistryTag<Block> plantable = RegistryTag.direct(
      new ArrayList<>() {{
          addAll(dirt);
          addAll(sand);
      }}
    );

    private static final Set<Map.Entry<Integer, Integer>> VON_NEUMANN = Set.of(
        Map.entry(0, 1),
        Map.entry(0, -1),
        Map.entry(1, 0),
        Map.entry(-1, 0)
    );

    private static List<Block> toList(RegistryTag<Block> tag) {
        List<Block> list = new ArrayList<>();
        if (tag != null) {
            tag.forEach(it -> list.add(it.asValue()));
        }
        return list;
    }

    public SugarCanePlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        if (!isSupported(placementState.instance(), placementState.placePosition())) {
            return null;
        }
        return placementState.block();
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {
        if (!isSupported(updateState.instance(), updateState.blockPosition())) {
            DroppedItemFactory.maybeDrop(updateState);
            return Block.AIR;
        }
        return updateState.currentBlock();
    }

    private boolean isSupported(Block.Getter instance, Point blockPosition) {
        Point posBelow = blockPosition.sub(0.0, 1.0, 0.0);
        Block below = instance.getBlock(posBelow);

        if (below.compare(block, Block.Comparator.ID)) {
            return true;
        }

        if (!plantable.contains(below)) {
            return false;
        }

        for (var entry : VON_NEUMANN) {
            int x = entry.getKey();
            int z = entry.getValue();
            Point pos = posBelow.add(x, 0.0, z);
            Block neighborBlock = instance.getBlock(pos);

            if (FluidUtils.isWater(neighborBlock)) {
                return true;
            }
        }

        return false;
    }
}
