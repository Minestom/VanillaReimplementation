package net.minestom.vanilla.blocks;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.Material;
import net.minestom.vanilla.entity.PrimedTNT;

import java.util.Random;

public class TNTBlockHandler extends VanillaBlockHandler {

    public static final Random TNT_RANDOM = new Random();

    public TNTBlockHandler() {
        super(Block.TNT);
    }

//    @Override
//    protected BlockPropertyList createPropertyValues() {
//        return new BlockPropertyList().booleanProperty("unstable");
//    }

    @Override
    public boolean onInteract(Interaction interaction) {
        Point blockPosition = interaction.getBlockPosition();
        Player player = interaction.getPlayer();
        Player.Hand hand = interaction.getHand();
        PlayerInventory inventory = player.getInventory();

        if (inventory.getItemInHand(hand).getMaterial() != Material.FLINT_AND_STEEL) {
            return true;
        }

        player.getInstance().setBlock(blockPosition, Block.AIR);

        spawnPrimedTNT(player.getInstance(), blockPosition, 80);

        return true;
    }

//    @Override
//    public boolean onExplode(Instance instance, Point position, Data lootTableArguments) {
//        int fuseTime = tntRNG.nextInt(21) + 10;
//        spawnPrimedTNT(instance, position, fuseTime);
//        return false; // don't cancel
//    }

    private void spawnPrimedTNT(Instance instance, Point blockPosition, int fuseTime) {
        Pos initialPosition = new Pos(blockPosition.blockX() + 0.5f, blockPosition.blockY() + 0f, blockPosition.blockZ() + 0.5f);

        PrimedTNT primedTNT = new PrimedTNT(fuseTime);
        primedTNT.setInstance(instance);
        primedTNT.teleport(initialPosition);
        primedTNT.setVelocity(new Vec(TNT_RANDOM.nextFloat() * 2f - 1f, TNT_RANDOM.nextFloat() * 5f, TNT_RANDOM.nextFloat() * 2f - 1f));
    }
}
