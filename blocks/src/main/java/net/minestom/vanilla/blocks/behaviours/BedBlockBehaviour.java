package net.minestom.vanilla.blocks.behaviours;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.EntityPose;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.avatar.PlayerMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.Direction;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.time.TimeUnit;
import net.minestom.server.world.DimensionType;
import net.minestom.server.world.attribute.BedRule;
import net.minestom.server.world.attribute.EnvironmentAttribute;
import net.minestom.server.world.attribute.EnvironmentAttributeMap;
import net.minestom.vanilla.blocks.VanillaBlockBehaviour;
import net.minestom.vanilla.blocks.VanillaBlocks;
import net.minestom.vanilla.instance.VanillaExplosion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
            Block foot = placeBed(instance, bedBlock, bedHeadPosition, playerDirection);
            placement.blockToPlace(foot);
        } else {
            placement.blockToPlace(placement.instance().getBlock(placement.position()));
        }

    }

    private boolean isReplaceable(Block blockAtPosition) {
        return blockAtPosition.isAir() || blockAtPosition.isLiquid();
    }

    private Block placeBed(Instance instance, Block bedBlock, Point headPosition, Direction facing) {
        Block correctFacing = bedBlock.withProperty("facing", facing.name().toLowerCase());

        Block footBlock = correctFacing.withProperty("part", "foot");
        Block headBlock = correctFacing.withProperty("part", "head").withHandler(new BedBlockBehaviour(this.context));
        instance.setBlock(headPosition, headBlock);
        return footBlock;
    }

    @Override
    public boolean onInteract(@NotNull Interaction interaction) {
        Instance instance = interaction.getInstance();
        Point pos = interaction.getBlockPosition();
        Player player = interaction.getPlayer();

        var dimensionKey = instance.getDimensionType();
        DimensionType dimension = MinecraftServer.getDimensionTypeRegistry().get(dimensionKey);
        if (dimension == null) {
            return false;
        }

        BedRule bedRule = resolveBedRule(dimension);
        if (bedRule == null) {
            return false;
        }

        // Closest replacement for old dimension.bedWorks():
        // beds "work" if they do not explode in this dimension.
        if (!bedRule.explodes()) {
            // Optional: only allow actual sleeping when the rule says so
            if (bedRule.canSleep() == BedRule.Rule.NEVER) {
                return false;
            }

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
            meta.setPose(EntityPose.SLEEPING);

            // Schedule player getting out of bed
            MinecraftServer.getSchedulerManager().buildTask(() -> {
                        if (!player.getPlayerConnection().isOnline()) {
                            return;
                        }

                        meta.setBedInWhichSleepingPosition(null);
                        meta.setPose(EntityPose.STANDING);
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

    @SuppressWarnings("unchecked")
    private static @Nullable BedRule resolveBedRule(@NotNull DimensionType dimension) {
        EnvironmentAttributeMap.Entry<?, ?> rawEntry =
                dimension.attributes().entries().get(EnvironmentAttribute.BED_RULE);

        if (rawEntry == null) {
            return null;
        }

        EnvironmentAttributeMap.Entry<BedRule, Object> entry =
                (EnvironmentAttributeMap.Entry<BedRule, Object>) rawEntry;

        return entry.modifier().modify(null, entry.argument());
    }

    @Override
    public void onDestroy(@NotNull Destroy destroy) {
        Instance instance = destroy.getInstance();
        Block block = destroy.getBlock();
        Point pos = destroy.getBlockPosition();

        boolean isHead = "head".equals(block.getProperty("part"));
        Direction facing = Direction.valueOf(block.getProperty("facing").toUpperCase());

        if (isHead) {
            facing = facing.opposite();
        }

        Point otherPartPosition = pos.add(facing.normalX(), facing.normalY(), facing.normalZ());
        instance.setBlock(pos, Block.AIR);
        instance.setBlock(otherPartPosition, Block.AIR);
    }
}
