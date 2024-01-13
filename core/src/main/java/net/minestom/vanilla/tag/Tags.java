package net.minestom.vanilla.tag;

import net.minestom.server.item.Material;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.NamespaceID;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTByte;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTInt;
import org.jglrxavpok.hephaistos.nbt.NBTString;

import java.util.List;
import java.util.Map;

public interface Tags {


    interface Items {
        Tag<NBT> TAG = Tag.NBT("tag")
                .defaultValue(NBTCompound.EMPTY);
        Tag<NBTCompound> BLOCKSTATE = TAG.path("BlockEntityTag")
                .map(nbt -> nbt instanceof NBTCompound ? (NBTCompound) nbt : NBTCompound.EMPTY, nbt -> nbt)
                .defaultValue(NBTCompound.EMPTY);

        interface Banner {
            record Pattern(String pattern, int color) {
            }

            Tag<List<Pattern>> PATTERNS = BLOCKSTATE.path("Patterns") // TODO: Is this correct?
                    .map(nbt -> new Pattern(
                            nbt.getString("Pattern"),
                            nbt.getInt("Color")
                    ), pattern -> new NBTCompound(Map.of(
                            "Pattern", new NBTString(pattern.pattern()),
                            "Color", new NBTInt(pattern.color())
                    )))
                    .list();
        }

        interface Potion {
            Tag<NamespaceID> POTION = TAG.path("Potion")
                    .map(nbt -> nbt instanceof NBTString nbts ?
                            NamespaceID.from(nbts.getValue()) : NamespaceID.from("minecraft:empty"),
                            nbt -> new NBTString(nbt.toString()));
        }
    }

    interface Blocks {
        interface Campfire {
            record ItemWithSlot(Material material, int slot){
            }

            Tag<List<ItemWithSlot>> ITEMS = Tag.NBT("Items")
                    .map(nbt -> nbt instanceof NBTCompound ? (NBTCompound) nbt : NBTCompound.EMPTY, nbt -> nbt)
                    .map(nbt -> new ItemWithSlot(
                                    Material.fromNamespaceId(nbt.getString("id")),
                                    nbt.getByte("Slot")
                            ),
                            item -> new NBTCompound(Map.of(
                                    "id", new NBTString(item.material.name()),
                                    "Slot", new NBTByte((byte) item.slot),
                                    "Count", new NBTByte((byte) 1)
                            ))).list();
        }
        interface Furnace {
            // The number of ticks that the furnace can cook for
            Tag<Integer> COOKING_TICKS = Tag.Integer("vri:furnace:cooking_ticks").defaultValue(0);

            // The number of ticks that the furnace has been cooking for
            Tag<Integer> COOKING_PROGRESS = Tag.Integer("vri:furnace:cooking_progress").defaultValue(0);

        }
    }
}
