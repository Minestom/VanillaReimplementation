package net.minestom.vanilla.loot.util;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.vanilla.loot.LootContext;
import net.minestom.vanilla.loot.LootNumber;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An inclusive number range based on loot numbers.
 * @param min the optional minimum value
 * @param max the optional maximum value
 */
public record LootNumberRange(@Nullable LootNumber min, @Nullable LootNumber max) {

    @SuppressWarnings({"UnstableApiUsage", "NumberEquality"})
    public static final @NotNull Codec<LootNumberRange> CODEC = Codec.DOUBLE.transform(LootNumber.Constant::new, LootNumber.Constant::value)
            .transform(n -> new LootNumberRange(n, n), r -> {
                if (r.min() instanceof LootNumber.Constant(Double min) && r.max() instanceof LootNumber.Constant(Double max) && min == max) {
                    return (LootNumber.Constant) r.min();
                } else throw new UnsupportedOperationException("Using struct codec");
            }).orElse(StructCodec.struct(
                    "min", LootNumber.CODEC.optional(), LootNumberRange::min,
                    "max", LootNumber.CODEC.optional(), LootNumberRange::max,
                    LootNumberRange::new
            ));

    /**
     * Limits the provided value to between the minimum and maximum.<br>
     * This API currently guarantees that, if the minimum ends up being larger than the maximum, the resulting value
     * will be equal to the maximum.
     * @param context the context, to use for getting the values of the min and max
     * @param number the number to constrain to between the minimum and maximum
     * @return the constrained number
     */
    public long limit(@NotNull LootContext context, long number) {
        if (this.min != null) {
            number = Math.max(this.min.getInt(context), number);
        }
        if (this.max != null) {
            number = Math.min(this.max.getInt(context), number);
        }
        return number;
    }

    /**
     * Limits the provided value to between the minimum and maximum.<br>
     * This API currently guarantees that, if the minimum ends up being larger than the maximum, the resulting value
     * will be equal to the maximum.
     * @param context the context, to use for getting the values of the min and max
     * @param number the number to constrain to between the minimum and maximum
     * @return the constrained number
     */
    public double limit(@NotNull LootContext context, double number) {
        if (this.min != null) {
            number = Math.max(this.min.getDouble(context), number);
        }
        if (this.max != null) {
            number = Math.min(this.max.getDouble(context), number);
        }
        return number;
    }

    /**
     * Assures that the provided number is not smaller than the minimum and is not larger than the maximum. If either of
     * the bounds is null, it's always considered as passing.
     * @param context the context, to use for getting the values of the min and max
     * @param number the number to check the validity of
     * @return true if the provided number fits within {@link #min()} and {@link #max()}, and false otherwise
     */
    public boolean check(@NotNull LootContext context, long number) {
        return (this.min == null || this.min.getInt(context) <= number) &&
                (this.max == null || this.max.getInt(context) >= number);
    }

    /**
     * Assures that the provided number is not smaller than the minimum and is not larger than the maximum. If either of
     * the bounds is null, it's always considered as passing.
     * @param context the context, to use for getting the values of the min and max
     * @param number the number to check the validity of
     * @return true if the provided number fits within {@link #min()} and {@link #max()}, and false otherwise
     */
    public boolean check(@NotNull LootContext context, double number) {
        return (this.min == null || this.min.getDouble(context) <= number) &&
                (this.max == null || this.max.getDouble(context) >= number);
    }

}
