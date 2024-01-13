package net.minestom.vanilla.datapack.trims;

import net.kyori.adventure.text.Component;
import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.datapack.json.Optional;

/**
 *
 The root object
 asset_id: A resource location which will be used in the resource pack.
 description: A JSON text component used for the tooltip on items.
 template_item: The item representing this pattern.
 decal: Optional, defaults to false. If true, the pattern texture will be masked based on the underlying armor.
 */
public record TrimPattern(NamespaceID asset_id, Component description, NamespaceID template_item, @Optional Boolean decal) {
}
