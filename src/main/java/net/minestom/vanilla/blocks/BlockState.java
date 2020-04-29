package net.minestom.vanilla.blocks;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a block state
 */
public class BlockState {

    private final BlockStates parent;
    private final Map<String, String> properties;
    private final short blockId;

    public BlockState(short blockId, BlockStates parent, String... propertyList) {
        this.blockId = blockId;
        this.properties = new HashMap<>();
        this.parent = parent;
        for(String property : propertyList) {
            String[] parts = property.split("=");
            String key = parts[0];
            String value = parts[1];
            properties.put(key, value);
        }
    }

    public short getBlockId() {
        return blockId;
    }

    /**
     * Return the value of the given property key
     * @param key the property key
     * @return the value of the property
     * @throws IllegalArgumentException if the key does not correspond to an existing property
     */
    public String get(String key) {
        String result = properties.get(key);
        if(result == null) {
            throw new IllegalArgumentException("Property '" + key + "' does not exist in blockstate "+this);
        }
        return result;
    }

    /**
     * Returns the block state corresponding to this state with a single property changed
     * @param key the key of the property to change
     * @param value the value of the property
     * @return the corresponding blockstate (they are pooled inside this blockstate's parent BlockStates)
     */
    public BlockState with(String key, String value) {
        return parent.getStateWithChange(properties, key, value);
    }

}
