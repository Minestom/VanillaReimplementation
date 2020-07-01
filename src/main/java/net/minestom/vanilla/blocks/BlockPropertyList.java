package net.minestom.vanilla.blocks;

import java.util.*;

/**
 * Represents all properties available for a single block
 */
public class BlockPropertyList {

    private final List<Entry> properties = new ArrayList<>();

    public static class Entry {

        private final String key;
        private final String[] values;

        public Entry(String key, String... values) {
            this.key = key;
            this.values = values;
        }

        public String getKey() {
            return key;
        }

        public String[] getValues() {
            return values;
        }

    }
    /**
     * Returns all possible combinations of properties from this list
     * @return
     */
    public List<String[]> getCartesianProduct() {
        if(properties.isEmpty()) {
            return Collections.emptyList();
        }
        Collections.sort(properties, Comparator.comparing(a -> a.key));
        List<List<String>> combinations = new LinkedList<>();
        computeCartesianProduct(0, combinations);
        List<String[]> cartesianProduct = new LinkedList<>();
        for(List<String> combination : combinations) {
            cartesianProduct.add(combination.toArray(new String[0]));
        }
        return cartesianProduct; // TODO
    }
    private void computeCartesianProduct(int currentIndex, List<List<String>> out) {
        if(currentIndex == properties.size())
            return;
        Entry current = properties.get(currentIndex);
        for(String value : current.values) {
            String property = current.key+"="+value;
            List<List<String>> cartesianNextProperties = new LinkedList<>();
            computeCartesianProduct(currentIndex+1, cartesianNextProperties);

            if(cartesianNextProperties.isEmpty()) {
                out.add(Collections.singletonList(property));
            } else {
                for(List<String> nextProperties : cartesianNextProperties) {
                    List<String> newList = new LinkedList<>();
                    newList.add(property);
                    newList.addAll(nextProperties);
                    out.add(newList);
                }
            }
        }
    }

    /**
     * Defines an int range property. Bounds are inclusive
     * @param key
     * @param rangeStart
     * @param rangeEnd
     * @return
     */
    public BlockPropertyList intRange(String key, int rangeStart, int rangeEnd) {
        assert rangeStart <= rangeEnd;
        String[] values = new String[rangeEnd-rangeStart+1];
        for (int i = rangeStart; i <= rangeEnd; i++) {
            values[i] = String.valueOf(i);
        }
        return property(key, values);
    }

    public BlockPropertyList property(String key, String... values) {
        this.properties.add(new Entry(key, values));
        return this;
    }

    public BlockPropertyList booleanProperty(String key) {
        return property(key, "false", "true");
    }

    public BlockPropertyList directionProperty(String key) {
        return property(key, "north", "east", "south", "west", "up", "down");
    }

    public BlockPropertyList facingProperty(String key) {
        return property(key, "north", "east", "south", "west");
    }

    public boolean isEmpty() {
        return properties.isEmpty();
    }

    public List<Entry> computeSortedList() {
        List<Entry> sortedVersion = new LinkedList<>(properties);
        Collections.sort(sortedVersion, Comparator.comparing(Entry::getKey));
        return sortedVersion;
    }
}
