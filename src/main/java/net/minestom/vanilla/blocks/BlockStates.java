package net.minestom.vanilla.blocks;

import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockAlternative;
import net.minestom.server.utils.BlockPosition;

import java.util.*;
import java.util.stream.Collectors;

/**
 * All block states for a block
 */
public class BlockStates {

    private final List<BlockState> states;

    /**
     * Map that stores a comma-separated list of properties, sorted by alphabetical order as key,
     * and the corresponding block state as value
     */
    private final Map<String, BlockState> nameLookup = new HashMap<>();
    private final Short2ObjectOpenHashMap<BlockState> idLookup = new Short2ObjectOpenHashMap<>();
    private final BlockPropertyList properties;

    public BlockStates(BlockPropertyList properties) {
        this.properties = properties;
        this.states = new LinkedList<>();
    }

    /**
     * Adds a new blockstate to the known ones
     */
    void add(BlockState blockState) {
        states.add(blockState);
        String lookupKey = properties.computeSortedList().stream()
                .map(property -> property.getKey()+"="+blockState.get(property.getKey()))
                .collect(Collectors.joining(","));
        nameLookup.put(lookupKey, blockState);
        idLookup.put(blockState.getBlockId(), blockState);
    }

    /**
     * Gets a BlockState based on the given properties
     * the value 'value'
     * @param properties
     * @return
     */
    public BlockState getState(Map<String, String> properties) {
        String lookupKey = properties.entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .map(entry -> entry.getKey()+"="+entry.getValue())
                .collect(Collectors.joining(","));
        return nameLookup.get(lookupKey);
    }

    /**
     * Gets a BlockState based on its protocol id. Return {@link #getDefault()} if none found
     * @param id
     * @return
     */
    public BlockState fromStateID(short id) {
        return idLookup.getOrDefault(id, getDefault());
    }

    /**
     * Gets a BlockState based on the given properties, with the property corresponding to 'key' being changed to have
     * the value 'value'
     * @param properties
     * @param key
     * @param value
     * @return
     */
    public BlockState getStateWithChange(Map<String, String> properties, String key, String value) {
        String lookupKey = properties.entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .map(entry -> {
                    String prefix = entry.getKey()+"=";
                    if(entry.getKey().equalsIgnoreCase(key)) {
                        return prefix+value;
                    }
                    return prefix+entry.getValue();
                })
                .collect(Collectors.joining(","));
        return nameLookup.get(lookupKey);
    }

    public BlockState getDefault() {
        return states.get(0);
    }

    /**
     * Returns the corresponding BlockState at the given position.
     * Can return null if it does not correspond to any known state.
     * @param instance
     * @param blockPosition
     * @return
     */
    public BlockState getFromInstance(Instance instance, BlockPosition blockPosition) {
        short id = instance.getBlockId(blockPosition);
        BlockAlternative alternative = Block.fromId(id).getAlternative(id);
        return getState(alternative.createPropertiesMap());
    }
}
