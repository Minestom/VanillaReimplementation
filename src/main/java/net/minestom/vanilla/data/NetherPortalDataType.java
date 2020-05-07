package net.minestom.vanilla.data;

import net.minestom.server.data.DataType;
import net.minestom.server.network.packet.PacketReader;
import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.utils.BlockPosition;
import net.minestom.vanilla.system.NetherPortal;

public class NetherPortalDataType extends DataType<NetherPortal> {
    @Override
    public void encode(PacketWriter packetWriter, NetherPortal value) {
        packetWriter.writeBlockPosition(value.getFrameBottomRightCorner());
        packetWriter.writeBlockPosition(value.getFrameTopLeftCorner());
        packetWriter.writeVarInt(value.getAxis().ordinal());
    }

    @Override
    public NetherPortal decode(PacketReader packetReader) {
        BlockPosition bottomRight = packetReader.readBlockPosition();
        BlockPosition topLeft = packetReader.readBlockPosition();
        NetherPortal.Axis[] axisValues = NetherPortal.Axis.values();
        NetherPortal.Axis axis = axisValues[packetReader.readVarInt() % axisValues.length];
        return new NetherPortal(axis, bottomRight, topLeft);
    }
}
