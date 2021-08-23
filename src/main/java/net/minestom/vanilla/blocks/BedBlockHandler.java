package net.minestom.vanilla.blocks;

import net.minestom.server.coordinate.Point;
import net.minestom.server.data.Data;
import net.minestom.server.data.DataImpl;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerUseItemOnBlockEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.Direction;
import net.minestom.server.utils.MathUtils;
import net.minestom.vanilla.instance.VanillaExplosion;
import net.minestom.vanilla.inventory.InventoryManipulation;
import org.jetbrains.annotations.NotNull;

/**
 * TODO:
 * This block handler needs to be able to override block placements in the onPlace method to work correctly.
 */
public class BedBlockHandler extends VanillaBlockHandler {
    public BedBlockHandler(Block bedBlock) {
        super(bedBlock);
    }

//    @Override
//    protected BlockPropertyList createPropertyValues() {
//        return new BlockPropertyList().facingProperty("facing").booleanProperty("occupied").property("part", "foot", "head");
//    }

    public void onPlace(@NotNull Placement placement) {
        if (!(placement instanceof PlayerPlacement)) {
            return;
        }

        PlayerPlacement playerPlacement = (PlayerPlacement) placement;
        Instance instance = placement.getInstance();
        Point pos = placement.getBlockPosition();
        Player player = playerPlacement.getPlayer();

        ItemStack itemStack = player.getItemInMainHand(); // TODO: Hand determination

        Block bedBlock = itemStack.getMaterial().block();

        // TODO: Proper block placement management
        Direction playerDirection = MathUtils.getHorizontalDirection(player.getPosition().yaw());

        Point bedHeadPosition = pos.add(playerDirection.normalX(), playerDirection.normalY(), playerDirection.normalZ());
        Block blockAtPotentialBedHead = instance.getBlock(bedHeadPosition);

        if (isReplaceable(blockAtPotentialBedHead)) {
            placeBed(instance, bedBlock, pos, bedHeadPosition, playerDirection);
        }
    }

    private boolean isReplaceable(Block blockAtPosition) {
        return blockAtPosition.isAir() || blockAtPosition.isLiquid();
    }

    private void placeBed(Instance instance, Block bedBlock, Point footPosition, Point headPosition, Direction facing) {
        Block correctFacing = bedBlock.withProperty("facing", facing.name().toLowerCase());

        Block footBlock = correctFacing.withProperty("part", "foot");
        Block headBlock = correctFacing.withProperty("part", "head");
        instance.setBlock(footPosition, footBlock);
        instance.setBlock(headPosition, headBlock);
    }

    @Override
    public boolean onInteract(@NotNull Interaction interaction) {
        Instance instance = interaction.getInstance();
        Point pos = interaction.getBlockPosition();

        if (instance.getDimensionType().isBedSafe()) {
            // TODO: make player sleep
            // TODO: checks for mobs
            // TODO: check for day
            //if(instance.getDayTime() > 12541L && instance.getDayTime() < 23458L) {

            //}
            return true;
        }

        Data args = new DataImpl();
        args.set(VanillaExplosion.IS_FLAMING_KEY, true, Boolean.TYPE);
        instance.explode(
                (float) pos.x() + 0.5f,
                (float) pos.y() + 0.5f,
                (float) pos.z() + 0.5f,
                5f,
                args
        );
        return true;
    }

    @Override
    public void onDestroy(@NotNull Destroy destroy) {
        Instance instance = destroy.getInstance();
        Block block = destroy.getBlock();
        Point pos = destroy.getBlockPosition();

        System.out.println(block.name());

        boolean isFoot = "foot".equals(block.getProperty("part"));
        Direction facing = Direction.valueOf(block.getProperty("facing").toUpperCase());

        if (isFoot) {
            facing = facing.opposite();
        }

        Point otherPartPosition = pos.add(facing.normalX(), facing.normalY(), facing.normalZ()); // TODO: Investigate why direction is wrong
        instance.setBlock(pos, Block.AIR);
        instance.setBlock(otherPartPosition, Block.AIR);
    }
}
