package net.minestom.vanilla.fluids.impl;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.utils.Direction;

/**
 * This file contains code ported from Kotlin to Java, adapted from the Blocks and Stuff project.
 * Original source: https://github.com/everbuild-org/blocks-and-stuff
 * <p>
 * Original authors: ChrisB, AEinNico, CreepyX
 * <p>
 * Ported from Kotlin to Java and adapted for use in this project with modifications.
 */
public abstract class Fluid {
    protected final Block defaultBlock;
    protected final ItemStack bucket;

    protected Fluid(Block defaultBlock, Material bucket) {
        this.defaultBlock = defaultBlock;
        this.bucket = ItemStack.of(bucket);
    }

    public Block getDefaultBlock() {
        return defaultBlock;
    }

    public ItemStack getBucket() {
        return bucket;
    }

    public abstract boolean canBeReplacedWith(
        Instance instance,
        Point point,
        Fluid other,
        Direction direction
    );

    public abstract int getNextTickDelay(Instance instance, Point point, Block block);

    public void onTick(Instance instance, Point point, Block block) {
        // Default implementation does nothing
    }

    protected boolean isEmpty() {
        return false;
    }

    protected abstract double getBlastResistance();

    public abstract double getHeight(Block block, Instance instance, Point point);
    public abstract double getHeight(Block block);
    public abstract boolean isInTile(Block block);

    public static boolean isSource(Block block) {
        String levelStr = block.getProperty("level");
        return levelStr == null || Integer.parseInt(levelStr) == 0;
    }

    public static int getLevel(Block block) {
        String levelStr = block.getProperty("level");
        if (levelStr == null) return 8;

        int level = Integer.parseInt(levelStr);
        if (level >= 8) return 8; // Falling water

        return 8 - level;
    }

    public static boolean isFalling(Block block) {
        String levelStr = block.getProperty("level");
        if (levelStr == null) return false;
        return Integer.parseInt(levelStr) >= 8;
    }
}
