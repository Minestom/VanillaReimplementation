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
