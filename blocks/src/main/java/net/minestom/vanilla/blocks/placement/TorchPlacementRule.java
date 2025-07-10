package net.minestom.vanilla.blocks.placement;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.registry.Registry;
import net.minestom.server.registry.RegistryTag;
import net.minestom.vanilla.common.item.DroppedItemFactory;
import net.minestom.vanilla.common.tag.BlockTags;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;

public class TorchPlacementRule extends BlockPlacementRule {

    private final Registry<Block> tagManager = Block.staticRegistry();

    private final Set<Block> glassPanes = BlockTags.getInstance().getTaggedWith("vri:glass_panes");

    private final RegistryTag<Block> nonFullButPlaceable = RegistryTag.direct(
        new ArrayList<>() {{
            addAll(Block.values().stream().filter(it -> it.name().endsWith("_fence")).toList());
            addAll(Block.values().stream().filter(it -> it.name().endsWith("_wall")).toList());
            addAll(glassPanes);
        }}
    );

    public TorchPlacementRule(Block block) {
        super(block);
    }

    private boolean getIsNotFullFace(Block.Getter instance, Point position, BlockFace face) {
        return !instance.getBlock(position).registry().collisionShape().isFaceFull(face);
    }

    private boolean canSupportTorch(Block.Getter instance, Point position, BlockFace blockFace) {
        Block block = instance.getBlock(position);
        boolean isFullFace = !getIsNotFullFace(instance, position, blockFace);
        // Certain blocks like fences and walls don't have full faces on the top but torches can be placed on them
        return isFullFace || (blockFace == BlockFace.TOP && nonFullButPlaceable.contains(block));
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        BlockFace blockFace = placementState.blockFace();
        if (blockFace == null) {
            return null;
        }
        Point supporting = placementState.placePosition().add(blockFace.getOppositeFace().toDirection().vec());
        boolean isNotFullFace = getIsNotFullFace(placementState.instance(), supporting, blockFace);

        if (blockFace == BlockFace.BOTTOM) {
            return null;
        }

        if (isNotFullFace && blockFace != BlockFace.TOP) {
            // placing on the side of a block with bottom support places the torch next to the block
            blockFace = BlockFace.TOP;
            supporting = placementState.placePosition().add(0.0, -1.0, 0.0);
        }

        if (blockFace == BlockFace.TOP) {
            if (!canSupportTorch(placementState.instance(), supporting, blockFace)) {
                return null;
            }
            return block;
        }

        Block torch;
        var material = placementState.block().registry().material();
        if (material == Block.TORCH.registry().material()) {
            torch = Block.WALL_TORCH;
        } else if (material == Block.SOUL_TORCH.registry().material()) {
            torch = Block.SOUL_WALL_TORCH;
        } else if (material == Block.REDSTONE_TORCH.registry().material()) {
            torch = Block.REDSTONE_WALL_TORCH;
        } else {
            return null;
        }

        return torch.withNbt(placementState.block().nbtOrEmpty())
                .withProperty("facing", placementState.blockFace().name().toLowerCase(Locale.ROOT));
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {
        String facingProp = updateState.currentBlock().getProperty("facing");
        BlockFace supportingFace = (facingProp != null)
                ? BlockFace.valueOf(facingProp.toUpperCase(Locale.ROOT)).getOppositeFace()
                : BlockFace.BOTTOM;

        if (!canSupportTorch(
                updateState.instance(),
                updateState.blockPosition().add(supportingFace.toDirection().vec()),
                supportingFace
        )) {
            DroppedItemFactory.maybeDrop(updateState);
            return Block.AIR;
        }

        return updateState.currentBlock();
    }
}

