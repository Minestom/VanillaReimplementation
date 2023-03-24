package net.minestom.vanilla.datapack.loot;

import org.jglrxavpok.hephaistos.nbt.NBT;

public interface NBTPath {

    /**
     * Indexes the given NBT using this path and returns the result.
     * @param nbt the NBT to index
     * @return the indexed NBT result
     */
    NBT get(NBT nbt);

    /**
     * Sets the given NBT using this path and returns the result.
     * @param container the NBT container to apply the path to
     * @param nbt the NBT to set
     * @return the new NBT container
     */
    NBT set(NBT container, NBT nbt);
}
