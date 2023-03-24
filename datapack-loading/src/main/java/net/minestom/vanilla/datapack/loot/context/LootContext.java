package net.minestom.vanilla.datapack.loot.context;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.random.RandomGenerator;

// Information Source: https://minecraft.fandom.com/wiki/Loot_table#Loot_context_types
public interface LootContext extends Traits {

    // Traits
    interface Trait<T> {
        String id();
        Function<Object, @Nullable T> finder();
        default <N> Trait<N> map(Function<T, @Nullable N> mapper) {
            return new MappedTraitImpl<>(this, mapper);
        }
    }
    <T> @Nullable T get(Trait<T> trait);
    default <T> T getOrThrow(Trait<T> trait) {
        T value = get(trait);
        if (value == null) throw new IllegalStateException("LootContext does not have trait " + trait.id());
        return value;
    }


    //     Not used. Supplies no loot context parameters.
    //    Specifying "type":"empty" means no context parameters can be used in this loot table.
    record Empty() implements Util.EmptyLootContext {
    }

    // Opening of a container with loot table (can be barrel, chest, trapped chest, hopper, minecart with chest,
    // boat with chest, minecart with hopper, dispenser, dropper, and shulker box).
    // The command /loot … loot <loot_table>
    record Chest(Point origin, @Nullable net.minestom.server.entity.Entity entity) implements LootContext {
        private static final Util.LootContextTraitMap<Chest> traitMap = Util.LootContextTraitMap.<Chest>builder()
                .put(ORIGIN, Chest::origin)
                .put(THIS, Chest::entity)
                .build();

        @Override
        public <T> @Nullable T get(Trait<T> trait) {
            return traitMap.obtain(this, trait);
        }
    }

    //     Not used for loot table. Specifying "type":"command" doesn't make sense.
    //    Used internally by commands such as /item modify or /execute (if|unless) predicate.
    record Command(Point origin, @Nullable net.minestom.server.entity.Entity entity) implements LootContext {
        private static final Util.LootContextTraitMap<Command> traitMap = Util.LootContextTraitMap.<Command>builder()
                .put(ORIGIN, Command::origin)
                .put(THIS, Command::entity)
                .build();

        @Override
        public <T> @Nullable T get(Trait<T> trait) {
            return traitMap.obtain(this, trait);
        }
    }

    //     Not used for loot table. Specifying "type":"selector" doesn't make sense.
    //    Used internally by the predicate target selector argument.
    record Selector(Point origin, net.minestom.server.entity.Entity entity) implements LootContext {
        private static final Util.LootContextTraitMap<Selector> traitMap = Util.LootContextTraitMap.<Selector>builder()
                .put(ORIGIN, Selector::origin)
                .put(THIS, Selector::entity)
                .build();

        @Override
        public <T> @Nullable T get(Trait<T> trait) {
            return traitMap.obtain(this, trait);
        }
    }

    //     Fishing.
    //    The command /loot … fish <loot_table>.
    record Fishing(Point origin, ItemStack tool, @Nullable net.minestom.server.entity.Entity entity) implements LootContext {
        private static final Util.LootContextTraitMap<Fishing> traitMap = Util.LootContextTraitMap.<Fishing>builder()
                .put(ORIGIN, Fishing::origin)
                .put(TOOL, Fishing::tool)
                .put(THIS, Fishing::entity)
                .build();

        @Override
        public <T> @Nullable T get(Trait<T> trait) {
            return traitMap.obtain(this, trait);
        }
    }

    //    Loots from a living entity's death.
    //    The command /loot … kill <target>.
    record Entity(Point origin, net.minestom.server.entity.Entity entity, DamageType damageSource,
                  @Nullable net.minestom.server.entity.Entity killer,
                  @Nullable net.minestom.server.entity.Entity directKiller,
                  @Nullable net.minestom.server.entity.Player killerPlayer) implements LootContext {
        private static final Util.LootContextTraitMap<Entity> traitMap = Util.LootContextTraitMap.<Entity>builder()
                .put(ORIGIN, Entity::origin)
                .put(THIS, Entity::entity)
                .put(DAMAGE_SOURCE, Entity::damageSource)
                .put(KILLER_ENTITY, Entity::killer)
                .put(DIRECT_KILLER, Entity::directKiller)
                .put(KILLER_PLAYER, Entity::killerPlayer)
                .build();

        @Override
        public <T> @Nullable T get(Trait<T> trait) {
            return traitMap.obtain(this, trait);
        }
    }

    // Using a brush on suspicious sand that has a loot table.
    record Archeology(Point origin, @Nullable net.minestom.server.entity.Player entity) implements LootContext {
        private static final Util.LootContextTraitMap<Archeology> traitMap = Util.LootContextTraitMap.<Archeology>builder()
                .put(ORIGIN, Archeology::origin)
                .put(THIS, Archeology::entity)
                .build();

        @Override
        public <T> @Nullable T get(Trait<T> trait) {
            return traitMap.obtain(this, trait);
        }
    }

    // Gift from a cat or villager.
    record Gift(Point origin, net.minestom.server.entity.Player entity) implements LootContext {
        private static final Util.LootContextTraitMap<Gift> traitMap = Util.LootContextTraitMap.<Gift>builder()
                .put(ORIGIN, Gift::origin)
                .put(THIS, Gift::entity)
                .build();

        @Override
        public <T> @Nullable T get(Trait<T> trait) {
            return traitMap.obtain(this, trait);
        }
    }

    //    Bartering with piglins.
    record Barter(net.minestom.server.entity.Player entity) implements LootContext {
        private static final Util.LootContextTraitMap<Barter> traitMap = Util.LootContextTraitMap.<Barter>builder()
                .put(THIS, Barter::entity)
                .build();

        @Override
        public <T> @Nullable T get(Trait<T> trait) {
            return traitMap.obtain(this, trait);
        }
    }

    // Loot table set as an advancement's reward.
    record AdvancementReward(Point origin, net.minestom.server.entity.Player entity) implements LootContext {
        private static final Util.LootContextTraitMap<AdvancementReward> traitMap = Util.LootContextTraitMap.<AdvancementReward>builder()
                .put(ORIGIN, AdvancementReward::origin)
                .put(THIS, AdvancementReward::entity)
                .build();

        @Override
        public <T> @Nullable T get(Trait<T> trait) {
            return traitMap.obtain(this, trait);
        }
    }

    //     Not used for loot table. Specifying "type":"advancement_entity" doesn't make sense.
    //    Used internally by an advancement invokes a predicate.
    record AdvancementEntity(Point origin, net.minestom.server.entity.Player entity) implements LootContext {
        private static final Util.LootContextTraitMap<AdvancementEntity> traitMap = Util.LootContextTraitMap.<AdvancementEntity>builder()
                .put(ORIGIN, AdvancementEntity::origin)
                .put(THIS, AdvancementEntity::entity)
                .build();

        @Override
        public <T> @Nullable T get(Trait<T> trait) {
            return traitMap.obtain(this, trait);
        }
    }

    //     Not used. Supplies all loot context parameters.
    //    Specifying "type":"generic" or omitting it means no checking for context parameters in this loot table when loading the data pack.
    record Generic() implements Util.EmptyLootContext {
    }

    //     Loots from breaking a block.
    //    The command /loot … mine <pos>.
    record Block(net.minestom.server.instance.block.Block blockState, Point origin, ItemStack tool,
                 @Nullable net.minestom.server.entity.Player entity,
                 @Nullable net.minestom.server.instance.block.Block blockEntity, @Nullable Double explosionRadius) implements LootContext {
        private static final Util.LootContextTraitMap<Block> traitMap = Util.LootContextTraitMap.<Block>builder()
                .put(BLOCK_STATE, Block::blockState)
                .put(ORIGIN, Block::origin)
                .put(TOOL, Block::tool)
                .put(THIS, Block::entity)
                .put(BLOCK_ENTITY, Block::blockEntity)
                .put(EXPLOSION_RADIUS, Block::explosionRadius)
                .build();

        @Override
        public <T> @Nullable T get(Trait<T> trait) {
            return traitMap.obtain(this, trait);
        }
    }
}
