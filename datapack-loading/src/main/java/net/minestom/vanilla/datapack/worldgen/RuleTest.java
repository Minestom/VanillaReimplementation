package net.minestom.vanilla.datapack.worldgen;

import com.squareup.moshi.JsonReader;
import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.datapack.json.JsonUtils;
import net.minestom.vanilla.datapack.json.NamespaceTag;

import java.io.IOException;

public sealed interface RuleTest {
    default NamespaceID predicate_type() {
        return JsonUtils.getNamespaceTag(this.getClass());
    }

    static RuleTest fromJson(JsonReader reader) throws IOException {
        return JsonUtils.sealedUnionNamespace(reader, RuleTest.class, "predicate_type");
    }

    /**
     * Tests for a block.
     * @param block A block ID.
     */
    @NamespaceTag("block_match")
    record BlockMatch(NamespaceID block) implements RuleTest {
    }

    /**
     * Tests for a specific block state.
     * @param block_state A block state.
     */
    @NamespaceTag("blockstate_match")
    record BlockStateMatch(BlockState block_state) implements RuleTest {
    }

    /**
     * Tests for a block with a random chance.
     * @param block A block ID.
     * @param probability The probability of the predicate to pass if the block is found. Values below 0.0 is treated as 0.0; values above 1.0 is treated as 1.0.
     */
    @NamespaceTag("random_block_match")
    record RandomBlockMatch(NamespaceID block, float probability) implements RuleTest {
    }

    /**
     * Tests for a block state with a random chance.
     * @param block_state A block state.
     * @param probability The probability of the predicate to pass if the block state is found. Values below 0.0 is treated as 0.0; values above 1.0 is treated as 1.0.
     */
    @NamespaceTag("random_blockstate_match")
    record RandomBlockStateMatch(BlockState block_state, float probability) implements RuleTest {
    }

    /**
     * Tests for a block in a block tag.
     * @param tag A block tag without #.
     */
    @NamespaceTag("tag_match")
    record TagMatch(String tag) implements RuleTest {
    }
}
