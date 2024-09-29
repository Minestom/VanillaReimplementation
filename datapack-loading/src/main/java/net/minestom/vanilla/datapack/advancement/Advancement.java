package net.minestom.vanilla.datapack.advancement;

import com.squareup.moshi.JsonReader;
import net.kyori.adventure.text.Component;
import net.minestom.server.instance.block.Block;
import net.kyori.adventure.key.Key;
import net.minestom.vanilla.datapack.json.JsonUtils;
import net.minestom.vanilla.datapack.json.Optional;
import net.minestom.vanilla.datapack.loot.function.Predicate;
import net.minestom.vanilla.datapack.tags.ConditionsFor;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public record Advancement(Display display,
                          @Nullable String parent,
                          @Nullable Criteria<?> criteria,
                          @Nullable List<List<String>> requirements,
                          Rewards rewards) {


    public record Display(Display.Icon icon, Component title, @Nullable String frame, String background,
                          Component description, @Nullable Boolean showToast, @Nullable Boolean announceToChat,
                          @Nullable Boolean hidden) {
        public record Icon(Key item, String nbt) {
        }
    }

    public record Criteria<TD extends Conditions>(Trigger<TD> trigger, TD conditions) {
    }

    @SuppressWarnings("unused")
    public interface Trigger<TD> {
        static Trigger<?> from(String trigger) {
            return switch (trigger) {
                case "minecraft:allay_drop_item_on_block" -> ALLAY_DROP_ITEM_ON_BLOCK;
                case "minecraft:avoid_vibration" -> AVOID_VIBRATION;
                case "minecraft:bee_nest_destroyed" -> BEE_NEST_DESTROYED;
                case "minecraft:bred_animals" -> BRED_ANIMALS;
                case "minecraft:brewed_potion" -> BREWED_POTION;
                case "minecraft:changed_dimension" -> CHANGED_DIMENSION;
                case "minecraft:channeled_lightning" -> CHANNELED_LIGHTNING;
                case "minecraft:construct_beacon" -> CONSTRUCT_BEACON;
                case "minecraft:consume_item" -> CONSUME_ITEM;
                case "minecraft:cured_zombie_villager" -> CURED_ZOMBIE_VILLAGER;
                case "minecraft:effects_changed" -> EFFECTS_CHANGED;
                case "minecraft:enchanted_item" -> ENCHANTED_ITEM;
                case "minecraft:enter_block" -> ENTER_BLOCK;
                case "minecraft:entity_hurt_player" -> ENTITY_HURT_PLAYER;
                case "minecraft:entity_killed_player" -> ENTITY_KILLED_PLAYER;
                case "minecraft:filled_bucket" -> FILLED_BUCKET;
                case "minecraft:fishing_rod_hooked" -> FISHING_ROD_HOOKED;
                case "minecraft:hero_of_the_village" -> HERO_OF_THE_VILLAGE;
                case "minecraft:impossible" -> IMPOSSIBLE;
                case "minecraft:inventory_changed" -> INVENTORY_CHANGED;
                case "minecraft:item_durability_changed" -> ITEM_DURABILITY_CHANGED;
                case "minecraft:levitation" -> LEVITATION;
                case "minecraft:location" -> LOCATION;
                case "minecraft:nether_travel" -> NETHER_TRAVEL;
                case "minecraft:placed_block" -> PLACED_BLOCK;
                case "minecraft:player_hurt_entity" -> PLAYER_HURT_ENTITY;
                case "minecraft:player_killed_entity" -> PLAYER_KILLED_ENTITY;
                case "minecraft:recipe_unlocked" -> RECIPE_UNLOCKED;
                case "minecraft:shot_crossbow" -> SHOT_CROSSBOW;
                case "minecraft:slept_in_bed" -> SLEPT_IN_BED;
                case "minecraft:summoned_entity" -> SUMMONED_ENTITY;
                case "minecraft:tame_animal" -> TAME_ANIMAL;
                case "minecraft:tick" -> TICK;
                case "minecraft:used_ender_eye" -> USED_ENDER_EYE;
                case "minecraft:used_totem" -> USED_TOTEM;
                case "minecraft:villager_trade" -> VILLAGER_TRADE;
                default -> throw new IllegalArgumentException("Unknown trigger: " + trigger);
            };
        }

        String trigger();

        // allay_drop_item_on_block
        Trigger<Conditions.AllayDropItemOnBlock> ALLAY_DROP_ITEM_ON_BLOCK = () -> "minecraft:allay_drop_item_on_block";

        // avoid_vibration
        Trigger<Conditions.AvoidVibration> AVOID_VIBRATION = () -> "minecraft:avoid_vibration";

        // bee_nest_destroyed
        Trigger<Conditions.BeeNestDestroyed> BEE_NEST_DESTROYED = () -> "minecraft:bee_nest_destroyed";

        // bred_animals
        Trigger<Conditions.BredAnimals> BRED_ANIMALS = () -> "minecraft:bred_animals";

        // brewed_potion
        Trigger<Conditions.BrewedPotion> BREWED_POTION = () -> "minecraft:brewed_potion";

        // changed_dimension
        Trigger<Conditions.ChangedDimension> CHANGED_DIMENSION = () -> "minecraft:changed_dimension";

        // channeled_lightning
        Trigger<Conditions.ChanneledLightning> CHANNELED_LIGHTNING = () -> "minecraft:channeled_lightning";

        // construct_beacon
        Trigger<Conditions.ConstructBeacon> CONSTRUCT_BEACON = () -> "minecraft:construct_beacon";

        // consume_item
        Trigger<Conditions.ConsumeItem> CONSUME_ITEM = () -> "minecraft:consume_item";

        // cured_zombie_villager
        Trigger<Conditions.CuredZombieVillager> CURED_ZOMBIE_VILLAGER = () -> "minecraft:cured_zombie_villager";

        // effects_changed
        Trigger<Conditions.EffectsChanged> EFFECTS_CHANGED = () -> "minecraft:effects_changed";

        // enchanted_item
        Trigger<Conditions.EnchantedItem> ENCHANTED_ITEM = () -> "minecraft:enchanted_item";

        // enter_block
        Trigger<Conditions.EnterBlock> ENTER_BLOCK = () -> "minecraft:enter_block";

        // entity_hurt_player
        Trigger<Conditions.EntityHurtPlayer> ENTITY_HURT_PLAYER = () -> "minecraft:entity_hurt_player";

        // entity_killed_player
        Trigger<Conditions.EntityKilledPlayer> ENTITY_KILLED_PLAYER = () -> "minecraft:entity_killed_player";

        // fall_from_height
        Trigger<Conditions.FallFromHeight> FALL_FROM_HEIGHT = () -> "minecraft:fall_from_height";

        // filled_bucket
        Trigger<Conditions.FilledBucket> FILLED_BUCKET = () -> "minecraft:filled_bucket";

        // fishing_rod_hooked
        Trigger<Conditions.FishingRodHooked> FISHING_ROD_HOOKED = () -> "minecraft:fishing_rod_hooked";

        // hero_of_the_village
        Trigger<Conditions.HeroOfTheVillage> HERO_OF_THE_VILLAGE = () -> "minecraft:hero_of_the_village";

        // impossible
        Trigger<Conditions.Impossible> IMPOSSIBLE = () -> "minecraft:impossible";

        // inventory_changed
        Trigger<Conditions.InventoryChanged> INVENTORY_CHANGED = () -> "minecraft:inventory_changed";

        // item_durability_changed
        Trigger<Conditions.ItemDurabilityChanged> ITEM_DURABILITY_CHANGED = () -> "minecraft:item_durability_changed";

        // item_used_on_block
        Trigger<Conditions.ItemUsedOnBlock> ITEM_USED_ON_BLOCK = () -> "minecraft:item_used_on_block";

        // kill_mob_near_sculk_catalyst
        Trigger<Conditions.KillMobNearSculkCatalyst> KILL_MOB_NEAR_SCULK_CATALYST = () -> "minecraft:kill_mob_near_sculk_catalyst";

        // killed_by_crossbow
        Trigger<Conditions.KilledByCrossbow> KILLED_BY_CROSSBOW = () -> "minecraft:killed_by_crossbow";

        // levitation
        Trigger<Conditions.Levitation> LEVITATION = () -> "minecraft:levitation";

        // lightning_strike
        Trigger<Conditions.LightningStrike> LIGHTNING_STRIKE = () -> "minecraft:lightning_strike";

        // location
        Trigger<Conditions.Location> LOCATION = () -> "minecraft:location";

        // nether_travel
        Trigger<Conditions.NetherTravel> NETHER_TRAVEL = () -> "minecraft:nether_travel";

        // placed_block
        Trigger<Conditions.PlacedBlock> PLACED_BLOCK = () -> "minecraft:placed_block";

        // player_generates_container_loot
        Trigger<Conditions.PlayerGeneratesContainerLoot> PLAYER_GENERATES_CONTAINER_LOOT = () -> "minecraft:player_generates_container_loot";

        // player_hurt_entity
        Trigger<Conditions.PlayerHurtEntity> PLAYER_HURT_ENTITY = () -> "minecraft:player_hurt_entity";

        // player_interacted_with_entity
        Trigger<Conditions.PlayerInteractedWithEntity> PLAYER_INTERACTED_WITH_ENTITY = () -> "minecraft:player_interacted_with_entity";

        // player_killed_entity
        Trigger<Conditions.PlayerKilledEntity> PLAYER_KILLED_ENTITY = () -> "minecraft:player_killed_entity";

        // recipe_unlocked
        Trigger<Conditions.RecipeUnlocked> RECIPE_UNLOCKED = () -> "minecraft:recipe_unlocked";

        // ride_entity_in_lava
        Trigger<Conditions.RideEntityInLava> RIDE_ENTITY_IN_LAVA = () -> "minecraft:ride_entity_in_lava";

        // shot_crossbow
        Trigger<Conditions.ShotCrossbow> SHOT_CROSSBOW = () -> "minecraft:shot_crossbow";

        // slept_in_bed
        Trigger<Conditions.SleptInBed> SLEPT_IN_BED = () -> "minecraft:slept_in_bed";

        // slide_down_block
        Trigger<Conditions.SlideDownBlock> SLIDE_DOWN_BLOCK = () -> "minecraft:slide_down_block";

        // started_riding
        Trigger<Conditions.StartedRiding> STARTED_RIDING = () -> "minecraft:started_riding";

        // summoned_entity
        Trigger<Conditions.SummonedEntity> SUMMONED_ENTITY = () -> "minecraft:summoned_entity";

        // tame_animal
        Trigger<Conditions.TameAnimal> TAME_ANIMAL = () -> "minecraft:tame_animal";

        // target_hit
        Trigger<Conditions.TargetHit> TARGET_HIT = () -> "minecraft:target_hit";

        // thrown_item_picked_up_by_entity
        Trigger<Conditions.ThrownItemPickedUpByEntity> THROWN_ITEM_PICKED_UP_BY_ENTITY = () -> "minecraft:thrown_item_picked_up_by_entity";

        // thrown_item_picked_up_by_player
        Trigger<Conditions.ThrownItemPickedUpByPlayer> THROWN_ITEM_PICKED_UP_BY_PLAYER = () -> "minecraft:thrown_item_picked_up_by_player";

        // tick
        Trigger<Conditions.Tick> TICK = () -> "minecraft:tick";

        // used_ender_eye
        Trigger<Conditions.UsedEnderEye> USED_ENDER_EYE = () -> "minecraft:used_ender_eye";

        // used_totem
        Trigger<Conditions.UsedTotem> USED_TOTEM = () -> "minecraft:used_totem";

        // using_item
        Trigger<Conditions.UsingItem> USING_ITEM = () -> "minecraft:using_item";

        // villager_trade
        Trigger<Conditions.VillagerTrade> VILLAGER_TRADE = () -> "minecraft:villager_trade";

        // voluntary_exile
        Trigger<Conditions.VoluntaryExile> VOLUNTARY_EXILE = () -> "minecraft:voluntary_exile";

        static Trigger<?> fromJson(JsonReader reader) throws IOException {
            String trigger = reader.nextString();
            return Advancement.Trigger.from(trigger);
        }
    }

    public sealed interface Conditions {

        // Triggers when an allay drops an item on a block. Available extra conditions:
        //
        //     conditions:
        //         location: The location at the center of the block the item was dropped on.
        //            Tags common to all locations[
        //
        //    ]
        //
        // item: The item dropped on the block.
        //
        //    All possible conditions for items[
        //
        //]
        record AllayDropItemOnBlock(ConditionsFor.Location location, ConditionsFor.Item item) implements Conditions {
        }

        // Triggers when a vibration event is ignored because the source player is crouching. No extra conditions.
        record AvoidVibration() implements Conditions {
        }

        // Triggers when the player breaks a bee nest or beehive. Available extra conditions:
        //
        //     conditions:
        //         block: Checks the block that was destroyed. Accepts block IDs.
        //         item: The item used to break the block.
        //            All possible conditions for items[
        //
        //    ]
        //
        // num_bees_inside: The number of bees that were inside the bee nest/beehive before it was broken.
        //
        //     num_bees_inside: Another form for  num_bees_inside.
        //         max: The maximum value.
        //         min: The minimum value.
        record BeeNestDestroyed(Block block, ConditionsFor.Item item, Count num_bees_inside) implements Conditions {
            public sealed interface Count {
                record Value(int value) implements Count {
                }

                record Range(int min, int max) implements Count {
                }
            }
        }


        // Triggers after the player breeds 2 animals. Available extra conditions:
        //
        //     conditions:
        //         child: Checks properties of the child that results from the breeding.
        //            All possible conditions for entities[
        //
        //    ]
        //
        // child: Another format for "child". Specifies a list of predicates that must pass in order for the criterion to be granted. The origin of the predicate is the position of the player that would get the advancement.
        //
        //    : A single predicate.
        //
        // parent: The parent.
        //
        //    All possible conditions for entities[
        //
        //    ]
        //
        // parent: Another format for "parent". Specifies a list of predicates that must pass in order for the criterion to be granted. The origin of the predicate is the position of the player that would get the advancement.
        //
        //    : A single predicate.
        //
        // partner: The partner (The entity the parent was bred with).
        //
        //    All possible conditions for entities[
        //
        //    ]
        //
        // partner: Another format for "partner". Specifies a list of predicates that must pass in order for the criterion to be granted. The origin of the predicate is the position of the player that would get the advancement.
        //
        //    : A single predicate.
        record BredAnimals(JsonUtils.ObjectOrList<ConditionsFor.Entity, Predicate> child,
                           JsonUtils.ObjectOrList<ConditionsFor.Entity, Predicate> parent,
                           JsonUtils.ObjectOrList<ConditionsFor.Entity, Predicate> partner) implements Conditions {
        }

        // minecraft:brewed_potion
        //
        //Triggers after the player takes any item out of a brewing stand. Available extra conditions:
        //
        //     conditions:
        //         potion: A brewed potion ID.
        record BrewedPotion(Key potion) implements Conditions {
        }

        // Triggers after the player travels between two dimensions. Available extra conditions:
        //
        //     conditions:
        //         from: The dimension the entity traveled from. This tag is a resource location for a dimension (only these in vanilla; more can be added with data packs).
        //         to: The dimension the entity traveled to. Same accepted values as above.
        record ChangedDimension(Key from, Key to) implements Conditions {
        }

        // Triggers after the player successfully uses the Channeling enchantment on an entity or a lightning rod. Available extra conditions:
        //
        //     conditions:
        //         victims: The victims hit by the lightning summoned by the Channeling enchantment. All entities in this list must be hit.
        //            : A victim.
        //                All possible conditions for entities[
        //
        //    ]
        //
        //: Another format for the victim. Specifies a list of predicates that must pass in order for the criterion to be granted. The checks are applied to the victim hit by the lighting, with the origin being the position of the player that would get the advancement.
        //
        //    : A single predicate.
        record ChanneledLightning(List<JsonUtils.ObjectOrList<ConditionsFor.Entity, Predicate>> victims) implements Conditions {
        }

        // Triggers after the player changes the structure of a beacon. (When the beacon updates itself). Available extra conditions:
        //
        //     conditions:
        //         level: The level of the updated beacon structure.
        //         level: Another format.
        //             max: The maximum value.
        //             min: The minimum value.
        record ConstructBeacon(Count level) implements Conditions {
            public sealed interface Count {
                record Value(int value) implements Count {
                }

                record Range(int min, int max) implements Count {
                }
            }
        }

        // Triggers when the player consumes an item. Available extra conditions:
        //
        //     conditions:
        //         item: The item that was consumed.
        //            All possible conditions for items[
        //
        //]
        record ConsumeItem(ConditionsFor.Item item) implements Conditions {
        }

        // Triggers when the player cures a zombie villager. Available extra conditions:
        //
        //     conditions:
        //         villager: The villager that is the result of the conversion. The 'type' tag is redundant since it will always be "villager".
        //            All possible conditions for entities[
        //
        //    ]
        //
        // villager: Another format for "villager". Specifies a list of predicates that must pass in order for the criterion to be granted. The checks are applied to the villager, with the origin being the position of the player that would get the advancement.
        //
        //    : A single predicate.
        //
        // zombie: The zombie villager right before the conversion is complete (not when it is initiated). The 'type' tag is redundant since it will always be "zombie_villager".
        //
        //    All possible conditions for entities[
        //
        //    ]
        //
        // zombie: Another format for "zombie". Specifies a list of predicates that must pass in order for the criterion to be granted. The checks are applied to the zombie villager, with the origin being the position of the player that would get the advancement.
        //
        //    : A single predicate.
        record CuredZombieVillager(JsonUtils.ObjectOrList<ConditionsFor.Entity, Predicate> villager,
                                   JsonUtils.ObjectOrList<ConditionsFor.Entity, Predicate> zombie) implements Conditions {
        }

        // Triggers after the player gets a status effect applied or taken from them. Available extra conditions:
        //
        //     conditions:
        //         effects: A list of active status effects the player currently has.
        //             <minecraft:effect_name>: The key name is a status effect name.
        //                 ambient: Whether the effect is from a beacon.
        //                 amplifier: The effect amplifier.
        //                 amplifier: Another format.
        //                     max: The maximum value.
        //                     min: The minimum value.
        //                 duration: The effect duration in ticks.
        //                 duration: Another format.
        //                     max: The maximum value.
        //                     min: The minimum value.
        //                 visible: Whether the effect has visible particles.
        //         source: The entity that was the source of the status effect. When there is no entity or when the effect was self-applied or removed, the test passes only if the source is not specified.
        //            All possible conditions for entities[
        //
        //    ]
        //
        // source: Another format for "source". Specifies a list of predicates that must pass in order for the criterion to be granted. The checks are applied to the source, with the origin being the position of the player that would get the advancement.
        //
        //    : A single predicate.
        record EffectsChanged(Map<Key, Effect> effects, JsonUtils.ObjectOrList<ConditionsFor.Entity, Predicate> source) implements Conditions {

            public record Effect(boolean ambient, Count amplifier, Count duration, boolean visible) {
                public interface Count {
                    record Value(int value) implements Count {
                    }

                    record Range(int min, int max) implements Count {
                    }
                }
            }
        }

        // minecraft:enchanted_item
        //
        //Triggers after the player enchants an item through an enchanting table (does not get triggered through an anvil, or through commands). Available extra conditions:
        //
        //     conditions:
        //         item: The item after it has been enchanted.
        //            All possible conditions for items[
        //
        //    ]
        //
        // levels: The levels spent by the player on the enchantment.
        // levels: Another format.
        //
        //     max: The maximum value.
        //     min: The minimum value.
        record EnchantedItem(ConditionsFor.Item item, Count levels) implements Conditions {
            public interface Count {
                record Value(int value) implements Count {
                }

                record Range(int min, int max) implements Count {
                }
            }
        }

        // minecraft:enter_block
        //
        //Every tick, triggers once for each block the player's hitbox is inside (up to 12 blocks, the maximum number of blocks the player can stand in). Available extra conditions:
        //
        //     conditions:
        //         block: The block that the player is standing in. Accepts block IDs.
        //         state: A map of block property names to values. Errors if the block doesn't have these properties.
        //             key: Block property key and value pair.
        //             key: Another format.
        //                 max: A maximum value.
        //                 min: A minimum value.
        record EnterBlock(Block block, Map<String, Count> state) implements Conditions {
            public interface Count {
                record Value(int value) implements Count {
                }

                record Range(int min, int max) implements Count {
                }
            }
        }

        // minecraft:entity_hurt_player
        //
        //Triggers after a player gets hurt, even without a source entity. Available extra conditions:
        //
        //     conditions:
        //         damage: Checks the damage done to the player.
        //            Damage tags[
        //
        //]
        record EntityHurtPlayer(ConditionsFor.Damage damage) implements Conditions {
        }

        // minecraft:entity_killed_player
        //
        //Triggers after a living entity kills a player. Available extra conditions:
        //
        //     conditions:
        //         entity: Checks the entity that was the source of the damage that killed the player (for example: The skeleton that shot the arrow).
        //            All possible conditions for entities[
        //
        //    ]
        //
        // entity: Another format for "entity". Specifies a list of predicates that must pass in order for the criterion to be granted. The checks are applied to the entity that kills the player, with the origin being the position of the player that would get the advancement.
        //
        //    : A single predicate.
        //
        // killing_blow: Checks the type of damage that killed the player.
        //
        //    Tags common to all damage types[
        //
        //]
        record EntityKilledPlayer(JsonUtils.ObjectOrList<ConditionsFor.Entity, Predicate> entity,
                                  ConditionsFor.DamageTypes killing_blow) implements Conditions {
        }

        // minecraft:fall_from_height
        //
        //Triggers when a player lands after falling. Available extra conditions:
        //
        //     conditions:
        //         start_position: A location predicate for the last position before the falling started.
        //            Tags common to all locations[
        //
        //    ]
        //
        // distance: The distance between the start position and the player's position.
        //
        //    Distance predicate tags[
        //
        //]
        record FallFromHeight(ConditionsFor.Location start_position, ConditionsFor.Distance distance) implements Conditions {
        }

        // minecraft:filled_bucket
        //
        //Triggers after the player fills a bucket. Available extra conditions:
        //
        //     conditions:
        //         item: The item resulting from filling the bucket.
        //            All possible conditions for items[
        //
        //]
        record FilledBucket(ConditionsFor.Item item) implements Conditions {
        }

        // minecraft:fishing_rod_hooked
        //
        //Triggers after the player successfully catches an item with a fishing rod or pulls an entity with a fishing rod. Available extra conditions:
        //
        //     conditions:
        //         entity: The entity that was pulled, or the fishing bobber if no entity is pulled.
        //            All possible conditions for entities[
        //
        //    ]
        //
        // entity: Another format for "entity". Specifies a list of predicates that must pass in order for the criterion to be granted. The checks are applied to the entity pulled or the bobber, with the origin being the position of the player that would get the advancement.
        //
        //    : A single predicate.
        //
        // item: The item that was caught.
        //
        //    All possible conditions for items[
        //
        //    ]
        //
        // rod: The fishing rod used.
        //
        //    All possible conditions for items[
        //
        //]
        record FishingRodHooked(JsonUtils.ObjectOrList<ConditionsFor.Entity, Predicate> entity, ConditionsFor.Item item,
                                ConditionsFor.Item rod) implements Conditions {
        }

        // minecraft:hero_of_the_village
        //
        //Triggers when a raid ends in victory and the player has attacked at least one raider from that raid. No extra conditions.
        record HeroOfTheVillage() implements Conditions {
        }

        // minecraft:impossible
        //
        //Never triggers. No available conditions.
        record Impossible() implements Conditions {
        }

        // minecraft:inventory_changed
        //
        //Triggers after any changes happen to the player's inventory. Available extra conditions:
        //
        //     conditions:
        //         items: A list of items in the player's inventory. All items in the list must be in the player's inventory, but not all items in the player's inventory have to be in this list.
        //            : An item stack.
        //                All possible conditions for items[
        //
        //        ]
        //
        // slots:
        //
        //     empty: The amount of slots empty in the inventory.
        //     empty: Another format.
        //         max: The maximum value.
        //         min: The minimum value.
        //     full: The amount of slots completely filled (stacksize) in the inventory.
        //     full: Another format.
        //         max: The maximum value.
        //         min: The minimum value.
        //     occupied: The amount of slots occupied in the inventory.
        //     occupied: Another format.
        //         max: The maximum value.
        //         min: The minimum value.
        record InventoryChanged(List<ConditionsFor.Item> items, Slots slots) implements Conditions {
            public record Slots(Count empty, Count full, Count occupied) {
                interface Count {
                    record Value(int value) implements Count {
                    }

                    record Range(int min, int max) implements Count {
                    }
                }
            }
        }

        // minecraft:item_durability_changed
        //
        //Triggers after any item in the inventory has been damaged in any form. Available extra conditions:
        //
        //     conditions:
        //         delta: The change in durability (negative numbers are used to indicate a decrease in durability).
        //         delta: Another format.
        //             max: The maximum value.
        //             min: The minimum value.
        //         durability: The remaining durability of the item.
        //         durability: Another format.
        //             max: The maximum value.
        //             min: The minimum value.
        //         item: The item before it was damaged, allows you to check the durability before the item was damaged.
        //            All possible conditions for items[
        //
        //]
        record ItemDurabilityChanged(Count delta, Count durability, ConditionsFor.Item item) implements Conditions {
            public record Count(Count.Value value, Count.Range range) {
                public record Value(int value) {
                }

                public record Range(int min, int max) {
                }
            }
        }

        // minecraft:item_used_on_block
        //
        //Triggers when the player uses their hand or an item on a block. Available extra conditions:
        //
        //     conditions:
        //         location: The location at the center of the block the item was used on.
        //            Tags common to all locations[
        //
        //    ]
        //
        // item: The item used on the block.
        //
        //    All possible conditions for items[
        //
        //]
        record ItemUsedOnBlock(ConditionsFor.Location location, ConditionsFor.Item item) implements Conditions {
        }

        // minecraft:kill_mob_near_sculk_catalyst
        //
        //Triggers after a player is the source of a mob or player being killed within the range of a sculk catalyst. Available extra conditions:
        //
        //     conditions:
        //         entity: The entity that was killed.
        //            All possible conditions for entities[
        //
        //    ]
        //
        // entity: Another format for "entity". Specifies a list of predicates that must pass in order for the criterion to be granted. The checks are applied to the mob, with the origin being the position of the player that would get the advancement.
        //
        //    : A single predicate.
        //
        // killing_blow: The type of damage that killed an entity.
        //
        //    Tags common to all damage types[
        //
        //]
        record KillMobNearSculkCatalyst(JsonUtils.ObjectOrList<ConditionsFor.Entity, Predicate> entity,
                                       ConditionsFor.Damage killing_blow) implements Conditions {
        }

        // minecraft:killed_by_crossbow
        //
        //Triggers after the player kills a mob or player using a crossbow in ranged combat. Available extra conditions:
        //
        //     conditions:
        //         unique_entity_types: The exact count of types of entities killed.
        //         unique_entity_types: Another format. The acceptable range of count of types of entities killed.
        //             max: The maximum value.
        //             min: The minimum value.
        //         victims: A list of victims. All of the entries must be matched, and one killed entity may match only one entry.
        //            : A killed entities.
        //                All possible conditions for entities[
        //
        //    ]
        //
        //: Another format for the victim. Specifies a list of predicates that must pass in order for the criterion to be granted. The checks are applied to the victim, with the origin being the position of the player that would get the advancement.
        //
        //    : A single predicate.
        record KilledByCrossbow(Count unique_entity_types, List<JsonUtils.ObjectOrList<ConditionsFor.Entity, Predicate>> victims) implements Conditions {
            public interface Count {
                record Value(int value) implements Count {
                }

                record Range(int min, int max) implements Count {
                }
            }
        }

        // minecraft:levitation
        //
        //Triggers when the player has the levitation status effect. Available extra conditions:
        //
        //     conditions:
        //         distance: The distance between the position where the player started levitating and the player's current position.
        //            Distance predicate tags[
        //
        //    ]
        //
        // duration: The duration of the levitation in ticks.
        // duration: Another format.
        //
        //     max: The maximum value.
        //     min: The minimum value.
        record Levitation(ConditionsFor.Distance distance, Count duration) implements Conditions {
            public interface Count {
                record Value(int value) implements Count {
                }

                record Range(int min, int max) implements Count {
                }
            }
        }

        // minecraft:lightning_strike
        //
        //Triggers when a lightning bolt disappears from the world, only for players within a 256 block radius of the lightning bolt. Available extra conditions:
        //
        //     conditions:
        //         lightning: The lightning bolt that disappeared.
        //            All possible conditions for entities[
        //
        //    ]
        //
        // lightning: Another format for "lightning". Specifies a list of predicates that must pass in order for the criterion to be granted. The checks are applied to the lightning, with the origin being the position of the player that would get the advancement.
        //
        //    : A single predicate.
        //
        // bystander: An entity not hurt by the lightning strike but in a certain area around it.
        //
        //    All possible conditions for entities[
        //
        //    ]
        //
        // bystander: Another format for "bystander". Specifies a list of predicates that must pass in order for the criterion to be granted. The checks are applied to the bystander, with the origin being the position of the player that would get the advancement.
        //
        //    : A single predicate.
        record LightningStrike(JsonUtils.ObjectOrList<ConditionsFor.Entity, Predicate> lightning,
                               JsonUtils.ObjectOrList<ConditionsFor.Entity, Predicate> bystander) implements Conditions {
        }

        // minecraft:location
        //
        //Triggers every 20 ticks (1 second). No extra conditions.
        record Location() implements Conditions {
        }

        // minecraft:nether_travel
        //
        //Triggers when the player travels to the Nether and then returns to the Overworld. Available extra conditions:
        //
        //     conditions:
        //         start_position: A location predicate for the last position before the player teleported to the Nether.
        //            Tags common to all locations[
        //
        //    ]
        //
        // distance: The distance between the position where the player teleported to the Nether and the player's position when they returned.
        //
        //    Distance predicate tags[
        //
        //]
        record NetherTravel(ConditionsFor.Location start_position, ConditionsFor.Distance distance) implements Conditions {
        }

        // minecraft:placed_block
        //
        //Triggers when the player places a block. Available extra conditions:
        //
        //     conditions:
        //         block: The block that was placed. Accepts block IDs.
        //         item: The item that was used to place the block before the item was consumed.
        //            All possible conditions for items[
        //
        //    ]
        //
        // location: The location of the block that was placed.
        //
        //    Tags common to all locations[
        //
        //    ]
        //
        // state: A map of block property names to values. Errors if the block doesn't have these properties.
        //
        //     key: Block property key and value pair.
        //     key: Another format.
        //         max: A maximum value.
        //         min: A minimum value.
        record PlacedBlock(Block block, ConditionsFor.Item item, ConditionsFor.Location location, Map<String, Property> state) implements Conditions {
            public interface Property {
                record Value(String value) implements Property {
                }

                record Range(String min, String max) implements Property {
                }
            }
        }

        // minecraft:player_generates_container_loot
        //
        //Triggers when the player generates the contents of a container with a loot table set. Available extra conditions:
        //
        //     conditions:
        //         loot_table*: The resource location of the generated loot table.
        record PlayerGeneratesContainerLoot(Key loot_table) implements Conditions {
        }

        // minecraft:player_hurt_entity
        //
        //Triggers after the player hurts a mob or player. Available extra conditions:
        //
        //     conditions:
        //         damage: The damage that was dealt.
        //            Damage tags[
        //
        //    ]
        //
        // entity: The entity that was damaged.
        //
        //    All possible conditions for entities[
        //
        //    ]
        //
        // entity: Another format for "entity". Specifies a list of predicates that must pass in order for the criterion to be granted. The checks are applied to the entity, with the origin being the position of the player that would get the advancement.
        //
        //    : A single predicate.
        record PlayerHurtEntity(ConditionsFor.Damage damage, JsonUtils.ObjectOrList<ConditionsFor.Entity, Predicate> entity) implements Conditions {
        }

        // minecraft:player_interacted_with_entity
        //
        //Triggers when the player interacts with an entity. Available extra conditions:
        //
        //     conditions:
        //         item: The item which was in the player's hand during interaction.
        //            All possible conditions for items[
        //
        //    ]
        //
        // entity: The entity which was interacted with.
        //
        //    All possible conditions for entities[
        //
        //    ]
        //
        // entity: Another format for "entity". Specifies a list of predicates that must pass in order for the criterion to be granted. The checks are applied to the entity, with the origin being the position of the player that would get the advancement.
        //
        //    : A single predicate.
        record PlayerInteractedWithEntity(ConditionsFor.Item item, JsonUtils.ObjectOrList<ConditionsFor.Entity, Predicate> entity) implements Conditions {
        }

        // minecraft:player_killed_entity
        //
        //Triggers after a player is the source of a mob or player being killed. Available extra conditions:
        //
        //     conditions:
        //         entity: The entity that was killed.
        //            All possible conditions for entities[
        //
        //    ]
        //
        // entity: Another format for "entity". Specifies a list of predicates that must pass in order for the criterion to be granted. The checks are applied to the entity, with the origin being the position of the player that would get the advancement.
        //
        //    : A single predicate.
        //
        // killing_blow: The type of damage that killed an entity.
        //
        //    Tags common to all damage types[
        //
        //]
        record PlayerKilledEntity(JsonUtils.ObjectOrList<ConditionsFor.Entity, Predicate> entity, ConditionsFor.Damage killing_blow) implements Conditions {
        }

        // minecraft:recipe_unlocked
        //
        //Triggers after the player unlocks a recipe (using a knowledge book for example). Available extra conditions:
        //
        //     conditions:
        //         recipe*: The recipe that was unlocked.
        record RecipeUnlocked(Key recipe) implements Conditions {
        }

        // minecraft:ride_entity_in_lava
        //
        //Triggers when a player mounts an entity walking on lava and while the entity moves with them. Available extra conditions:
        //
        //     conditions:
        //         start_position: A location predicate for the last position before the player mounted the entity.
        //            Tags common to all locations[
        //
        //    ]
        //
        // distance: The distance between the start position and the player's position.
        //
        //    Distance predicate tags[
        //
        //]
        record RideEntityInLava(ConditionsFor.Location start_position, ConditionsFor.Distance distance) implements Conditions {
        }

        // minecraft:shot_crossbow
        //
        //Triggers when the player shoots a crossbow. Available extra conditions:
        //
        //     conditions:
        //         item: The crossbow that is used.
        //            All possible conditions for items[
        //
        //]
        record ShotCrossbow(ConditionsFor.Item item) implements Conditions {
        }

        // minecraft:slept_in_bed
        //
        //Triggers when the player enters a bed. No extra conditions.
        record SleptInBed() implements Conditions {
        }

        // minecraft:slide_down_block
        //
        //Triggers when the player slides down a block. Available extra conditions:
        //
        //     conditions:
        //         block: The block that the player slid on.
        //         state: A map of block property names to values. Errors if the block doesn't have these properties.
        //             key: Block property key and value pair.
        //             key: Another format.
        //                 max: A maximum value.
        //                 min: A minimum value.
        record SlideDownBlock(Block block, Map<String, Property> state) implements Conditions {
            public interface Property {
                record Value(String value) implements Property {
                }

                record Range(String min, String max) implements Property {
                }
            }
        }

        // minecraft:started_riding
        //
        //Triggers when the player starts riding a vehicle or an entity starts riding a vehicle currently ridden by the player. No extra conditions.
        record StartedRiding() implements Conditions {
        }

        // minecraft:summoned_entity
        //
        //Triggers after an entity has been summoned. Works with iron golems (pumpkin and iron blocks), snow golems (pumpkin and snow blocks), the ender dragon (end crystals) and the wither (wither skulls and soul sand/soul soil). Using dispensers, commands, or pistons to place the wither skulls or pumpkins will still activate this trigger. Available extra conditions:
        //
        //     conditions:
        //         entity: The summoned entity.
        //            All possible conditions for entities[
        //
        //    ]
        //
        // entity: Another format for "entity". Specifies a list of predicates that must pass in order for the criterion to be granted. The checks are applied to the entity, with the origin being the position of the player that would get the advancement.
        //
        //    : A single predicate.
        record SummonedEntity(JsonUtils.ObjectOrList<ConditionsFor.Entity, Predicate> entity) implements Conditions {
        }

        // minecraft:tame_animal
        //
        //Triggers after the player tames an animal. Available extra conditions:
        //
        //     conditions:
        //         entity: Checks the entity that was tamed.
        //            All possible conditions for entities[
        //
        //    ]
        //
        // entity: Another format for "entity". Specifies a list of predicates that must pass in order for the criterion to be granted. The checks are applied to the entity, with the origin being the position of the player that would get the advancement.
        //
        //    : A single predicate.
        record TameAnimal(JsonUtils.ObjectOrList<ConditionsFor.Entity, Predicate> entity) implements Conditions {
        }

        // minecraft:target_hit
        //
        //Triggers when the player shoots a target block. Available extra conditions:
        //
        //     conditions:
        //         signal_strength: The redstone signal that will come out of the target block.
        //         signal_strength: Another format.
        //             max: The maximum value.
        //             min: The minimum value.
        //         projectile: The projectile hit the target block.
        //            All possible conditions for entities[
        //
        //    ]
        //
        // projectile: Another format for "projectile". Specifies a list of predicates that must pass in order for the criterion to be granted. The checks are applied to the projectile, with the origin being the position of the player that would get the advancement.
        //
        //    : A single predicate.
        record TargetHit(int signal_strength, JsonUtils.ObjectOrList<ConditionsFor.Entity, Predicate> projectile) implements Conditions {
        }

        // minecraft:thrown_item_picked_up_by_entity
        //
        //Triggers after the player throws an item and another entity picks it up. Available extra conditions:
        //
        //     conditions:
        //         item: The thrown item which was picked up.
        //            All possible conditions for items[
        //
        //    ]
        //
        // entity: The entity which picked up the item.
        //
        //    All possible conditions for entities[
        //
        //    ]
        //
        // entity: Another format for "entity". Specifies a list of predicates that must pass in order for the criterion to be granted. The checks are applied to the entity, with the origin being the position of the player that would get the advancement.
        //
        //    : A single predicate.
        record ThrownItemPickedUpByEntity(ConditionsFor.Item item, JsonUtils.ObjectOrList<ConditionsFor.Entity, Predicate> entity) implements Conditions {
        }

        // minecraft:thrown_item_picked_up_by_player
        //
        //Triggers when a player picks up an item thrown by another entity. Available extra conditions:
        //
        //     conditions:
        //         item: The item thrown.
        //            All possible conditions for items[
        //
        //    ]
        //
        // entity: The entity that threw the item.
        //
        //    All possible conditions for entities[
        //
        //    ]
        //
        // entity: Another format for "entity". Specifies a list of predicates that must pass in order for the criterion to be granted. The checks are applied to the entity, with the origin being the position of the player that would get the advancement.
        //
        //    : A single predicate.
        record ThrownItemPickedUpByPlayer(ConditionsFor.Item item, JsonUtils.ObjectOrList<ConditionsFor.Entity, Predicate> entity) implements Conditions {
        }

        // minecraft:tick
        //
        //Triggers every tick (20 times a second). No extra conditions.
        record Tick() implements Conditions {
        }

        // minecraft:used_ender_eye
        //
        //Triggers when the player uses an eye of ender (in a world where strongholds generate). Available extra conditions:
        //
        //     conditions:
        //         distance: The horizontal distance between the player and the stronghold.
        //         distance: Another format.
        //             max: A maximum value.
        //             min: A minimum value.
        record UsedEnderEye(Count distance) implements Conditions {
            public interface Count {
                record Value(double value) implements Count {
                }

                record Range(double min, double max) implements Count {
                }
            }
        }

        // minecraft:used_totem
        //
        //Triggers when the player uses a totem. Available extra conditions:
        //
        //     conditions:
        //         item: The item, only works with totem items.
        //            All possible conditions for items[
        //
        //]
        record UsedTotem(ConditionsFor.Item item) implements Conditions {
        }

        // minecraft:using_item
        //
        //Triggers for every tick that the player uses an item that is used continuously. It is known to trigger for bows, crossbows, honey bottles, milk buckets, potions, shields, spyglasses, tridents, food items, eyes of ender, etc. Most items that activate from a single click, such as fishing rods, do not affect this trigger. Available extra conditions:
        //
        //     conditions:
        //         item: The item that is used.
        //            All possible conditions for items[
        //
        //]
        record UsingItem(ConditionsFor.Item item) implements Conditions {
        }

        // minecraft:villager_trade
        //
        //Triggers after the player trades with a villager or a wandering trader. Available extra conditions:
        //
        //     conditions:
        //         item: The item that was purchased. The "count" tag checks the count from one trade, not multiple.
        //            All possible conditions for items[
        //
        //    ]
        //
        // villager: The villager the item was purchased from.
        //
        //    All possible conditions for entities[
        //
        //    ]
        //
        // villager: Another format for "villager". Specifies a list of predicates that must pass in order for the criterion to be granted. The checks are applied to the villager, with the origin being the position of the player that would get the advancement.
        //
        //    : A single predicate.
        record VillagerTrade(ConditionsFor.Item item, JsonUtils.ObjectOrList<ConditionsFor.Entity, Predicate> villager) implements Conditions {
        }

        // minecraft:voluntary_exile
        //
        //Triggers when the player causes a raid. No extra conditions.
        record VoluntaryExile() implements Conditions {
        }
    }

    public record Rewards(List<Key> recipes, List<Key> loot, @Optional Integer experience, String function) {
    }
}
