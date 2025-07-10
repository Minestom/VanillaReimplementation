package net.minestom.vanilla.common.utils;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;

/**
 * This file contains code ported from Kotlin to Java, adapted from the Blocks and Stuff project.
 * Original source: https://github.com/everbuild-org/blocks-and-stuff
 * <p>
 * Original authors: ChrisB, AEinNico, CreepyX
 * <p>
 * Ported from Kotlin to Java and adapted for use in this project with modifications.
 */
public class EntityUtils {

    public static Pos eyePosition(Entity entity) {
        if (entity.isSneaking()) {
            return entity.getPosition().add(0.0, 1.23, 0.0);
        }
        return entity.getPosition().add(0.0, 1.53, 0.0);
    }
}
