package net.minestom.vanilla.blocks;

import net.minestom.server.data.Data;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.BlockPosition;
import net.minestom.vanilla.blockentity.BlockEntity;
import net.minestom.vanilla.blockentity.NetherPortalBlockEntity;
import net.minestom.vanilla.system.NetherPortalSystem;

public class NetherPortalBlock extends VanillaBlock {

    public NetherPortalBlock() {
        super(Block.NETHER_PORTAL);
    }

    @Override
    public Data createData(Instance instance, BlockPosition blockPosition, Data data) {
        if(data instanceof NetherPortalBlockEntity && ((BlockEntity) data).getPosition().equals(blockPosition))
            return data;
        return new NetherPortalBlockEntity(blockPosition);
    }

    @Override
    public void onDestroy(Instance instance, BlockPosition blockPosition, Data data) {
        NetherPortalSystem.NetherPortal netherPortal = getPortal(data);
        if(netherPortal != null) {
            netherPortal.breakFrame(instance);
        }
    }

    private NetherPortalSystem.NetherPortal getPortal(Data data) {
        if(data instanceof NetherPortalBlockEntity) {
            NetherPortalBlockEntity blockEntity = (NetherPortalBlockEntity)data;
            return blockEntity.getRelatedPortal();
        }
        return null;
    }

    @Override
    public void updateFromNeighbor(Instance instance, BlockPosition thisPosition, BlockPosition neighborPosition, boolean directNeighbor) {
        breakPortalIfNoLongerValid(instance, thisPosition);
    }

    private void breakPortalIfNoLongerValid(Instance instance, BlockPosition blockPosition) {
        NetherPortalSystem.NetherPortal netherPortal = getPortal(instance.getBlockData(blockPosition));
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
