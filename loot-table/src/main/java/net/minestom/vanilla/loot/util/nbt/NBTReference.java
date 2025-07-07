package net.minestom.vanilla.loot.util.nbt;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.BinaryTagTypes;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

public record NBTReference(@NotNull Supplier<BinaryTag> getter, @NotNull Consumer<BinaryTag> setter) {

    public static @NotNull NBTReference of(@NotNull BinaryTag tag) {
        AtomicReference<BinaryTag> reference = new AtomicReference<>(tag);
        return new NBTReference(reference::get, reference::set);
    }

    public BinaryTag get() {
        return getter.get();
    }

    public void set(BinaryTag nbt) {
        setter.accept(nbt);
    }

    public boolean has(@NotNull String key) {
        return get() instanceof CompoundBinaryTag compound && compound.get(key) != null;
    }

    public NBTReference get(@NotNull String key) {
        return new NBTReference(
                () -> get() instanceof CompoundBinaryTag compound ? compound.get(key) : null,
                nbt -> {
                    if (get() instanceof CompoundBinaryTag compound) {
                        set(compound.put(key, nbt));
                    }
                }
        );
    }

    public int listSize() {
        return get() instanceof ListBinaryTag list ? list.size() : -1;
    }

    public @NotNull NBTReference get(int index) {
        return new NBTReference(
                () -> get() instanceof ListBinaryTag list && index >= 0 && index < list.size() ? list.get(index) : null,
                value -> {
                    if (get() instanceof ListBinaryTag list
                            && (value.type().equals(BinaryTagTypes.END) || list.elementType().equals(value.type()))
                            && index >= 0 && index < list.size()) {
                        set(list.set(index, value, null));
                    }
                }
        );
    }

    public void listAdd(@NotNull BinaryTag tag) {
        if (get() instanceof ListBinaryTag list && (list.isEmpty() || list.elementType().equals(tag.type()))) {
            set(list.add(tag));
        }
    }

}
