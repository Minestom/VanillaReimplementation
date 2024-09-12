package net.minestom.vanilla.blocks.behaviours;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.PlayerMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.Direction;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.time.TimeUnit;
import net.minestom.server.world.DimensionType;
import net.minestom.vanilla.blocks.VanillaBlockBehaviour;
import net.minestom.vanilla.blocks.VanillaBlocks;
import net.minestom.vanilla.instance.VanillaExplosion;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage")
public class BedBlockBehaviour extends VanillaBlockBehaviour {
    public BedBlockBehaviour(@NotNull VanillaBlocks.BlockContext context) {
        super(context);
    }

//    @Override
//    protected BlockPropertyList createPropertyValues() {
//        return new BlockPropertyList().facingProperty("facing").booleanProperty("occupied").property("part", "foot", "head");
//    }


    @Override
    public void onPlace(@NotNull VanillaPlacement placement) {
        if (!(placement instanceof VanillaPlacement.HasPlayer hasPlayer)) {
            return;
        }

        Instance instance = placement.instance();
        Point pos = placement.position();
        Player player = hasPlayer.player();

        ItemStack itemStack = player.getItemInMainHand(); // TODO: Hand determination

        Block bedBlock = itemStack.material().block();

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
        Player player = interaction.getPlayer();
        var dimensionKey = instance.getDimensionType();
        DimensionType dimension = MinecraftServer.getDimensionTypeRegistry().get(dimensionKey);

        if (dimension.bedWorks()) {
            // TODO: make player sleep
            // TODO: checks for mobs
            // TODO: check for day

            // If time is not day
//            long dayTime = instance.getTime() % 24000L;
//            if (!(dayTime > 12541L && dayTime < 23458L)) {
//                return true;
//            }

            // Make player sleep
            PlayerMeta meta = player.getPlayerMeta();
            meta.setBedInWhichSleepingPosition(pos);
            meta.setPose(Entity.Pose.SLEEPING);

            // Schedule player getting out of bed
            MinecraftServer.getSchedulerManager().buildTask(() -> {
                        if (!player.getPlayerConnection().isOnline()) {
                            return;
                        }

                        meta.setBedInWhichSleepingPosition(null);
                        meta.setPose(Entity.Pose.STANDING);
                    })
                    .delay(101, TimeUnit.SERVER_TICK)
                    .schedule();
            return true;
        }

        VanillaExplosion.builder(pos.add(0.5), 5)
                .isFlaming(true)
                .build()
                .apply(instance);
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
