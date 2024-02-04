package net.minestom.vanilla.tag;

import net.minestom.server.item.ItemStack;
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
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
                            Objects.requireNonNull(nbt.getInt("Color"))
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
            Tag<List<ItemStack>> ITEMS = Tag.NBT("Items")
                    .map(nbt -> nbt instanceof NBTCompound ? (NBTCompound) nbt : NBTCompound.EMPTY, nbt -> nbt)
                    .list()
                    .map(nbtList -> nbtList.stream()
                                    .map(nbt -> ItemStack.of(Material.fromNamespaceId(nbt.getString("id"))))
                                    .collect(Collectors.toList()),
                            items -> IntStream.range(0, items.size())
                                    .mapToObj(slot -> new NBTCompound(Map.of(
                                            "id", new NBTString(items.get(slot).material().name()),
                                            "Slot", new NBTByte((byte) slot),
                                            "Count", new NBTByte((byte) 1)
                                    )))
                                    .toList());

            // The number of ticks that the campfire has been cooking for, for each of the 4 slots, 0 means end of the cooking
            Tag<List<Integer>> COOKING_PROGRESS = Tag.Integer("vri:campfire:cooking_progress").list().defaultValue(List.of(0, 0, 0, 0));

        }
        interface Smelting {
            // The number of ticks that the furnace can cook for without using another fuel item
            Tag<Integer> COOKING_TICKS = Tag.Integer("vri:furnace:cooking_ticks").defaultValue(0);

            // The last fuel item that was used. Null if this furnace is not currently burning
            Tag<Material> LAST_COOKED_ITEM = Tag.String("vri:furnace:last_cooked_item")
                    .map(Material::fromNamespaceId, mat -> mat.namespace().toString())
                    .defaultValue(Material.AIR);

            // The number of ticks that the furnace has been cooking for
            Tag<Integer> COOKING_PROGRESS = Tag.Integer("vri:furnace:cooking_progress").defaultValue(0);
        }
    }
}
