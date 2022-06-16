package net.minestom.vanilla.blocks;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.Material;
import net.minestom.vanilla.VanillaRegistry;
import net.minestom.vanilla.blocks.redstone.RedstoneContainerBlockHandler;
import net.minestom.vanilla.blocks.redstone.signal.info.RedstoneSignal;
import net.minestom.vanilla.blocks.redstone.signal.info.RedstoneSignalTarget;
import net.minestom.vanilla.entitymeta.EntityTags;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.naming.Context;
import java.util.Random;

public class TNTBlockHandler extends RedstoneContainerBlockHandler {

    public static final Random TNT_RANDOM = new Random();

    public TNTBlockHandler(@NotNull VanillaBlocks.BlockContext context) {
        super(context);
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

    @Override
    public void newRedstoneSignal(
            @NotNull RedstoneSignalTarget redstoneSignalTarget,
            @NotNull RedstoneSignal newRedstoneSignal,
            @Nullable RedstoneSignal oldRedstoneSignal
    ) {
        Instance instance = redstoneSignalTarget.instance();
        Point blockPosition = redstoneSignalTarget.target();

        instance.setBlock(blockPosition, Block.AIR);
        spawnPrimedTNT(instance, blockPosition, 80);
    }

    private void spawnPrimedTNT(Instance instance, Point blockPosition, int fuseTime) {
        Pos initialPosition = new Pos(blockPosition.blockX() + 0.5f, blockPosition.blockY() + 0f, blockPosition.blockZ() + 0.5f);

        // Create the entity
        VanillaRegistry.EntityContext entityContext = context.vri().entityContext(EntityType.TNT, initialPosition,
                writable -> writable.setTag(EntityTags.PrimedTnt.FUSE_TIME, fuseTime));
        Entity primedTnt = context.vri().createEntityOrDummy(entityContext);

        // Spawn it with random velocity
        primedTnt.setInstance(instance, initialPosition);
        primedTnt.setVelocity(new Vec(TNT_RANDOM.nextFloat() * 2f - 1f, TNT_RANDOM.nextFloat() * 5f, TNT_RANDOM.nextFloat() * 2f - 1f));
    }
}
