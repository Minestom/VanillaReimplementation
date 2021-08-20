package net.minestom.vanilla.blocks;

import net.minestom.server.coordinate.Point;
import net.minestom.server.data.Data;
import net.minestom.server.data.DataImpl;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.vanilla.instance.VanillaExplosion;
import org.jetbrains.annotations.NotNull;

public class BedBlockHandler extends VanillaBlockHandler {
    public BedBlockHandler(Block bedBlock) {
        super(bedBlock);
    }

//    @Override
//    protected BlockPropertyList createPropertyValues() {
//        return new BlockPropertyList().facingProperty("facing").booleanProperty("occupied").property("part", "foot", "head");
//    }



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
//        BlockState currentState = getBlockStates().getFromInstance(instance, blockPosition);
//        boolean isFoot = "foot".equals(currentState.get("part"));
//        Direction facing = Direction.valueOf(currentState.get("facing").toUpperCase());
//        BlockPosition otherPartPosition = new BlockPosition(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ());
//        if (!isFoot) {
//            facing = facing.opposite();
//        }
//        otherPartPosition.add(facing.normalX(), facing.normalY(), facing.normalZ());
//        VanillaBlocks.dropOnBreak(instance, blockPosition);
//        instance.setBlock(otherPartPosition, Block.AIR);
    }
}
