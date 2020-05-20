package net.minestom.vanilla.blocks;

import net.minestom.server.data.Data;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.Vector;
import net.minestom.vanilla.entity.PrimedTNT;

import java.util.Random;

public class TNTBlock extends VanillaBlock {

    private static final Random tntRNG = new Random();

    public TNTBlock() {
        super(Block.TNT);
    }

    @Override
    protected BlockPropertyList createPropertyValues() {
        return new BlockPropertyList().booleanProperty("unstable");
    }

    @Override
    public boolean onInteract(Player player, Player.Hand hand, BlockPosition blockPosition, Data data) {
        if(player.getInventory().getItemInHand(hand).getMaterial() == Material.FLINT_AND_STEEL) {
            player.getInstance().setBlock(blockPosition, Block.AIR);

            spawnPrimedTNT(player.getInstance(), blockPosition, 80);
            return true;
        }
        return super.onInteract(player, hand, blockPosition, data);
    }

    @Override
    public boolean onExplode(Instance instance, BlockPosition position, Data lootTableArguments) {
        int fuseTime = tntRNG.nextInt(21)+10;
        spawnPrimedTNT(instance, position, fuseTime);
        return false; // don't cancel
    }

    private void spawnPrimedTNT(Instance instance, BlockPosition blockPosition, int fuseTime) {
        PrimedTNT primedTNT = new PrimedTNT();
        primedTNT.getPosition().setX(blockPosition.getX()+0.5f);
        primedTNT.getPosition().setY(blockPosition.getY()+0f);
        primedTNT.getPosition().setZ(blockPosition.getZ()+0.5f);

        primedTNT.setVelocity(new Vector(tntRNG.nextFloat()*2f-1f, tntRNG.nextFloat()*5f, tntRNG.nextFloat()*2f-1f));

        primedTNT.setFuseTime(fuseTime);
        primedTNT.setInstance(instance);
    }
}
