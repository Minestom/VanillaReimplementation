package net.minestom.vanilla.blocks;

import net.minestom.server.data.Data;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.Direction;
import net.minestom.server.utils.MathUtils;
import net.minestom.vanilla.blockentity.ChestBlockEntity;
import net.minestom.vanilla.inventory.DoubleChestInventory;
import net.minestom.vanilla.system.EnderChestSystem;

public class ShulkerBoxBlock extends ChestLikeBlock {
    public ShulkerBoxBlock(Block shulkerboxBlock) {
        super(shulkerboxBlock);
    }

    @Override
    public boolean dropContentsOnDestroy() {
        return false;
    }

    @Override
    protected BlockPropertyList createPropertyValues() {
        return new BlockPropertyList().directionProperty("facing");
    }
    
    @Override
    public short getVisualBlockForPlacement(Player player, Player.Hand hand, BlockPosition position) {
    	float yaw = player.getPosition().getYaw();
    	float pitch = player.getPosition().getPitch();
    	boolean open = false; // TODO: Open Shulker Box
    	Direction direction;    	

    	if (pitch > 45) {
    		direction = Direction.UP;
    	} else if (pitch < -45) {
    		direction = Direction.DOWN;
    	} else {
    		direction = MathUtils.getHorizontalDirection(yaw).opposite();
    	}

        return getBaseBlockState().with("facing", direction.name().toLowerCase()).getBlockId();
    }
    
    @Override
    public boolean onInteract(Player player, Player.Hand hand, BlockPosition blockPosition, Data data) {
    	player.openInventory(getInventory(player, blockPosition, data));
        return true;
    }
    
    @Override
    public Data createData(Instance instance, BlockPosition blockPosition, Data data) {
        return new ChestBlockEntity(blockPosition);
    }

    @Override
    protected Inventory getInventory(Player player, BlockPosition blockPosition, Data data) {
        return ((ChestBlockEntity) data).getInventory();
    }
}
