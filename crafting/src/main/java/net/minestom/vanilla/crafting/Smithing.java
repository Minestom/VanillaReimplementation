package net.minestom.vanilla.crafting;

import dev.goldenstack.window.InventoryView;
import dev.goldenstack.window.Views;
import net.minestom.vanilla.logging.Logger;
import org.jetbrains.annotations.NotNull;

record Smithing(@NotNull InventoryView view,
                @NotNull InventoryView.Singular template,
                @NotNull InventoryView.Singular base,
                @NotNull InventoryView.Singular addition,
                @NotNull InventoryView.Singular output) {

    // TODO: waiting for org.goldenstack.window.Views.Smithing to be updated and replace this. until then, this class will act as a placeholder.


    static final Smithing smithing = new Smithing();

    public static Smithing smithing() { return smithing;  }

    public int size() {
        return view().size();
    }

    public int localToExternal(int localSlot) {
        return view().localToExternal(localSlot);
    }

    public int externalToLocal(int externalSlot) {
        return view().externalToLocal(externalSlot);
    }

    public boolean isValidExternal(int externalSlot) {
        return view().isValidExternal(externalSlot);
    }
    public static final @NotNull InventoryView VIEW = InventoryView.contiguous(0, 4);
    public static final @NotNull InventoryView.Singular TEMPLATE = VIEW.fork(0);
    public static final @NotNull InventoryView.Singular BASE = VIEW.fork(1);
    public static final @NotNull InventoryView.Singular ADDITION = VIEW.fork(2);
    public static final @NotNull InventoryView.Singular OUTPUT = VIEW.fork(3);

    public Smithing() {
        this(VIEW, TEMPLATE, BASE, ADDITION, OUTPUT);
    }
}
