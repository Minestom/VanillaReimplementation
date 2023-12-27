package net.minestom.vanilla.utils;

import java.util.Collection;
import java.util.random.RandomGenerator;

public class JavaUtils {

    public static <T> T randomElement(RandomGenerator random, Collection<T> collection) {
        int size = collection.size();
        int index = random.nextInt(size);
        for (T element : collection) {
            if (index == 0) {
                return element;
            }
            index--;
        }
        throw new IllegalStateException("Collection size changed during iteration");
    }
}
