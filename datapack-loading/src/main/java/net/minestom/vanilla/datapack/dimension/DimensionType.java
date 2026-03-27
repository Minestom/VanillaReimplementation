package net.minestom.vanilla.datapack.dimension;

import net.minestom.vanilla.datapack.number.NumberProvider;

// {
//  "ambient_light": 0.0,
//  "coordinate_scale": 1.0,
//  "effects": "minecraft:overworld",
//  "has_ceiling": false,
//  "has_skylight": true,
//  "height": 384,
//  "infiniburn": "#minecraft:infiniburn_overworld",
//  "logical_height": 384,
//  "min_y": -64,
//  "monster_spawn_block_light_limit": 0,
//  "monster_spawn_light_level": {
//    "type": "minecraft:uniform",
//    "value": {
//      "max_inclusive": 7,
//      "min_inclusive": 0
//    }
//  },
//  "natural": true,
//  "piglin_safe": false,
//  "respawn_anchor_works": false,
//  "ultrawarm": false
//}
public record DimensionType(
        double ambient_light,
        double coordinate_scale,
        boolean has_ceiling,
        boolean has_skylight,
        int height,
        String infiniburn,
        int logical_height,
        int min_y,
        int monster_spawn_block_light_limit,
        NumberProvider.Int monster_spawn_light_level
) {
}
