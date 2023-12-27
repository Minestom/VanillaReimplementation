package net.minestom.vanilla.tag;

import net.minestom.server.color.DyeColor;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.NamespaceID;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTInt;
import org.jglrxavpok.hephaistos.nbt.NBTString;

import java.util.List;
import java.util.Map;

public interface VanillaTags {


    interface ItemStack {
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
}
