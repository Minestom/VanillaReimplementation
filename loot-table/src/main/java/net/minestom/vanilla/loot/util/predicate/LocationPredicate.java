package net.minestom.vanilla.loot.util.predicate;

import net.minestom.server.codec.Codec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.Unit;
import org.jetbrains.annotations.NotNull;

// TODO: Incomplete

@SuppressWarnings("UnstableApiUsage")
public interface LocationPredicate {

    @NotNull Codec<LocationPredicate> CODEC = Codec.UNIT.transform(a -> (instance, point) -> false, a -> Unit.INSTANCE);

    boolean test(@NotNull Instance instance, @NotNull Point point);

}
