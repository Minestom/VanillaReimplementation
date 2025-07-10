package net.minestom.vanilla.common.item;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.rule.BlockPlacementRule.UpdateState;
import net.minestom.server.item.ItemStack;

public interface DroppedItemFactory {
    void spawn(Instance instance, Point position, Block block);
    void spawn(Instance instance, Point position, ItemStack item);

    class Companion {
        private static DroppedItemFactory current = new DefaultDroppedItemFactory();
        private static boolean doDropItems = true;

        public static DroppedItemFactory getCurrent() {
            return current;
        }

        public static void setCurrent(DroppedItemFactory factory) {
            current = factory;
        }

        public static boolean getDoDropItems() {
            return doDropItems;
        }

        public static void setDoDropItems(boolean value) {
            doDropItems = value;
        }

        public static void maybeDrop(Instance instance, Point position, Block block) {
            if (doDropItems) {
                current.spawn(instance, position, block);
            }
        }

        public static void maybeDrop(UpdateState state) {
            if (doDropItems) {
                current.spawn((Instance)state.instance(), state.blockPosition(), state.currentBlock());
            }
        }

        public static void maybeDrop(Instance instance, Point position, ItemStack item) {
            if (doDropItems) {
                current.spawn(instance, position, item);
            }
        }
    }

    // Static members to access companion object methods directly
    DroppedItemFactory current = Companion.getCurrent();
    boolean doDropItems = Companion.getDoDropItems();

    static void maybeDrop(Instance instance, Point position, Block block) {
        Companion.maybeDrop(instance, position, block);
    }

    static void maybeDrop(UpdateState state) {
        Companion.maybeDrop(state);
    }

    static void maybeDrop(Instance instance, Point position, ItemStack item) {
        Companion.maybeDrop(instance, position, item);
    }
}
