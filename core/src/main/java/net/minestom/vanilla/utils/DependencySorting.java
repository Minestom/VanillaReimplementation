package net.minestom.vanilla.utils;

import java.util.*;

public class DependencySorting {

    public interface NamespaceDependent<T> {
        T identity();
        Set<T> dependencies();
    }

    public static <T, ND extends NamespaceDependent<T>> List<ND> sort(Set<ND> dependants) {
        List<ND> sorted = new ArrayList<>();
        Set<T> visited = new HashSet<>();
        for (ND dependant : dependants) {
            visit(dependant, dependants, sorted, visited);
        }
        return List.copyOf(sorted);
    }

    private static <T, ND extends NamespaceDependent<T>> void visit(ND dependant, Set<ND> dependants, List<ND> sorted, Set<T> visited) {
        T identity = dependant.identity();
        if (visited.contains(identity)) {
            return;
        }
        visited.add(identity);
        for (T dependency : dependant.dependencies()) {
            ND dependencyDependant = dependants.stream()
                    .filter(d -> d.identity().equals(dependency))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Missing dependency " + dependency + " for " + identity));
            visit(dependencyDependant, dependants, sorted, visited);
        }
        sorted.add(dependant);
    }
}
