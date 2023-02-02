package net.minestom.vanilla.utils;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class DependencySorting {

    public interface NamespaceDependent<T> {
        T identity();
        Set<T> dependencies();
    }

    private record SortableSortable<T>(NamespaceDependent<T> dependant, Set<NamespaceDependent<T>> dependencies, Set<NamespaceDependent<T>> dependants) {
        public SortableSortable(NamespaceDependent<T> dependant, Set<NamespaceDependent<T>> dependants) {
            this(dependant, new HashSet<>(), new HashSet<>());
            for (T dependency : dependant.dependencies()) {
                NamespaceDependent<T> dependencyNamespaceDependent = getWhere(dependants, feat -> feat.dependencies().contains(dependency));
                if (dependencyNamespaceDependent == null) throw new IllegalStateException("Sortable " + dependant.identity() + " depends on " + dependency + " which does not exist");
                dependencies.add(dependencyNamespaceDependent);
            }
            for (NamespaceDependent<T> other : dependants) {
                if (other.dependencies().contains(dependant.identity())) {
                    this.dependants.add(other);
                }
            }
        }

        private static <T> T getWhere(Set<T> set, Predicate<T> predicate) {
            for (T t : set) {
                if (predicate.test(t)) return t;
            }
            return null;
        }

        public boolean hasDependencies() {
            return !dependencies.isEmpty();
        }
    }

    private record Sortable<I, ND extends NamespaceDependent<I>>(ND value, I identity, Set<I> dependencies, Set<I> dependants) {
        public Sortable(ND value, Set<ND> all) {
            this(value, value.identity(), new HashSet<>(value.dependencies()), new HashSet<>());
            for (NamespaceDependent<I> other : all) {
                if (other.dependencies().contains(identity)) {
                    dependants.add(other.identity());
                }
            }
        }
    }

    public static <T, ND extends NamespaceDependent<T>> List<ND> sort(Set<ND> dependants) {
        List<ND> out = new ArrayList<>();
        Queue<Sortable<T, ND>> remaining = new PriorityQueue<>(Comparator.comparingInt(o -> o.dependencies().size()));

        for (ND dependant : dependants) {
            if (dependant.dependencies().isEmpty()) out.add(dependant);
            else remaining.add(new Sortable<>(dependant, dependants));
        }

        while (!remaining.isEmpty()) {
            Sortable<T, ND> dependant = remaining.poll();
            if (dependant.dependencies().isEmpty()) {
                out.add(dependant.value);
                continue;
            }
            for (ND completed : out) {
                dependant.dependencies().remove(completed.identity());
            }
            if (dependant.dependencies().isEmpty()) {
                out.add(dependant.value);
            } else {
                remaining.add(dependant);
            }
        }

        if (out.size() != dependants.size()) throw new IllegalStateException("Circular dependency detected");
        return out;
    }
}
