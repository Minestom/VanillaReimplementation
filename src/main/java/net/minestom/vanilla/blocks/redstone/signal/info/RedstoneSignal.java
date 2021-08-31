package net.minestom.vanilla.blocks.redstone.signal.info;

import org.jetbrains.annotations.NotNull;

/**
 * Represents the properties of a redstone signal
 */
public record RedstoneSignal(
        @NotNull Type type,
        int strength
) implements Comparable<RedstoneSignal> {

    @Override
    public int compareTo(@NotNull RedstoneSignal o) {
        return switch (type) {

            case HARD -> switch (o.type) {
                case HARD -> o.strength - strength;
                case SOFT -> 1;
            };

            case SOFT -> switch (o.type) {
                case HARD -> -1;
                case SOFT -> o.strength - strength;
            };
        };
    }

    /**
     * @return new signal with strength lowered by 1 and type HARD
     */
    public RedstoneSignal reduce() {
        return new RedstoneSignal(
                Type.SOFT,
                Math.max(strength - 1, 0)
        );
    }

    public enum Type {
        HARD,
        SOFT
    }
}
