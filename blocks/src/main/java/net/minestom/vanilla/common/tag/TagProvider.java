package net.minestom.vanilla.common.tag;

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
 * Interface for tag providers that check if elements are tagged and can return sets of elements with specific tags
 * @param <T> The type of element being tagged
 */
public interface TagProvider<T> {
    /**
     * Checks if an element has a specific tag
     * @param element The element to check
     * @param tag The tag to check for
     * @return True if the element has the tag, false otherwise
     */
    boolean hasTag(T element, String tag);

    /**
     * Gets all elements with a specific tag
     * @param tag The tag to look for
     * @return A set of all elements tagged with the given tag
     */
    Set<T> getTaggedWith(String tag);
}
