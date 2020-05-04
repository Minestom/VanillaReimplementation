package net.minestom.vanilla.blockentity;

import net.minestom.server.utils.BlockPosition;
import net.minestom.vanilla.system.NetherPortalSystem;

public class NetherPortalBlockEntity extends BlockEntity {
    public NetherPortalBlockEntity(BlockPosition position) {
        super(position);
    }

    public void setRelatedPortal(NetherPortalSystem.NetherPortal portal) {
        set("portal", portal, NetherPortalSystem.NetherPortal.class);
    }

    public NetherPortalSystem.NetherPortal getRelatedPortal() {
        return get("portal");
    }
}
