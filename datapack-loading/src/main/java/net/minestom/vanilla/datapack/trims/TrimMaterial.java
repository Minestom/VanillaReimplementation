package net.minestom.vanilla.datapack.trims;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.minestom.vanilla.datapack.json.Optional;

import java.util.Map;

/**
 *
 The root object
 asset_name: A string which will be used in the resource pack.
 description: A JSON text component used for the tooltip on items. The color
 #258474 is used here.
 override_armor_materials: Optional. Map of armor material to override color palette.
 */
public record TrimMaterial(String asset_name, Component description, @Optional Map<Key, String> override_armor_materials) {
}
