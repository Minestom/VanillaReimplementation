package net.minestom.vanilla.blocks;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.vanilla.system.EnderChestSystem;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTList;

import java.util.List;

public class EnderChestBlockHandler extends ChestLikeBlockHandler {
    public EnderChestBlockHandler(@NotNull VanillaBlocks.BlockContext context) {
        super(context);
    }

    @Override
    public boolean dropContentsOnDestroy() {
        return false;
    }

    @Override
    protected List<NBTCompound> getAllItems(Instance instance, Point pos, Player player) {
        return EnderChestSystem.getInstance().getItems(player);
    }
}
