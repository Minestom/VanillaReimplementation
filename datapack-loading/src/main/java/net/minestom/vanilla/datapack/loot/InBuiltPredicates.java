package net.minestom.vanilla.datapack.loot;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Enchantment;
import net.minestom.server.item.ItemMeta;
import net.minestom.vanilla.datapack.loot.context.LootContext;
import net.minestom.vanilla.datapack.number.NumberProvider;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

interface InBuiltPredicates {

    /**
     * alternative—Evaluates a list of predicates and passes if any one of them passes. Invokable from any context.
     * • terms: The list of predicates to evaluate. A predicate within this array must be an object.
     * - A predicate, following this structure recursively.
     */
    record Alternative(List<Predicate> terms) implements Predicate {
        @Override
        public String condition() {
            return "alternative";
        }

        @Override
        public boolean test(LootContext context) {
            for (Predicate term : terms) {
                if (term.test(context)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * block_state_property—Checks the mined block and its block states. Requires block state provided by loot context, and always fails if not provided.
     * • block: A block ID. The test fails if the block doesn't match.
     * • properties: (Optional) A map of block state names to values. Errors if the block doesn't have these properties.
     * • name: A block state and a exact value. The value is a string.
     * OR
     * • name: A block state name and a ranged value to match.
     * • min: The min value.
     * • max: The max value.
     */
    record BlockStateProperty(Block block, @Nullable Map<Block, Property> properties) implements Predicate {
        @Override
        public String condition() {
            return "block_state_property";
        }

        @Override
        public boolean test(LootContext context) {
            if (properties == null) return false;

            Block minedBlock = context.get(LootContext.BLOCK_STATE);
            if (minedBlock == null) return false;

            Property property = properties.get(minedBlock);
            if (property == null) return false;

            return property.test(minedBlock);
        }

        // if (properties == null) return false;

        interface Property {
            boolean test(Block block);

            record Value(Block block) implements Property {
                @Override
                public boolean test(Block block) {
                    return this.block.compare(block);
                }
            }

            record Range(Block min, Block max) implements Property {
                @Override
                public boolean test(Block block) {
                    short minStateId = min.stateId();
                    short maxStateId = max.stateId();
                    short blockStateId = block.stateId();
                    return minStateId <= blockStateId && blockStateId < maxStateId;
                }
            }
        }
    }

    /**
     * damage_source_properties—Checks properties of damage source. Requires origin and damage source provided by loot context, and always fails if not provided.
     * • predicate: Predicate applied to the damage source.
     * - Tags common to all damage types
     */
    record DamageSourceProperties(Map<String, Object> predicate) implements Predicate {
        @Override
        public String condition() {
            return "damage_source_properties";
        }

        @Override
        public boolean test(LootContext context) {
            // TODO: Implement conditions
            return false;
        }
    }

    /**
     * entity_properties—Checks properties of an entity. Invokable from any context.
     * • entity: The entity to check. Specifies an entity from loot context. Can be this, killer, direct_killer, or killer_player.
     * • predicate: Predicate applied to entity, uses same structure as advancements.
     * - All possible conditions for entities
     */
    record EntityProperties(LootContext.Trait<Entity> entity, Map<String, Object> predicate) implements Predicate {
        @Override
        public String condition() {
            return "entity_properties";
        }

        @Override
        public boolean test(LootContext context) {
            // TODO: Implement conditions
            return false;
        }
    }

    /**
     * entity_scores—Checks the scoreboard scores of an entity. Requires the specified entity provided by loot context, and always fails if not provided.
     * • entity: The entity to check. Specifies an entity from loot context. Can be this, killer, direct_killer, or killer_player.
     * • scores: Scores to check. All specified scores must pass for the condition to pass.
     * • A score: Key name is the objective while the value specifies a range of score values required for the condition to pass.
     * + min: A number Provider. Minimum score.
     * + max: A number Provider. Maximum score.
     * OR
     * • A score: Shorthand version of the other syntax above, to check the score against a single number only. Key name is the objective while the value is the required score.
     */
    record EntityScores(LootContext.Trait<Entity> entity, Map<String, Score> scores) implements Predicate {
        @Override
        public String condition() {
            return "entity_scores";
        }

        @Override
        public boolean test(LootContext context) {
            // TODO: Implement conditions
            return false;
        }

        public sealed interface Score {
            boolean test();

            record Value(int value) implements Score {
                @Override
                public boolean test() {
                    return false;
                }
            }

            record Range(NumberProvider min, NumberProvider max) implements Score {
                @Override
                public boolean test() {
                    return false;
                }
            }
        }
    }

    /**
     * inverted—Inverts another loot table condition. Invokable from any context.
     * • term: The condition to be negated, following the same structure as outlined here, recursively.
     */
    record Inverted(Predicate term) implements Predicate {
        @Override
        public String condition() {
            return "inverted";
        }

        @Override
        public boolean test(LootContext context) {
            return !term.test(context);
        }
    }

    /**
     * killed_by_player—Checks if there is a killer_player entity provided by loot context. Requires killer_player entity provided by loot context, and always fails if not provided.
     */
    record KilledByPlayer() implements Predicate {
        @Override
        public String condition() {
            return "killed_by_player";
        }

        @Override
        public boolean test(LootContext context) {
            return context.get(LootContext.KILLER_PLAYER) != null;
        }
    }

    /**
     * location_check—Checks the current location against location criteria. Requires origin provided by loot context, and always fails if not provided.
     * • offsetX - optional offsets to location
     * • offsetY - optional offsets to location
     * • offsetZ - optional offsets to location
     * • predicate: Predicate applied to location, uses same structure as advancements.
     * - Tags common to all locations
     */
    record LocationCheck(int offsetX, int offsetY, int offsetZ, Predicate predicate) implements Predicate {
        @Override
        public String condition() {
            return "location_check";
        }

        @Override
        public boolean test(LootContext context) {
            // TODO: Implement conditions
            return false;
        }
    }

    /**
     * match_tool—Checks tool used to mine the block. Requires tool provided by loot context, and always fails if not provided.
     * • predicate: Predicate applied to item, uses same structure as advancements.
     * - All possible conditions for items
     */
    record MatchTool(Predicate predicate) implements Predicate {
        @Override
        public String condition() {
            return "match_tool";
        }

        @Override
        public boolean test(LootContext context) {
            // TODO: Implement conditions
            return false;
        }
    }

    /**
     * random_chance—Generates a random number between 0.0 and 1.0, and checks if it is less than a specified value. Invokable from any context.
     * • chance: Success rate as a number 0.0–1.0.
     */
    record RandomChance(float chance) implements Predicate {
        @Override
        public String condition() {
            return "random_chance";
        }

        @Override
        public boolean test(LootContext context) {
            return ThreadLocalRandom.current().nextFloat() < chance;
        }
    }

    /**
     * random_chance_with_looting—Generates a random number between 0.0 and 1.0, and checks if it is less than a specified value which has been affected by the level of Looting on the killer entity. Requires killer entity provided by loot context, and if not provided, the looting level is regarded as 0.
     * • chance: Base success rate.
     * • looting_multiplier: Looting adjustment to the base success rate. Formula is chance + (looting_level * looting_multiplier).
     */
    record RandomChanceWithLooting(float chance, float lootingMultiplier) implements Predicate {
        @Override
        public String condition() {
            return "random_chance_with_looting";
        }

        @Override
        public boolean test(LootContext context) {
            double random = ThreadLocalRandom.current().nextDouble();

            int looting = 0;

            Player player = context.get(LootContext.KILLER_PLAYER);
            if (player != null) {
                ItemMeta meta = player.getItemInMainHand().meta();
                looting = meta.getEnchantmentMap().getOrDefault(Enchantment.LOOTING, (short) 0);
            }

            return random < chance + (looting * lootingMultiplier);
        }
    }

    /**
     * reference—Invokes a predicate file and returns its result. Invokable from any context.
     * • name: The resource location of the predicate to invoke. A cyclic reference causes a parsing failure.
     */
    record Reference(String name) implements Predicate {
        @Override
        public String condition() {
            return "reference";
        }

        @Override
        public boolean test(LootContext context) {
            // TODO: Implement conditions
            return false;
        }
    }

    /**
     * survives_explosion—Returns success with 1 ÷ explosion radius probability. Requires explosion radius provided by loot context, and always success if not provided.
     */
    record SurvivesExplosion() implements Predicate {
        @Override
        public String condition() {
            return "survives_explosion";
        }

        @Override
        public boolean test(LootContext context) {
            return ThreadLocalRandom.current().nextFloat() < 1.0 / context.getOrThrow(LootContext.EXPLOSION_RADIUS);
        }
    }

    /**
     * table_bonus—Passes with probability picked from a list, indexed by enchantment power. Requires tool provided by loot context. If not provided, the enchantment level is regarded as 0.
     * • enchantment: Resource location of enchantment.
     * • chances: List of probabilities for enchantment power, indexed from 0.
     */
    record TableBonus(Enchantment enchantment, List<Float> chances) implements Predicate {
        @Override
        public String condition() {
            return "table_bonus";
        }

        @Override
        public boolean test(LootContext context) {
            ItemMeta meta = context.getOrThrow(LootContext.TOOL).meta();
            int level = meta.getEnchantmentMap().getOrDefault(enchantment, (short) 0);
            return ThreadLocalRandom.current().nextFloat() < chances.get(level);
        }
    }

    /**
     * time_check—Compares the current day time (or rather, 24000 * day count + day time) against given values. Invokable from any context.
     * • value: The time to compare the day time against.
     * • min: A number Provider. The minimum value.
     * • max: A number Provider. The maximum value.
     * OR
     * • value: Shorthand version of value above, used to check for a single value only. Number providers cannot be used in this shorthand form.
     * • period: If present, the day time is first reduced modulo the given number before being checked against value. For example, setting this to 24000 causes the checked time to be equal to the current daytime.
     */
    record TimeCheck(Value value, @Nullable Integer period) implements Predicate {
        @Override
        public String condition() {
            return "time_check";
        }

        @Override
        public boolean test(LootContext context) {
            // TODO: Implement conditions
            return false;
        }

        public sealed interface Value {
            record MinMax(NumberProvider min, NumberProvider max) implements Value {
            }

            record Single(int value) implements Value {
            }
        }
    }

    /**
     * value_check—Compares a number against another number or range of numbers. Invokable from any context.
     * • value: A number Provider. The number to test.
     * • range: The range of numbers to compare value against.
     * • min: A number Provider. The minimum value.
     * • max: A number Provider. The maximum value.
     * OR
     * • range: Shorthand version of range above, used to compare value against a single number only. Number providers cannot be used in this shorthand form.
     */
    record ValueCheck(NumberProvider value, Range range) implements Predicate {
        @Override
        public String condition() {
            return "value_check";
        }

        @Override
        public boolean test(LootContext context) {
            // TODO: Implement conditions
            return false;
        }

        public sealed interface Range {
            record MinMax(NumberProvider min, NumberProvider max) implements Range {
            }

            record Single(int value) implements Range {
            }
        }
    }

    /**
     * weather_check—Checks the current game weather. Invokable from any context.
     * • raining: If true, the condition passes only if it is raining or thundering.
     * • thundering: If true, the condition passes only if it is thundering.
     */
    record WeatherCheck(boolean raining, boolean thundering) implements Predicate {
        @Override
        public String condition() {
            return "weather_check";
        }

        @Override
        public boolean test(LootContext context) {
            // TODO: Implement conditions
            return false;
        }
    }
}
