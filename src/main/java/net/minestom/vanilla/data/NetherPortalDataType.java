package net.minestom.vanilla.data;

import net.minestom.server.data.DataType;
import net.minestom.server.network.packet.PacketReader;
import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.utils.BlockPosition;
import net.minestom.vanilla.system.NetherPortalSystem;

public class NetherPortalDataType extends DataType<NetherPortalSystem.NetherPortal> {
    @Override
    public void encode(PacketWriter packetWriter, NetherPortalSystem.NetherPortal value) {
        packetWriter.writeBlockPosition(value.getFrameBottomRightCorner());
        packetWriter.writeBlockPosition(value.getFrameTopLeftCorner());
        packetWriter.writeVarInt(value.getAxis().ordinal());
    }

    @Override
    public NetherPortalSystem.NetherPortal decode(PacketReader packetReader) {
        BlockPosition bottomRight = packetReader.readBlockPosition();
        BlockPosition topLeft = packetReader.readBlockPosition();
        NetherPortalSystem.Axis[] axisValues = NetherPortalSystem.Axis.values();
        NetherPortalSystem.Axis axis = axisValues[packetReader.readVarInt() % axisValues.length];
        return new NetherPortalSystem.NetherPortal(axis, bottomRight, topLeft);
    }
}
