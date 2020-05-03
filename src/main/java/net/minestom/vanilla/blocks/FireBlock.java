package net.minestom.vanilla.blocks;

import net.minestom.server.data.Data;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.time.TimeUnit;
import net.minestom.vanilla.system.NetherPortalSystem;

public class FireBlock extends VanillaBlock {
    public FireBlock() {
        super(Block.FIRE);
    }

    @Override
    protected BlockPropertyList createPropertyValues() {
        return new BlockPropertyList().intRange("age", 0, 15);
    }

    @Override
    public void scheduledUpdate(Instance instance, BlockPosition position, Data blockData) {
        NetherPortalSystem.NetherPortal portal = NetherPortalSystem.findPortalFrameFromFrameBlock(instance, position.clone());
        if(portal != null) {
            portal.tryFillFrame(instance);
        }
    }

    @Override
    public void onPlace(Instance instance, BlockPosition blockPosition, Data data) {
        // check for Nether portal immediately next tick
        instance.scheduleUpdate(0, TimeUnit.TICK, blockPosition);
    }
}
