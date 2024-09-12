package net.minestom.vanilla.datapack.loot.context;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ContextGroups {

    private static final Map<String, LootContext.Trait<Entity>> entityTraits = Set.of(
                    Traits.THIS,
                    Traits.DIRECT_KILLER,
                    Traits.KILLER_ENTITY,
                    Traits.KILLER_PLAYER.map(entity -> (Entity) entity)
            ).stream()
            .map(trait -> Map.entry(trait.id(), trait))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    private static final Map<String, LootContext.Trait<Component>> namedTraits = Set.of(
                    Traits.BLOCK_ENTITY.map(block -> (Component) Component.text(block.name())),
                    Traits.DIRECT_KILLER.map(entity -> {
                        @Nullable Component customName = entity.getCustomName();
                        if (customName != null) return customName;
                        return Component.text(entity.getEntityType().name());
                    }),
                    Traits.KILLER_ENTITY.map(entity -> {
                        @Nullable Component customName = entity.getCustomName();
                        if (customName != null) return customName;
                        return Component.text(entity.getEntityType().name());
                    }),
                    Traits.KILLER_PLAYER.map(entity -> {
                        @Nullable Component customName = entity.getCustomName();
                        if (customName != null) return customName;
                        return Component.text(entity.getEntityType().name());
                    })
            ).stream()
            .map(trait -> Map.entry(trait.id(), trait))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    private static final Map<String, LootContext.Trait<CompoundBinaryTag>> nbtTraits = Set.of(
                    Traits.BLOCK_ENTITY.map(Block::nbt),
                    Traits.THIS.map(entity -> entity.tagHandler().asCompound()),
                    Traits.KILLER_ENTITY.map(entity -> entity.tagHandler().asCompound()),
                    Traits.DIRECT_KILLER.map(entity -> entity.tagHandler().asCompound()),
                    Traits.KILLER_PLAYER.map(entity -> entity.tagHandler().asCompound())
            ).stream()
            .map(trait -> Map.entry(trait.id(), trait))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
}
