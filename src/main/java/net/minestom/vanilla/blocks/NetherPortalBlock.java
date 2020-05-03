package net.minestom.vanilla.blocks;

import net.minestom.server.data.Data;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.BlockPosition;
import net.minestom.vanilla.system.NetherPortalSystem;

public class NetherPortalBlock extends VanillaBlock {

    public NetherPortalBlock() {
        super(Block.NETHER_PORTAL);
    }

    @Override
    public Data createData(Instance instance, BlockPosition blockPosition, Data data) {
        return new Data();
    }

    @Override
    public void onDestroy(Instance instance, BlockPosition blockPosition, Data data) {
        if(data.getOrDefault("unbroken", true)) {
            data.set("unbroken", false, Boolean.class);
            NetherPortalSystem.NetherPortal netherPortal = NetherPortalSystem.findPortalFrameFromFrameBlock(instance, blockPosition);
            if(netherPortal != null) {
                netherPortal.breakFrame(instance);
            }
        }
    }

    @Override
    public void updateFromNeighbor(Instance instance, BlockPosition thisPosition, BlockPosition neighborPosition, boolean directNeighbor) {
        breakPortalIfNoLongerValid(instance, thisPosition);
    }

    private void breakPortalIfNoLongerValid(Instance instance, BlockPosition blockPosition) {
        // FIXME this cannot work because the frame no longer exists if an obsidian block is removed
        // TODO: Add new method to get portal only from frames?
        NetherPortalSystem.NetherPortal netherPortal = NetherPortalSystem.findPortalFrameFromFrameBlock(instance, blockPosition);
        if(netherPortal != null) {
            if(!netherPortal.isStillValid(instance)) {
                netherPortal.breakFrame(instance);
            }
        }
    }

    @Override
    protected BlockPropertyList createPropertyValues() {
        return new BlockPropertyList().property("axis", "x", "z");
    }
}
