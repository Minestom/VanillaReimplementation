package net.minestom.vanilla.blocks;

import net.minestom.server.data.Data;
import net.minestom.server.data.DataImpl;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.Direction;
import net.minestom.vanilla.instance.VanillaExplosion;

public class BedBlock extends VanillaBlock {
    public BedBlock(Block bedBlock) {
        super(bedBlock);
    }

    @Override
    protected BlockPropertyList createPropertyValues() {
        return new BlockPropertyList().facingProperty("facing").booleanProperty("occupied").property("part", "foot", "head");
    }

    @Override
    public boolean onInteract(Player player, Player.Hand hand, BlockPosition blockPosition, Data data) {
        Instance instance = player.getInstance();
        if (instance.getDimensionType().isBedSafe()) {
            // TODO: make player sleep
            // TODO: checks for mobs
            // TODO: check for day
            //if(instance.getDayTime() > 12541L && instance.getDayTime() < 23458L) {

            //}
            return true;
        } else {
            Data args = new DataImpl();
            args.set(VanillaExplosion.IS_FLAMING_KEY, true, Boolean.TYPE);
            instance.explode(blockPosition.getX() + 0.5f, blockPosition.getY() + 0.5f, blockPosition.getZ() + 0.5f, 5f, data);
            return true;
        }
    }

    @Override
    public void onDestroy(Instance instance, BlockPosition blockPosition, Data data) {
        BlockState currentState = getBlockStates().getFromInstance(instance, blockPosition);
        boolean isFoot = "foot".equals(currentState.get("part"));
        Direction facing = Direction.valueOf(currentState.get("facing").toUpperCase());
        BlockPosition otherPartPosition = new BlockPosition(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ());
        if (!isFoot) {
            facing = facing.opposite();
        }
        otherPartPosition.add(facing.normalX(), facing.normalY(), facing.normalZ());
        VanillaBlocks.dropOnBreak(instance, blockPosition);
        instance.setBlock(otherPartPosition, Block.AIR);
    }
}
