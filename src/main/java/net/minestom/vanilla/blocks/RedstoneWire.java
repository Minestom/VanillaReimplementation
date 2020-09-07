package net.minestom.vanilla.blocks;

import net.minestom.server.data.Data;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.Direction;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.Vector;
import net.minestom.vanilla.entity.PrimedTNT;

import java.util.Random;

/**
 * Redstone Wire:
 * 
 * This is the entrypoint for all redstone wire.
 */

public class RedstoneWire extends VanillaBlock {
	
	
	/**
	 * Redstone Dot:
	 * 
	 * The redstone dot is different to regular redstone wire.
	 * While normal redstone wire powers the blocks that it is facing and the block below,
	 * a redstone dot only powers the bottom block. (Assuming its a full block)
	 * 
	 * Nethertheless, this is the entrypoint for all redstone wire.
	 */
	
	private boolean isDot;
	
	/**
	 * Redstone Power:
	 * This value dictates the strength of a powered redstone wire.
	 */
	
    private int Power;
    
    /**
	 * Redstone Wire Connections.
	 * These values dictate the connections of a redstone wire.
	 * They are used to update consecutive wire
	 */
	
    private boolean isConnectedNorthFlat, isConnectedNorthDown, isConnectedNorthUp;
    private boolean isConnectedEastFlat, isConnectedEastDown, isConnectedEastUp;
    private boolean isConnectedSouthFlat, isConnectedSouthDown, isConnectedSouthUp;
    private boolean isConnectedWestFlat, isConnectedWestDown, isConnectedWestUp;
    
    public RedstoneWire() {
        super(Block.REDSTONE_WIRE);
    }

    @Override
    protected BlockPropertyList createPropertyValues() {
        return new BlockPropertyList()
        		.property("east", "none", "side", "up")
        		.property("north", "none", "side", "up")
        		.property("power", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15")
        		.property("south", "none", "side", "up")
        		.property("west", "none", "side", "up");
    }
    
    @Override
    public short getVisualBlockForPlacement(Player player, Player.Hand hand, BlockPosition position) {
    	Power = (int) (Math.random() * 15);
    	return getBaseBlockState()
    			.with("east", "none")
    			.with("north", "none")
    			.with("power", Integer.toString(Power))
    			.with("south", "none")
    			.with("west", "none")
                .getBlockId();
    }
}
