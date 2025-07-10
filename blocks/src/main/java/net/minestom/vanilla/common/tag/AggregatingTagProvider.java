package net.minestom.vanilla.common.tag;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * This file contains code ported from Kotlin to Java, adapted from the Blocks and Stuff project.
 * Original source: https://github.com/everbuild-org/blocks-and-stuff
 * <p>
 * Original authors: ChrisB, AEinNico, CreepyX
 * <p>
 * Ported from Kotlin to Java and adapted for use in this project with modifications.
 * <p>
 * A TagProvider that aggregates results from multiple child tag providers
 * @param <T> The type of element being tagged
 */
public abstract class AggregatingTagProvider<T> implements TagProvider<T> {
    private final List<TagProvider<T>> children = new ArrayList<>();

    /**
     * Adds a child tag provider
     * @param child The tag provider to add
     * @return True if the child was added successfully
     */
    public boolean addChild(TagProvider<T> child) {
        return children.add(child);
    }

    /**
     * Removes a child tag provider
     * @param child The tag provider to remove
     * @return True if the child was removed successfully
     */
    public boolean removeChild(TagProvider<T> child) {
        return children.remove(child);
    }

    /**
     * Clears all child tag providers
     */
    public void clearChildren() {
        children.clear();
    }

    @Override
    public boolean hasTag(T element, String tag) {
        for (TagProvider<T> child : children) {
            if (child.hasTag(element, tag)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Set<T> getTaggedWith(String tag) {
        Set<T> result = new HashSet<>();
        for (TagProvider<T> child : children) {
            result.addAll(child.getTaggedWith(tag));
        }
        return result;
    }
}
