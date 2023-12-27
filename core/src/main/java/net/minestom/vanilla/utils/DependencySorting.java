package net.minestom.vanilla.utils;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class DependencySorting {

    public interface NamespaceDependent<T> {
        T identity();
        Set<T> dependencies();
    }

    public static <T, ND extends NamespaceDependent<T>> List<ND> sort(Set<ND> dependants) {
        List<ND> sorted = new ArrayList<>();

        for (ND dependant : dependants) {
            // find the index of the first element that depends on this element
            int index = -1;
            for (int i = 0; i < sorted.size(); i++) {
                if (sorted.get(i).dependencies().contains(dependant.identity())) {
                    index = i;
                    break;
                }
            }

            // if no element depends on this element, add it to the very start
            if (index == -1) {
                sorted.add(dependant);
                continue;
            }

            // else add it before the first element that depends on it
            sorted.add(index, dependant);
        }

        return List.copyOf(sorted);
    }
}
