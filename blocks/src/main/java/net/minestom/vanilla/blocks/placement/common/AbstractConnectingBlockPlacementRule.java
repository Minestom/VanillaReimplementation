package net.minestom.vanilla.blocks.placement.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.registry.Registry;
import net.minestom.server.registry.RegistryTag;
import net.minestom.vanilla.blocks.placement.util.States;

public abstract class AbstractConnectingBlockPlacementRule extends BlockPlacementRule {

    protected final Registry<Block> tagManager = Block.staticRegistry();

    protected final List<Block> leaves = toList(tagManager.getTag(Key.key("minecraft:leaves")));
    protected final List<Block> shulkerBoxes = toList(tagManager.getTag(Key.key("minecraft:shulker_boxes")));

    protected final RegistryTag<Block> cannotConnect = RegistryTag.direct(
      new ArrayList<>() {{
        addAll(leaves);
        addAll(shulkerBoxes);
        add(Block.BARRIER);
        add(Block.CARVED_PUMPKIN);
        add(Block.JACK_O_LANTERN);
        add(Block.MELON);
        add(Block.PUMPKIN);
      }}
    );

    private static List<Block> toList(RegistryTag<Block> tag) {
        List<Block> list = new ArrayList<>();
        if (tag != null) {
            tag.forEach(it -> list.add(it.asValue()));
        }
        return list;
    }

    protected AbstractConnectingBlockPlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {
        Block.Getter instance = updateState.instance();
        Point placePos = updateState.blockPosition();
        return transmute(instance, placePos, getProperty(updateState.currentBlock(), instance, placePos));
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        Block.Getter instance = placementState.instance();
        Point placePos = placementState.placePosition();
        return transmute(instance, placePos, getProperty(placementState.block(), instance, placePos));
    }

    private Block getProperty(Block block, Block.Getter instance, Point placePos) {
        Point north = placePos.relative(BlockFace.NORTH);
        Point east = placePos.relative(BlockFace.EAST);
        Point south = placePos.relative(BlockFace.SOUTH);
        Point west = placePos.relative(BlockFace.WEST);

        return block.withProperties(
            Map.of(
                States.NORTH, stringify(canConnect(instance, north, BlockFace.SOUTH), instance, north, BlockFace.SOUTH),
                States.EAST, stringify(canConnect(instance, east, BlockFace.WEST), instance, east, BlockFace.WEST),
                States.SOUTH, stringify(canConnect(instance, south, BlockFace.NORTH), instance, south, BlockFace.NORTH),
                States.WEST, stringify(canConnect(instance, west, BlockFace.EAST), instance, west, BlockFace.EAST)
            )
        );
    }

    /**
     * Determines whether the block can connect to the block at the given position in the specified direction
     */
    public abstract boolean canConnect(Block.Getter instance, Point pos, BlockFace blockFace);

    /**
     * Converts the connection status to a string property value
     */
    public String stringify(boolean connect, Block.Getter instance, Point pos, BlockFace direction) {
        return String.valueOf(connect);
    }

    /**
     * Allows transmuting the block into a different block type based on position
     */
    public Block transmute(Block.Getter instance, Point pos, Block block) {
        return block;
    }
}
