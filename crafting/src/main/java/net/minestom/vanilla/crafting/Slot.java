package net.minestom.vanilla.crafting;

import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Set;

enum Slot {
    TOP_LEFT(0, 0), TOP_MID(0, 1), TOP_RIGHT(0, 2),
    MID_LEFT(1, 0), MID_MID(1, 1), MID_RIGHT(1, 2),
    BOTTOM_LEFT(2, 0), BOTTOM_MID(2, 1), BOTTOM_RIGHT(2, 2);

    private final int row;
    private final int col;

    Slot(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public static Collection<Slot> craftingNxN(int n) {
        if (n == 2) return Set.of(TOP_LEFT, TOP_MID, MID_LEFT, MID_MID);
        if (n == 3) return Set.of(TOP_LEFT, TOP_MID, TOP_RIGHT, MID_LEFT, MID_MID, MID_RIGHT, BOTTOM_LEFT, BOTTOM_MID, BOTTOM_RIGHT);
        throw new IllegalArgumentException("n must be 2 or 3");
    }

    public boolean hasSpaceNxN(int rows, int cols, int n) {
        int rowSpace = n - row;
        int colSpace = n - col;
        return rowSpace >= rows && colSpace >= cols;
    }

    public static @Nullable Slot from(int row, int col) {
        for (Slot value : Slot.values()) {
            if (value.row == row && value.col == col) return value;
        }
        return null;
    }

    public int row() {
        return row;
    }

    public int col() {
        return col;
    }

    public @Nullable Slot translate(Slot slot) {
        return from(row - slot.row, col - slot.col);
    }
}
