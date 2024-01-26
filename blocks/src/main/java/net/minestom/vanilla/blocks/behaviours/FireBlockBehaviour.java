package net.minestom.vanilla.blocks.behaviours;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.time.TimeUnit;
import net.minestom.vanilla.blocks.VanillaBlockBehaviour;
import net.minestom.vanilla.blocks.VanillaBlocks;
// import net.minestom.vanilla.damage.DamageTypes;
import net.minestom.vanilla.system.NetherPortal;
import org.jetbrains.annotations.NotNull;

public class FireBlockBehaviour extends VanillaBlockBehaviour {
    public FireBlockBehaviour(@NotNull VanillaBlocks.BlockContext context) {
        super(context);
    }

//    @Override
//    protected BlockPropertyList createPropertyValues() {
//        return new BlockPropertyList().intRange("age", 0, 15);
//    }

    @Override
    public void onTouch(@NotNull Touch touch) {
        Entity touching = touch.getTouching();

        if (!(touching instanceof LivingEntity livingEntity)) {
            return;
        }

        if (livingEntity.isOnFire()) {
            return;
        }

        // livingEntity.damage(DamageTypes.IN_FIRE, 1.0f);
        livingEntity.setFireForDuration(8000, TimeUnit.MILLISECOND);
    }

    public void checkForPortal(Instance instance, Point pos, Block block) {
        NetherPortal portal = NetherPortal.findPortalFrameFromFrameBlock(instance, pos);

        if (portal == null) {
            return;
        }

        if (portal.tryFillFrame(instance)) {
            portal.register(instance);
        }
    }

    @Override
    public void onPlace(@NotNull VanillaPlacement placement) {
        // check for Nether portal immediately
        checkForPortal(placement.instance(), placement.position(), placement.blockToPlace());
    }
}
