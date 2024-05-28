package net.minestom.vanilla.datapack.worldgen;

import com.squareup.moshi.JsonReader;
import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.datapack.json.JsonUtils;
import net.minestom.vanilla.datapack.json.NamespaceTag;

import java.io.IOException;

/**
 * Modifies nbt data of the block entity if all conditions are met.
 */
public sealed interface BlockEntityModifier {
    default NamespaceID type() {
        return JsonUtils.getNamespaceTag(this.getClass());
    }

    static BlockEntityModifier fromJson(JsonReader reader) throws IOException {
        return JsonUtils.sealedUnionNamespace(reader, BlockEntityModifier.class, "type");
    }

    @NamespaceTag("append_loot")
    record AppendLoot(NamespaceID loot_table) implements BlockEntityModifier {
    }

    @NamespaceTag("append_static")
    record AppendStatic(String data) implements BlockEntityModifier {
    }

    @NamespaceTag("clear")
    record Clear() implements BlockEntityModifier {
    }

    @NamespaceTag("passthrough")
    record Passthrough() implements BlockEntityModifier {
    }
}
