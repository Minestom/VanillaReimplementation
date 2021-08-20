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

import java.util.Objects;

public class ItemStackUtils {
    public static @NotNull ItemStack fromNBTCompound(@NotNull NBTCompound tag) {
        Material material = Material.fromNamespaceId(Objects.requireNonNull(tag.getString("id")));

        if (material == Material.AIR) {
            material = Material.STONE;
        }

        Byte count = tag.getByte("Count");

        NBTCompound nbtCompound = null;

        if (tag.containsKey("tag")) {
            nbtCompound = tag.getCompound("tag");
        }

        return ItemStack.fromNBT(material, nbtCompound, count);
    }

    public static @NotNull NBTCompound toNBTCompound(@NotNull ItemStack itemStack) {
        NBTCompound compound = new NBTCompound();

        compound.setString("id", itemStack.getMaterial().namespace().namespace());
        compound.setByte("Count", (byte) itemStack.getAmount());
        compound.set("tag", itemStack.getMeta().toNBT());

        return compound;
    }

    public static Tag<ItemStack> itemStackTag(@NotNull String key) {
        return Tag.Structure(key, new ItemStackSerializer(key));
    }

    private static class ItemStackSerializer implements TagSerializer<ItemStack> {

        private final Tag<NBTCompound> tag;

        public ItemStackSerializer(String key) {
            this.tag = Tag.NBT(key);
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
