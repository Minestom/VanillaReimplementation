package net.minestom.vanilla.generation;

import net.minestom.server.instance.block.Block;

public final class NoiseColumn {
    private final int minY;
    private final Block[] column;

    public NoiseColumn(int minY, Block[] column) {
        this.minY = minY;
        this.column = column;
    }

    public Block getBlock(int y) {
        int index = y - this.minY;
        return index >= 0 && index < this.column.length ? this.column[index] : Block.AIR;
    }

    public void setBlock(int y, Block block) {
        int index = y - this.minY;

        if (index < 0 || index >= this.column.length) {
            throw new IllegalArgumentException("Outside of column height: " + y);
        }

        this.column[index] = block;
    }
}
