package net.minestom.vanilla.data;

import net.minestom.server.data.DataType;
import net.minestom.server.network.packet.PacketReader;
import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.Position;
import net.minestom.vanilla.system.NetherPortal;

import java.util.concurrent.CopyOnWriteArraySet;

public class NetherPortalList extends CopyOnWriteArraySet<NetherPortal> {

    public NetherPortal findClosest(Position targetPosition) {
        NetherPortal targetPortal = null;
        BlockPosition targetBlockPosition = targetPosition.toBlockPosition();
        double closestDistance = 128.0; // don't select portals which are too far away
        for (NetherPortal possiblePortal : this) {
            // only X/Z coordinates are important for portal search
            double dx = Math.abs(targetBlockPosition.getX() - possiblePortal.getCenter().getX());
            double dz = Math.abs(targetBlockPosition.getZ() - possiblePortal.getCenter().getZ());
            double distance = Math.max(dx, dz);
            if(distance < closestDistance) {
                closestDistance = distance;
                targetPortal = possiblePortal;
            }
        }
        return targetPortal;
    }

    public static class DataType extends net.minestom.server.data.DataType<NetherPortalList> {

        private static final NetherPortalDataType portalSerializer = new NetherPortalDataType();

        @Override
        public void encode(PacketWriter packetWriter, NetherPortalList value) {
            int count = value.size();
            packetWriter.writeVarInt(count);
            for (NetherPortal portal : value) {
                portalSerializer.encode(packetWriter, portal);
            }
        }

        @Override
        public NetherPortalList decode(PacketReader packetReader) {
            NetherPortalList list = new NetherPortalList();
            int count = packetReader.readVarInt();
            for (int i = 0; i < count; i++) {
                NetherPortal portal = portalSerializer.decode(packetReader);
                list.add(portal);
            }
            return list;
        }
    }

}
