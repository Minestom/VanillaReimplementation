package net.minestom.vanilla.datapack.worldgen;

import com.squareup.moshi.JsonReader;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.key.Key;
import net.minestom.vanilla.datapack.Datapack;
import net.minestom.vanilla.datapack.DatapackLoader;
import net.minestom.vanilla.datapack.json.JsonUtils;
import net.minestom.vanilla.datapack.json.Optional;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a biome in the game world.
 *
 * @param has_precipitation Determines whether the biome has precipitation or not.
 * @param temperature Controls gameplay features like grass and foliage color, and a height adjusted temperature.
 * @param temperature_modifier (optional, defaults to none) Modifies temperature before calculating the height adjusted temperature.
 * @param downfall Controls grass and foliage color.
 * @param effects Ambient effects in this biome.
 * @param carvers The carvers to use. TODO: Carvers
 * @param features List of generation steps (Can be empty). Usually, there are 11 steps, but any amount is possible. TODO: Features
 * @param creature_spawn_probability (optional) Higher value results in more creatures spawned in world generation.
 * @param spawners (Required, but can be empty. If this object doesn't contain a certain category, mobs in this category do not spawn.) Entity spawning settings.
 * @param spawn_costs (Required, but can be empty. Only mobs listed here use the spawn cost mechanism) See Spawn#Spawn costs for details.
 */
public record Biome(
        boolean has_precipitation,
        float temperature,
        @Optional TemperatureModifier temperature_modifier,
        float downfall,
        Effects effects,
        Carvers carvers,
        Object features,
        @Optional Float creature_spawn_probability,
        Map<MobCategory, List<SpawnerData>> spawners,
        Map<Key, SpawnCost> spawn_costs
) {

    /**
     * Enumeration of temperature modifiers.
     */
    public enum TemperatureModifier {
        none,
        frozen
    }

    /**
     * Represents a sound in the game world.
     */
    public interface Sound {
        Key type();

        /**
         * Reads a Sound from JSON.
         *
         * @param reader The JSON reader.
         * @return The constructed Sound.
         * @throws IOException If an IO error occurs.
         */
        static Sound fromJson(JsonReader reader) throws IOException {
            return JsonUtils.<Sound>typeMap(reader, token -> switch (token) {
                case STRING -> json -> new SoundID(Key.key(json.nextString()));
                case BEGIN_OBJECT -> json -> JsonUtils.unionStringTypeAdapted(json, "type", type -> switch (type) {
                    case "sound_id" -> SoundID.class;
                    case "range" -> Range.class;
                    default -> null;
                });
                default -> null;
            });
        }

        /**
         * Represents a sound with a namespace ID.
         */
        record SoundID(Key value) implements Sound {
            @Override
            public Key type() {
                return Key.key("sound_id");
            }
        }

        /**
         * Represents a sound with a range.
         */
        record Range(@Optional Float value) implements Sound {
            @Override
            public Key type() {
                return Key.key("range");
            }
        }
    }

    /**
     * Represents the effects of a biome.
     */
    public record Effects(
            int fog_color,
            int sky_color,
            int water_color,
            int water_fog_color,
            @Optional Integer foliage_color,
            @Optional Integer grass_color,
            @Optional GrassColorModifier grass_color_modifier,
            @Optional Particle particle,
            @Optional Sound ambient_sound,
            @Optional MoodSound mood_sound,
            @Optional AdditionsSound additions_sound,
            @Optional Music music
    ) {

        /**
         * Represents a particle effect in the game world.
         */
        public record Particle(float probability, Options options) {

            /**
             * Represents options for particle effects.
             */
            public interface Options {
                /**
                 * Gets the namespaced ID of the particle type.
                 *
                 * @return The namespaced ID of the particle type.
                 */
                Key type();

                /**
                 * Reads options from JSON.
                 *
                 * @param reader The JSON reader.
                 * @return The constructed Options.
                 * @throws IOException If an IO error occurs.
                 */
                static Options fromJson(JsonReader reader) throws IOException {
                    return JsonUtils.unionStringTypeAdapted(reader, "type", type -> switch (type) {
                        case "minecraft:block" -> Block.class;
                        case "minecraft:block_marker" -> BlockMarker.class;
                        case "minecraft:falling_dust" -> FallingDust.class;
                        case "minecraft:item" -> Item.class;
                        case "minecraft:dust" -> Dust.class;
                        case "minecraft:dust_color_transition" -> DustColorTransition.class;
                        case "minecraft:sculk_charge" -> SculkCharge.class;
                        case "minecraft:vibration" -> Vibration.class;
                        case "minecraft:shriek" -> Shriek.class;
                        default -> Generic.class;
                    });
                }

                /**
                 * Represents a block particle.
                 */
                record Block(BlockState value) implements Options {
                    @Override
                    public Key type() {
                        return Key.key("block");
                    }
                }

                /**
                 * Represents a block marker particle.
                 */
                record BlockMarker(BlockState value) implements Options {
                    @Override
                    public Key type() {
                        return Key.key("block_marker");
                    }
                }

                /**
                 * Represents a falling dust particle.
                 */
                record FallingDust(BlockState value) implements Options {
                    @Override
                    public Key type() {
                        return Key.key("falling_dust");
                    }
                }

                /**
                 * Represents an item particle.
                 */
                record Item(Value value) implements Options {

                    /**
                     * Represents the value of an item particle.
                     */
                    record Value(Key id, int count, CompoundBinaryTag tag) {

                    }

                    @Override
                    public Key type() {
                        return Key.key("item");
                    }
                }

                /**
                 * Represents a dust particle.
                 */
                record Dust(List<Float> color, float scale) implements Options {
                    @Override
                    public Key type() {
                        return Key.key("dust");
                    }
                }

                /**
                 * Represents a dust color transition particle.
                 */
                record DustColorTransition(List<Float> fromColor, List<Float> toColor, float scale) implements Options {
                    @Override
                    public Key type() {
                        return Key.key("dust_color_transition");
                    }
                }

                /**
                 * Represents a sculk charge particle.
                 */
                record SculkCharge(float roll) implements Options {
                    @Override
                    public Key type() {
                        return Key.key("sculk_charge");
                    }
                }

                /**
                 * Represents a vibration particle.
                 */
                record Vibration(PositionSource destination, int arrival_in_ticks) implements Options {
                    @Override
                    public Key type() {
                        return Key.key("vibration");
                    }

                    /**
                     * Represents a position source for the vibration particle.
                     */
                    interface PositionSource {
                        Key type();

                        /**
                         * Reads a PositionSource from JSON.
                         *
                         * @param reader The JSON reader.
                         * @return The constructed PositionSource.
                         * @throws IOException If an IO error occurs.
                         */
                        static PositionSource fromJson(JsonReader reader) throws IOException {
                            return JsonUtils.unionStringTypeAdapted(reader, "type", type -> switch (type) {
                                case "minecraft:block" -> Block.class;
                                case "minecraft:entity" -> Entity.class;
                                default -> null;
                            });
                        }

                        /**
                         * Represents a block position for the vibration particle.
                         */
                        record Block(int x, int y, int z) implements PositionSource {
                            @Override
                            public Key type() {
                                return Key.key("block");
                            }
                        }

                        /**
                         * Represents an entity position source for the vibration particle.
                         */
                        record Entity(UUID source_entity, @Optional Float y_offset) implements PositionSource {
                            @Override
                            public Key type() {
                                return Key.key("entity");
                            }
                        }
                    }
                }

                /**
                 * Represents a shriek particle.
                 */
                record Shriek(int delay) implements Options {
                    @Override
                    public Key type() {
                        return Key.key("shriek");
                    }
                }

                /**
                 * Represents generic particle options.
                 */
                record Generic(Key type) implements Options {
                }
            }
        }

        /**
         * Represents mood sound settings for a biome.
         */
        public record MoodSound(Sound sound, int tick_delay, int block_search_extent, double offset) {
        }

        /**
         * Represents additions sound settings for a biome.
         */
        public record AdditionsSound(Sound sound, double tick_chance) {
        }

        /**
         * Represents music settings for a biome.
         */
        public record Music(Sound sound, int min_delay, int max_delay, boolean replace_current_music) {
        }
    }

    public record Carvers(@Optional CarversList air, @Optional CarversList liquid) {
    }

    public interface CarversList {
        //  air: carver (referenced by ID or inlined), or carver #tag or list (containing either IDs or inlined objects) (Optional; can be empty) — Carvers for the air cave generation step.

        List<Carver> carvers();

        static CarversList fromJson(JsonReader reader) throws IOException {
            return JsonUtils.<CarversList>typeMap(reader, token -> switch (token) {
                case STRING -> json -> new Single.Reference(Key.key(json.nextString()));
                case BEGIN_OBJECT, BEGIN_ARRAY -> json -> {
                    var singleOrList = JsonUtils.SingleOrList.<CarversList.Single>fromJson(CarversList.Single.class, json);
                    if (!singleOrList.isList()) {
                        return new Single.Inlined(singleOrList.asObject().carver());
                    }
                    return new Multiple(singleOrList.asList());
                };
                default -> null;
            });
        }

        interface Single extends CarversList {
            Carver carver();

            default List<Carver> carvers() {
                return List.of(carver());
            }

            static Single fromJson(JsonReader reader) throws IOException {
                return JsonUtils.<Single>typeMap(reader, token -> switch (token) {
                    case STRING -> json -> new Reference(Key.key(json.nextString()));
                    case BEGIN_OBJECT -> json -> new Inlined(DatapackLoader.moshi(Carver.class).apply(json));
                    default -> null;
                });
            }

            final class Reference implements Single {
                private final Key id;
                private @Nullable Carver carver = null;

                public Reference(Key id) {
                    this.id = id;
                    DatapackLoader.loading().whenFinished(finisher -> {
                        for (var entry : finisher.datapack().namespacedData().entrySet()) {
                            String namespace = entry.getKey();

                            var carvers = entry.getValue().world_gen().configured_carver();
                            for (String file : carvers.files()) {
                                var carver = carvers.file(file);

                                Key carverId = Key.key(namespace, file.substring(0, file.length() - ".json".length()));
                                if (carverId.equals(id)) {
                                    Reference.this.carver = carver;
                                    return;
                                }
                            }
                        }
                    });
                }

                @Override
                public Carver carver() {
                    if (carver == null) {
                        if (DatapackLoader.loading().isStatic()) {
                            throw new IllegalStateException("Cannot load carver in a static context");
                        }
                        throw new IllegalStateException("Carver not loaded yet");
                    }
                    return carver;
                }

                public Key id() {
                    return id;
                }

                @Override
                public boolean equals(Object obj) {
                    if (obj == this) return true;
                    if (obj == null || obj.getClass() != this.getClass()) return false;
                    var that = (Reference) obj;
                    return Objects.equals(this.id, that.id);
                }

                @Override
                public int hashCode() {
                    return Objects.hash(id);
                }

                @Override
                public String toString() {
                    return "Reference[" +
                            "id=" + id + ']';
                }
            }

            record Inlined(Carver carver) implements Single {
            }
        }

        record Multiple(List<CarversList.Single> singles) implements CarversList {
            @Override
            public List<Carver> carvers() {
                return singles.stream().map(CarversList.Single::carver).toList();
            }
        }
    }

    /**
     * The spawner data for a single mob.
     * @param type The namespaced entity id of the mob.
     * @param weight How often this mob should spawn, higher values produce more spawns.
     * @param minCount The minimum count of mobs to spawn in a pack. Must be greater than 0.
     * @param maxCount The maximum count of mobs to spawn in a pack. Must be greater than 0. And must be not less than  minCount.
     */
    public record SpawnerData(Key type, int weight, int minCount, int maxCount) {
    }

    public enum MobCategory {
        monster,
        creature,
        ambient,
        water_creature,
        underground_water_creature,
        water_ambient,
        misc,
        axolotls
    }

    /**
     * Represents the spawn costs for a mob.
     * @param energy_budget New mob's maximum potential.
     * @param charge New mob's charge.
     */
    public record SpawnCost(double energy_budget, double charge) {
    }

    /**
     * Enumeration of grass color modifiers.
     */
    public enum GrassColorModifier {
        none,
        dark_forest,
        swamp
    }
}
