package net.minestom.vanilla.data;

import net.minestom.server.data.DataType;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.vanilla.system.NetherPortal;

public class NetherPortalDataType extends DataType<NetherPortal> {
    @Override
    public void encode(BinaryWriter packetWriter, NetherPortal value) {
        packetWriter.writeBlockPosition(value.getFrameBottomRightCorner());
        packetWriter.writeBlockPosition(value.getFrameTopLeftCorner());
        packetWriter.writeVarInt(value.getAxis().ordinal());
    }

    @Override
    public NetherPortal decode(BinaryReader packetReader) {
        BlockPosition bottomRight = packetReader.readBlockPosition();
        BlockPosition topLeft = packetReader.readBlockPosition();
        NetherPortal.Axis[] axisValues = NetherPortal.Axis.values();
        NetherPortal.Axis axis = axisValues[packetReader.readVarInt() % axisValues.length];
        return new NetherPortal(axis, bottomRight, topLeft);
    }
}
