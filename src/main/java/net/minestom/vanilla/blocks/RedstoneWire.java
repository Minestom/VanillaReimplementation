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
	 * Redstone Power:
	 * This value dictates the strength of a powered redstone wire.
	 */
	
    private int Power;
    
    
    /**
	 * Redstone Wire Connections.
	 * These values dictate the connections of a redstone wire.
	 * They are used to update consecutive wire
	 * 
	 * Array Specification
	 * 
	 * 
	 * 		0		1		2
	 * 		=		=		=
	 * 		Up		Flat	Down
	 * 
	 * 0 = [true	false	true	] // 1 is North 
	 * 1 = [true	false	true	] // 2 is East
	 * 2 = [true	true	true	] // 3 is South
	 * 3 = [true	true	true	] // 3 is West
	 * 
	 * 
	 * 
	 */
	
    // private Boolean[][] wireConnections;
    
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
    public short getVisualBlockForPlacement(Player player, Player.Hand hand, BlockPosition blockPosition) {
    	if (player.isOnGround()) {
    		Power = (int) (Math.random() * 15);
    	} else {
    		int test = getSurroundingPower(player.getInstance(), blockPosition);
    		System.out.println(test);
    		if (test > 0) {
    			Power = test - 1;
    		} else {
    			Power = 0;
    		}
    	}
    	String North = "none";
    	String East = "none";
    	String South = "none";
    	String West = "none";
    	Boolean[][] Cons = getConnections(player.getInstance(), blockPosition);
    	if (Cons[0][0]) { // If Up-North
    		North = "up";
    	} else if (Cons[0][1] || (Cons[0][2])) { // If Flat-North or Down-North
    		North = "side";
    	}
    	if (Cons[1][0]) { // If Up-East
    		East = "up";
    	} else if (Cons[1][1] || (Cons[1][2])) { // If Flat-East or Down-East
    		East = "side";
    	}
    	if (Cons[2][0]) { // If Up-South
    		South = "up";
    	} else if (Cons[2][1] || (Cons[2][2])) { // If Flat-South or Down-South
    		South = "side";
    	}
    	if (Cons[3][0]) { // If Up-West
    		West = "up";
    	} else if (Cons[3][1] || (Cons[3][2])) { // If Flat-West or Down-West
    		West = "side";
    	}
    	return getBaseBlockState()
    			.with("east", East)
    			.with("north", North)
    			.with("power", Integer.toString(Power))
    			.with("south", South)
    			.with("west", West)
                .getBlockId();
    }
    
    @Override
    public void update(Instance instance, BlockPosition blockPosition, Data data) {
    	
    }
    
    /**
     * 
     * @param instance
     * @param blockPosition
     * @return 
     */
    
    private Boolean[][] getConnections(Instance instance, BlockPosition blockPosition) {
    	
    	// TODO: Structuring needs work here
    	
    	int X = blockPosition.getX();
    	int Y = blockPosition.getY();
    	int Z = blockPosition.getZ();
    	Boolean[][] Connections = {
    			{false, false, false},	// North - Z
    			{false, false, false},	// East + X
    			{false, false, false},	// South + Z
    			{false, false, false}	// West - X
    		};
    	Connections[0][0] = (Block.fromStateId(instance.getBlockStateId(X, Y + 1, Z - 1)) == Block.REDSTONE_WIRE);	// North-Up
    	Connections[0][1] = (Block.fromStateId(instance.getBlockStateId(X, Y, Z - 1)) == Block.REDSTONE_WIRE);		// North-Flat
    	Connections[0][2] = (Block.fromStateId(instance.getBlockStateId(X, Y - 1, Z - 1)) == Block.REDSTONE_WIRE);	// North-Down
    	Connections[1][0] = (Block.fromStateId(instance.getBlockStateId(X + 1, Y + 1, Z)) == Block.REDSTONE_WIRE);	// East-Up
    	Connections[1][1] = (Block.fromStateId(instance.getBlockStateId(X + 1, Y, Z)) == Block.REDSTONE_WIRE);		// East-Flat
    	Connections[1][2] = (Block.fromStateId(instance.getBlockStateId(X + 1, Y - 1, Z)) == Block.REDSTONE_WIRE);	// East-Down
    	Connections[2][0] = (Block.fromStateId(instance.getBlockStateId(X, Y + 1, Z + 1)) == Block.REDSTONE_WIRE);	// South-Up
    	Connections[2][1] = (Block.fromStateId(instance.getBlockStateId(X, Y, Z + 1)) == Block.REDSTONE_WIRE);		// South-Flat
    	Connections[2][2] = (Block.fromStateId(instance.getBlockStateId(X, Y - 1, Z + 1)) == Block.REDSTONE_WIRE);	// South-Down
    	Connections[3][0] = (Block.fromStateId(instance.getBlockStateId(X - 1, Y + 1, Z)) == Block.REDSTONE_WIRE);	// West-Up
    	Connections[3][1] = (Block.fromStateId(instance.getBlockStateId(X - 1, Y, Z)) == Block.REDSTONE_WIRE);		// West-Flat
    	Connections[3][2] = (Block.fromStateId(instance.getBlockStateId(X - 1, Y - 1, Z)) == Block.REDSTONE_WIRE);	// West-Down
    	
    	return Connections;
    }
    
private int getSurroundingPower(Instance instance, BlockPosition blockPosition) {
    	
    	// TODO: Structuring needs work here
    	
		int highestPower = 0;
	
    	int X = blockPosition.getX();
    	int Y = blockPosition.getY();
    	int Z = blockPosition.getZ();
    	for (int i = -1; i < 2; i = i + 2) {
    		for (int j = -1; j < 2; j = j + 2) {
    			for (int k = -1; k < 2; k = k + 2) {
    				int powerLevel = 0;
    				try {
    					powerLevel = (int) instance.getBlockData(X + i, Y + j, Z + k).get("power");
    					System.out.println(powerLevel);
    				} catch (Exception e) {
    					e.printStackTrace();
    				}
    	    		if (powerLevel > highestPower) {
    	    			highestPower = powerLevel;
    	    		}
    	    	}
        	}
    	}
    	
    	return highestPower;
    }
}
