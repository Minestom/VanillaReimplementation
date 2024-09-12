package net.minestom.vanilla.generation.moj;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class DepthFirstSearch {
    private DepthFirstSearch() {
    }

    public static <T> boolean depthFirstSearch(Map<T, Set<T>> graph, Set<T> visited, Set<T> recursionStack, Consumer<T> onCycleDetected, T currentNode) {
        if (visited.contains(currentNode)) {
            return false;
        }

        if (recursionStack.contains(currentNode)) {
            return true;
        }

        recursionStack.add(currentNode);
        Iterator<T> neighbors = (graph.getOrDefault(currentNode, Set.of())).iterator();

        T neighbor;
        do {
            if (!neighbors.hasNext()) {
                recursionStack.remove(currentNode);
                visited.add(currentNode);
                onCycleDetected.accept(currentNode);
                return false;
            }

            neighbor = neighbors.next();
        } while (!depthFirstSearch(graph, visited, recursionStack, onCycleDetected, neighbor));

        return true;
    }

}
