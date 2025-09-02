package net.minestom.vanilla.common.item;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.utils.time.TimeUnit;

import java.util.Random;

/**
 * This file contains code ported from Kotlin to Java, adapted from the Blocks and Stuff project.
 * Original source: https://github.com/everbuild-org/blocks-and-stuff
 * <p>
 * Original authors: ChrisB, AEinNico, CreepyX
 * <p>
 * Ported from Kotlin to Java and adapted for use in this project with modifications.
 */
public class DefaultDroppedItemFactory implements DroppedItemFactory {
    private final Random random = new Random();

    @Override
    public void spawn(Instance instance, Point position, Block block) {
        Material material = block.registry().material();
        if (material == null) return;
        spawn(instance, position, ItemStack.of(material));
    }

    @Override
    public void spawn(Instance instance, Point position, ItemStack item) {
        ItemEntity entity = new ItemEntity(item);
        entity.setPickupDelay(1, TimeUnit.SECOND); // 1s for natural drop
        entity.scheduleRemove(5, TimeUnit.MINUTE);
        entity.setVelocity(new Vec(
            random.nextDouble() * 2 - 1,
            2.0,
            random.nextDouble() * 2 - 1
        ));
        entity.setInstance(instance, position.add(0.5, 0.5, 0.5));
    }
}
