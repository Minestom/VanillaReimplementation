package net.minestom.vanilla.gamedata;

import net.minestom.server.inventory.Inventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.registry.Registries;
import net.minestom.server.utils.nbt.NbtWriter;
import net.querz.nbt.tag.*;

import java.util.HashMap;
import java.util.Map;

// for lack of a better name
public class NBTUtils {

    private static Map<Class<?>, Byte> class2id = new HashMap<>();

    static {
        class2id.put(EndTag.class, EndTag.ID);
        class2id.put(ByteTag.class, ByteTag.ID);
        class2id.put(ShortTag.class, ShortTag.ID);
        class2id.put(IntTag.class, IntTag.ID);
        class2id.put(LongTag.class, LongTag.ID);
        class2id.put(FloatTag.class, FloatTag.ID);
        class2id.put(DoubleTag.class, DoubleTag.ID);
        class2id.put(ByteArrayTag.class, ByteArrayTag.ID);
        class2id.put(StringTag.class, StringTag.ID);
        class2id.put(ListTag.class, ListTag.ID);
        class2id.put(CompoundTag.class, CompoundTag.ID);
        class2id.put(IntArrayTag.class, IntArrayTag.ID);
        class2id.put(LongArrayTag.class, LongArrayTag.ID);
    }

    /**
     * Loads all the items from the 'items' list into the given inventory
     * @param items
     * @param destination
     */
    public static void loadAllItems(ListTag<CompoundTag> items, Inventory destination) {
        // TODO: clear inventory
        for(CompoundTag tag : items) {
            Material item = Registries.getMaterial(tag.getString("id"));
            if(item == Material.AIR) {
                item = Material.STONE;
            }
            ItemStack stack = new ItemStack(item, tag.getByte("Count"));
            destination.setItemStack(tag.getByte("Slot"), stack);
        }
    }

    public static void saveAllItems(ListTag<CompoundTag> list, Inventory inventory) {
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack stack = inventory.getItemStack(i);
            CompoundTag nbt = new CompoundTag();
            nbt.put("Slot", new ByteTag((byte) i));
            nbt.put("Count", new ByteTag(stack.getAmount()));
            nbt.put("id", new StringTag(stack.getMaterial().getName()));

            list.add(nbt);
        }
    }

    public static void writeTag(NbtWriter nbt, String name, Tag<?> tag) {
        if(tag instanceof CompoundTag) {
            nbt.writeCompound(name, writer -> {
                for(var entry : ((CompoundTag) tag).entrySet()) {
                    writeTag(writer, entry.getKey(), entry.getValue());
                }
            });
        } else if(tag instanceof ByteArrayTag) {
            nbt.writeByteArray(name, ((ByteArrayTag) tag).getValue());
        } else if(tag instanceof IntArrayTag) {
            nbt.writeIntArray(name, ((IntArrayTag) tag).getValue());
        } else if(tag instanceof LongArrayTag) {
            nbt.writeLongArray(name, ((LongArrayTag) tag).getValue());
        } else if(tag instanceof ByteTag) {
            nbt.writeByte(name, ((ByteTag) tag).asByte());
        } else if(tag instanceof ShortTag) {
            nbt.writeShort(name, ((ShortTag) tag).asShort());
        } else if(tag instanceof IntTag) {
            nbt.writeInt(name, ((IntTag) tag).asInt());
        } else if(tag instanceof LongTag) {
            nbt.writeLong(name, ((LongTag) tag).asLong());
        } else if(tag instanceof DoubleTag) {
            nbt.writeDouble(name, ((DoubleTag) tag).asDouble());
        } else if(tag instanceof FloatTag) {
            nbt.writeFloat(name, ((FloatTag) tag).asFloat());
        } else if(tag instanceof StringTag) {
            nbt.writeString(name, ((StringTag) tag).getValue());
        } else if(tag instanceof ListTag<?>) {
            ListTag<?> list = (ListTag<?>) tag;
            byte type = getElementType(list);
            PacketWriter writer = nbt.getPacketWriter();
            writer.writeByte(list.getID());
            writer.writeShortSizedString(name);
            basicWrite(writer, list);
        } else if(tag instanceof EndTag) {

        } else {
            throw new UnsupportedOperationException("Unsupported tag class: "+ tag.getClass());
        }
    }

    private static void basicWrite(PacketWriter writer, Tag<?> tag) {
        if(tag instanceof CompoundTag) {
            for(var entry : ((CompoundTag) tag).entrySet()) {
                writer.writeByte(entry.getValue().getID());
                writer.writeShortSizedString(entry.getKey());
                basicWrite(writer, entry.getValue());
            }
            writer.writeByte((byte) 0);
        } else if(tag instanceof ByteArrayTag) {
            writer.writeBytes(((ByteArrayTag) tag).getValue());
        } else if(tag instanceof IntArrayTag) {
            int[] values = ((IntArrayTag) tag).getValue();
            writer.writeInt(values.length);
            for (int v : values) {
                writer.writeInt(v);
            }
        } else if(tag instanceof LongArrayTag) {
            long[] values = ((LongArrayTag) tag).getValue();
            writer.writeInt(values.length);
            for (long v : values) {
                writer.writeLong(v);
            }
        } else if(tag instanceof ByteTag) {
            writer.writeByte(((ByteTag) tag).asByte());
        } else if(tag instanceof ShortTag) {
            writer.writeShort(((ShortTag) tag).asShort());
        } else if(tag instanceof IntTag) {
            writer.writeInt(((IntTag) tag).asInt());
        } else if(tag instanceof LongTag) {
            writer.writeLong(((LongTag) tag).asLong());
        } else if(tag instanceof DoubleTag) {
            writer.writeDouble(((DoubleTag) tag).asDouble());
        } else if(tag instanceof FloatTag) {
            writer.writeFloat(((FloatTag) tag).asFloat());
        } else if(tag instanceof StringTag) {
            writer.writeShortSizedString(((StringTag) tag).getValue());
        } else if(tag instanceof ListTag<?>) {
            ListTag<?> list = (ListTag<?>)tag;
            byte type = getElementType(list);
            writer.writeByte(type);
            writer.writeInt(list.size());
            for (Tag<?> t : list) {
                basicWrite(writer, t);
            }
        } else if(tag instanceof EndTag) {
        } else {
            throw new UnsupportedOperationException("Unsupported tag class: "+tag.getClass());
        }
    }

    private static byte getElementType(ListTag<?> list) {
        return class2id.getOrDefault(list.getTypeClass(), (byte) 0x00);
    }
}
