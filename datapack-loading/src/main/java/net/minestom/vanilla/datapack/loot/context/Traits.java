package net.minestom.vanilla.datapack.loot.context;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;

interface Traits {
    LootContext.Trait<Block> BLOCK_STATE = new TraitImpl<>("block_state", Block.class);

    LootContext.Trait<Point> ORIGIN = new TraitImpl<>("origin", Point.class);

    LootContext.Trait<DamageType> DAMAGE_SOURCE = new TraitImpl<>("damage_source", DamageType.class);

    LootContext.Trait<Entity> THIS = new TraitImpl<>("this", Entity.class);

    LootContext.Trait<Entity> KILLER_ENTITY = new TraitImpl<>("killer_entity", Entity.class);
    LootContext.Trait<Player> KILLER_PLAYER = new TraitImpl<>("killer_player", Player.class);
    LootContext.Trait<Entity> DIRECT_KILLER = new TraitImpl<>("direct_killer_entity", Entity.class);

    LootContext.Trait<ItemStack> TOOL = new TraitImpl<>("tool", ItemStack.class);

    LootContext.Trait<Block> BLOCK_ENTITY = new TraitImpl<>("block_entity", Block.class);
    LootContext.Trait<Double> EXPLOSION_RADIUS = new TraitImpl<>("explosion_radius", Double.class);
}
