package net.minestom.vanilla.blockentity;

import net.minestom.server.utils.BlockPosition;
import net.minestom.vanilla.system.NetherPortal;

public class NetherPortalBlockEntity extends BlockEntity {
    public NetherPortalBlockEntity(BlockPosition position) {
        super(position);
    }

    public void setRelatedPortal(NetherPortal portal) {
        set("portal", portal, NetherPortal.class);
    }

    public NetherPortal getRelatedPortal() {
        return get("portal");
    }
}
