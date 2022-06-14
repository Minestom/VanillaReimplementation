package net.minestom.vanilla.blocks;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.vanilla.system.EnderChestSystem;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTList;

public class EnderChestBlockHandler extends ChestLikeBlockHandler {
    public EnderChestBlockHandler() {
        super(Block.ENDER_CHEST);
    }

    @Override
    public boolean dropContentsOnDestroy() {
        return false;
    }

    @Override
    protected NBTList<NBTCompound> getAllItems(Instance instance, Point pos, Player player) {
        return EnderChestSystem.getInstance().getItems(player);
    }
}
