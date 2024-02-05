package net.minestom.vanilla.datapack.worldgen;

import net.minestom.server.utils.NamespaceID;

public interface RuleTest {
    NamespaceID predicate_type();

    /**
     * Tests for a block.
     * @param block A block ID.
     */
    record BlockMatch(NamespaceID block) implements RuleTest {
        @Override
        public NamespaceID predicate_type() {
            return NamespaceID.from("block_match");
        }
    }

    /**
     * Tests for a specific block state.
     * @param block_state A block state.
     */
    record BlockStateMatch(BlockState block_state) implements RuleTest {
        @Override
        public NamespaceID predicate_type() {
            return NamespaceID.from("blockstate_match");
        }
    }

    /**
     * Tests for a block with a random chance.
     * @param block A block ID.
     * @param probability The probability of the predicate to pass if the block is found. Values below 0.0 is treated as 0.0; values above 1.0 is treated as 1.0.
     */
    record RandomBlockMatch(NamespaceID block, float probability) implements RuleTest {
        @Override
        public NamespaceID predicate_type() {
            return NamespaceID.from("random_block_match");
        }
    }

    /**
     * Tests for a block state with a random chance.
     * @param block_state A block state.
     * @param probability The probability of the predicate to pass if the block state is found. Values below 0.0 is treated as 0.0; values above 1.0 is treated as 1.0.
     */
    record RandomBlockStateMatch(BlockState block_state, float probability) implements RuleTest {
        @Override
        public NamespaceID predicate_type() {
            return NamespaceID.from("random_blockstate_match");
        }
    }

    /**
     * Tests for a block in a block tag.
     * @param tag A block tag without #.
     */
    record TagMatch(String tag) implements RuleTest {
        @Override
        public NamespaceID predicate_type() {
            return NamespaceID.from("tag_match");
        }
    }
}
