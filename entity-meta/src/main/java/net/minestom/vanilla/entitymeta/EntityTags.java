package net.minestom.vanilla.entitymeta;

import net.minestom.server.instance.block.Block;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;

public interface EntityTags {

    interface FallingBlock {
        @NotNull Tag<Block> BLOCK = Tag.String("vri:entity-meta.falling_block.block")
                .map(Block::fromKey, block -> block.key().toString());
    }

    interface PrimedTnt {
        @NotNull Tag<Integer> FUSE_TIME = Tag.Integer("vri:entity-meta.primed_tnt.fuse_time");
    }
}
