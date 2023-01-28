package net.minestom.vanilla.entitymeta;

import net.minestom.server.entity.Entity;
import net.minestom.server.instance.block.Block;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;

public interface EntityTags {

    interface FallingBlock {
        @NotNull Tag<Block> BLOCK = Tag.String("vri:entity-meta.falling_block.block")
                .map(Block::fromNamespaceId, block -> block.namespace().toString());
    }

    interface PrimedTnt {
        @NotNull Tag<Integer> FUSE_TIME = Tag.Integer("vri:entity-meta.primed_tnt.fuse_time");
    }
}
