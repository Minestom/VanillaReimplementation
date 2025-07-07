package net.minestom.vanilla.loot.util.predicate;

import net.minestom.server.codec.Codec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.Unit;
import org.jetbrains.annotations.NotNull;

// TODO: Incomplete

@SuppressWarnings("UnstableApiUsage")
public interface DamageSourcePredicate {

    @NotNull Codec<DamageSourcePredicate> CODEC = Codec.UNIT.transform(a -> (instance, pos, type) -> false, a -> Unit.INSTANCE);

    boolean test(@NotNull Instance instance, @NotNull Point pos, @NotNull DamageType type);

}
