package net.minestom.vanilla.inventory;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.tag.Tag;
import net.minestom.server.tag.TagReadable;
import net.minestom.server.tag.TagSerializer;
import net.minestom.server.tag.TagWritable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.mutable.MutableNBTCompound;

public class ItemStackUtils {
    public static @Nullable ItemStack fromNBTCompound(@NotNull NBTCompound tag) {
        String id = tag.getString("id");

        if (id == null) {
            return null;
        }

        Material material = Material.fromNamespaceId(id);

        if (material == null) {
            return null;
        }

        if (material == Material.AIR) {
            material = Material.STONE;
        }

        Byte count = tag.getByte("Count");

        NBTCompound nbtCompound = tag.getCompound("tag");

        if (count == null || nbtCompound == null) {
            return null;
        }

        return ItemStack.fromNBT(material, nbtCompound, count);
    }

    public static @NotNull NBTCompound toNBTCompound(@NotNull ItemStack itemStack) {
        MutableNBTCompound compound = new MutableNBTCompound();

        compound.setString("id", itemStack.material().namespace().namespace());
        compound.setByte("Count", (byte) itemStack.amount());
        compound.set("tag", itemStack.meta().toNBT());

        return compound.toCompound();
    }

    public static Tag<ItemStack> itemStackTag(@NotNull String key) {
        return Tag.Structure(key, new ItemStackSerializer(key));
    }

    private static class ItemStackSerializer implements TagSerializer<ItemStack> {

        private final Tag<NBTCompound> tag;

        public ItemStackSerializer(String key) {
            //noinspection unchecked
            this.tag = (Tag<NBTCompound>) (Object) Tag.NBT(key);
        }

        @Nullable
        @Override
        public ItemStack read(@NotNull TagReadable reader) {
            NBTCompound nbtCompound = reader.getTag(tag);
            return nbtCompound != null ? ItemStackUtils.fromNBTCompound(nbtCompound) : null;
        }

        @Override
        public void write(@NotNull TagWritable writer, @Nullable ItemStack value) {
            if (value == null) {
                writer.setTag(tag, null);
                return;
            }

            writer.setTag(tag, ItemStackUtils.toNBTCompound(value));
        }
    }
}
