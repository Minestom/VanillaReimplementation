package net.minestom.vanilla.datapack.worldgen;

import com.squareup.moshi.JsonReader;
import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.datapack.json.JsonUtils;
import net.minestom.vanilla.datapack.json.NamespaceTag;
import net.minestom.vanilla.datapack.json.Optional;

import java.io.IOException;

public sealed interface PosRuleTest {

    default NamespaceID predicate_type() {
        return JsonUtils.getNamespaceTag(this.getClass());
    }

    static PosRuleTest fromJson(JsonReader reader) throws IOException {
        return JsonUtils.sealedUnionNamespace(reader, PosRuleTest.class, "predicate_type");
    }

    /**
     * Always passes.
     */
    @NamespaceTag("always_true")
    record AlwaysTrue() implements PosRuleTest {
    }

    /**
     * Passes with a random probability, the probability is based on the 3D Manhattan distance from the current block to the structure start.
     * @param min_chance (optional, default is 0.0) The probability (probability less than 0 is treated as 0, greater than 1 is treated as 1) for the predicate to pass when the distance of a block to the structure start is equal to or less than min_dist.
     * @param max_chance (optional, default is 0.0) The probability (probability less than 0 is treated as 0, greater than 1 is treated as 1) for the predicate to pass when the distance of a block to the structure start is equal to or greater than max_dist. If a block's distance is between min_dist and max_dist, probability is obtained by linear interpolation between the values of min_dist and max_dist, that is, the probability is (distance - min_dist ) / ( max_dist - min_dist ) * ( max_chance - min_chance ) + min_chance, and probability less than 0 is treated as 0, greater than 1 is treated as 1.
     * @param min_dist (optional, defaults to 0) the distance when the minimum probability is used. Must be less than  max_dist.
     * @param max_dist (optional, defaults to 0) the distance when the maximum probability is used. Must be greater than  min_dist.
     */
    @NamespaceTag("linear_pos")
    record LinearPos(@Optional Float min_chance, @Optional Float max_chance, @Optional Integer min_dist, @Optional Integer max_dist) implements PosRuleTest {
    }

    /**
     * Passes with a random probability, the probability only based on the distance on the specified axis.
     * @param axis (optional, defaults to y) can be x, y or z.
     * @param min_chance (optional, default is 0.0) The probability (probability less than 0 is treated as 0, greater than 1 is treated as 1) for the predicate to pass when the distance of a block to the structure start is equal to or less than min_dist.
     * @param max_chance (optional, default is 0.0) The probability (probability less than 0 is treated as 0, greater than 1 is treated as 1) for the predicate to pass when the distance of a block to the structure start is equal to or greater than max_dist. If a block's distance is between min_dist and max_dist, probability is obtained by linear interpolation between the values of min_dist and max_dist, that is, the probability is (distance - min_dist ) / ( max_dist - min_dist ) * ( max_chance - min_chance ) + min_chance, and probability less than 0 is treated as 0, greater than 1 is treated as 1.
     * @param min_dist (optional, defaults to 0) the distance when the minimum probability is used. Must be less than  max_dist.
     * @param max_dist (optional, defaults to 0) the distance when the maximum probability is used. Must be greater than  min_dist.
     */
    @NamespaceTag("axis_aligned_linear_pos")
    record AxisAlignedLinearPos(@Optional String axis, @Optional Float min_chance, @Optional Float max_chance, @Optional Integer min_dist, @Optional Integer max_dist) implements PosRuleTest {
    }
}
