package net.minestom.vanilla.blockentity;

import net.minestom.server.data.SerializableData;
import net.minestom.server.utils.BlockPosition;

/**
 * Base class used to represent Block Entities
 */
public class BlockEntity extends SerializableData {
    private final BlockPosition position;

    public BlockEntity(BlockPosition position) {
        this.position = position;
    }

    public BlockPosition getPosition() {
        return position;
    }
}
