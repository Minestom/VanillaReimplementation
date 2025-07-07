package net.minestom.vanilla.loot.util.predicate;

import net.minestom.server.codec.Codec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.Unit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// TODO: Incomplete

@SuppressWarnings("UnstableApiUsage")
public interface EntityPredicate {

    @NotNull Codec<EntityPredicate> CODEC = Codec.UNIT.transform(a -> (instance, pos, entity) -> false, a -> Unit.INSTANCE);

    boolean test(@NotNull Instance instance, @Nullable Point pos, @Nullable Entity entity);

}
