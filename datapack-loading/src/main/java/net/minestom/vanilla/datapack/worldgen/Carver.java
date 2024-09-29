package net.minestom.vanilla.datapack.worldgen;

import com.squareup.moshi.JsonReader;
import net.minestom.server.instance.block.Block;
import net.kyori.adventure.key.Key;
import net.minestom.vanilla.datapack.DatapackLoader;
import net.minestom.vanilla.datapack.json.JsonUtils;
import net.minestom.vanilla.datapack.json.Optional;

import java.io.IOException;

//  The root object.
//
//     type: The ID of carver type.
//     config: Configuration values for the carver.
public record Carver(Key type, BaseConfig config) {

    public static Carver fromJson(JsonReader reader) throws IOException {
        // carvers are a special case since the config object depends on the type of carver
        String type;
        try (var json = reader.peekJson()) {
            json.beginObject();
            type = JsonUtils.findProperty(json, "type", JsonReader::nextString);
            if (type == null) {
                throw new IOException("Missing type");
            }
        }

        var configReader = switch (type) {
            case "minecraft:cave", "minecraft:nether_cave" -> DatapackLoader.moshi(CaveConfig.class);
            case "minecraft:canyon" -> DatapackLoader.moshi(CanyonConfig.class);
            default -> throw new IOException("Unknown carver type: " + type);
        };
        reader.beginObject();
        var config = JsonUtils.findProperty(reader, "config", configReader);
        reader.endObject();
        if (config == null) {
            throw new IOException("Missing config");
        }
        return new Carver(Key.key(type), config);
    }

    //     cave - Carves a cave. A cave is a long tunnel that sometimes branches. Somtimes one or more tunnels start from a circular void.
    //    nether_cave - Similar to cave, but with a less frequency and wider tunnels. And aquifer doesn't work. The carved blocks below bottom_y + 32.0 are filled with lava.
    //    canyon - Carves a canyon.

    public interface BaseConfig {

        //  probability: The probability that each chunk attempts to generate carvers. Value between 0 and 1 (both inclusive).
        float probability();

        //  y: The height at which this carver attempts to generate.
        HeightProvider y();

        //  lava_level: The Y-level below or equal to which the carved areas are filled with lava. Doesn't affect nether_cave (where lava level is always bottom_y + 31). (This field is seemingly ignored and always set to -56 (MC-237017), needs testing)
        HeightProvider lava_level();

        //  replaceable: Blocks that can be carved. Can be a block ID, a block tag, or a list of block IDs.
        JsonUtils.SingleOrList<Block> replaceable();

        // debug_settings: (optional) Replaces blocks in the carved areas for debugging.
        //
        //     debug_mode: (optional, defauts to false) Enable debug mode for this carver.
        //     air_state: (optional, defaults to acacia button's default state) Replaces air blocks.
        //     water_state: (optional, defaults to acacia button's default state) Replaces water blocks and then waterlogs these blocks.
        //     lava_state: (optional, defaults to acacia button's default state) Replaces lava blocks.
        //     barrier_state: (optional, defaults to acacia button's default state) Replaces barriers of aquifers.
        @Optional
        BaseConfig.DebugSettings debug_settings();

        record DebugSettings(@Optional Boolean debug_mode, @Optional Block air_state,
                                    @Optional Block water_state, @Optional Block lava_state,
                                    @Optional Block barrier_state) {
        }
    }

    public record Config(float probability, HeightProvider y, HeightProvider lava_level,
                   JsonUtils.SingleOrList<Block> replaceable, @Optional BaseConfig.DebugSettings debug_settings) implements BaseConfig {
    }

    // If carver type is cave or nether_cave, additional fields are as follows:
    //
    // yScale: Vertically scales circular voids.
    // vertical_radius_multiplier: Vertically scales cave tunnels. Doesn't affect the length of tunnels.
    // floor_level: Value between -1.0 and 1.0 (both inclusive). Change the shape of the cave's horizontal floor. If 0.0, carves the terrain with ellipsoids. If 1.0, carves with upper semi-ellipsoids, resulting in a level floor.
    public record CaveConfig(float probability, HeightProvider y, HeightProvider lava_level,
                      JsonUtils.SingleOrList<Block> replaceable, @Optional BaseConfig.DebugSettings debug_settings,
                      FloatProvider yScale, FloatProvider vertical_radius_multiplier,
                      FloatProvider floor_level) implements BaseConfig {
    }

    // If carver type is canyon, additional fields are as follows:
    //  yScale: Vertically scales canyons.
    // vertical_rotation: Vertical rotation as a canyon extends.
    //  shape: The shape to use for the ravine.
    public record CanyonConfig(float probability, HeightProvider y, HeightProvider lava_level,
                        JsonUtils.SingleOrList<Block> replaceable, @Optional BaseConfig.DebugSettings debug_settings,
                        FloatProvider yScale, FloatProvider vertical_rotation, Shape shape) implements BaseConfig {
        //  distance_factor: Scales the length of canyons. Higher values make canyons longer.
        // thickness: Scales the breadth and height of canyons.
        // horizontal_radius_factor: Scales the breadth of canyons. Higher values make canyons wider.
        // vertical_radius_default_factor: Vertically scales canyons. Higher values make canyons deeper.
        // vertical_radius_center_factor: Scales the height based on the horizontal distance from the canyon's center, resulting in deeper center.
        // width_smoothness: Higher values smooth canyon walls on the vertical axis. Must be greater than 0.
        public record Shape(
                FloatProvider distance_factor,
                FloatProvider thickness,
                FloatProvider horizontal_radius_factor,
                float vertical_radius_default_factor,
                float vertical_radius_center_factor,
                int width_smoothness) {
        }
    }

}
