package net.minestom.vanilla.loot.util;

import net.kyori.adventure.key.Key;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.registry.DynamicRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@SuppressWarnings("UnstableApiUsage")
public sealed interface ListOperation {

    @NotNull StructCodec<ListOperation> CODEC = Codec.RegistryTaggedUnion(registries -> {
        class Holder {
            static final @NotNull DynamicRegistry<StructCodec<? extends ListOperation>> CODEC = createDefaultRegistry();
        }
        return Holder.CODEC;
    }, ListOperation::codec, "type");

    static @NotNull DynamicRegistry<StructCodec<? extends ListOperation>> createDefaultRegistry() {
        final DynamicRegistry<StructCodec<? extends ListOperation>> registry = DynamicRegistry.create(Key.key("list_operations"));
        registry.register("append", Append.CODEC);
        registry.register("insert", Insert.CODEC);
        registry.register("replace_all", ReplaceAll.CODEC);
        registry.register("replace_section", ReplaceSection.CODEC);
        return registry;
    }

    <T> @NotNull List<T> apply(@NotNull List<T> values, @NotNull List<T> input);

    /**
     * @return the codec that can encode this list operation
     */
    @NotNull StructCodec<? extends ListOperation> codec();

    record WithValues<T>(@NotNull ListOperation operation, @NotNull List<T> values) {
        public static <T> Codec<WithValues<T>> codec(Codec<T> codec) {
            return StructCodec.struct(
                    StructCodec.INLINE, ListOperation.CODEC, WithValues::operation,
                    "values", codec.list(), WithValues::values,
                    WithValues::new
            );
        }
    }

    record Append() implements ListOperation {
        public static final @NotNull StructCodec<Append> CODEC = StructCodec.struct(Append::new);

        @Override
        public @NotNull <T> List<T> apply(@NotNull List<T> values, @NotNull List<T> input) {
            return Stream.concat(input.stream(), values.stream()).toList();
        }

        @Override
        public @NotNull StructCodec<? extends ListOperation> codec() {
            return CODEC;
        }
    }

    record Insert(int offset) implements ListOperation {
        public static final @NotNull StructCodec<Insert> CODEC = StructCodec.struct(
                "offset", Codec.INT.optional(0), Insert::offset,
                Insert::new
        );

        @Override
        public @NotNull <T> List<T> apply(@NotNull List<T> values, @NotNull List<T> input) {
            List<T> items = new ArrayList<>();
            items.addAll(input.subList(0, this.offset));
            items.addAll(values);
            items.addAll(input.subList(this.offset, input.size()));
            return items;
        }

        @Override
        public @NotNull StructCodec<? extends ListOperation> codec() {
            return CODEC;
        }
    }

    record ReplaceAll() implements ListOperation {
        public static final @NotNull StructCodec<ReplaceAll> CODEC = StructCodec.struct(ReplaceAll::new);

        @Override
        public @NotNull <T> List<T> apply(@NotNull List<T> values, @NotNull List<T> input) {
            return values;
        }

        @Override
        public @NotNull StructCodec<? extends ListOperation> codec() {
            return CODEC;
        }
    }

    record ReplaceSection(int offset, @Nullable Integer size) implements ListOperation {
        public static final @NotNull StructCodec<ReplaceSection> CODEC = StructCodec.struct(
                "offset", Codec.INT.optional(0), ReplaceSection::offset,
                "size", Codec.INT.optional(), ReplaceSection::size,
                ReplaceSection::new
        );

        @Override
        public @NotNull <T> List<T> apply(@NotNull List<T> values, @NotNull List<T> input) {
            List<T> items = new ArrayList<>();
            items.addAll(input.subList(0, offset));
            items.addAll(values);

            int size = this.size != null ? this.size : values.size();

            // Add truncated part of list of possible
            if (offset + size < input.size()) {
                items.addAll(input.subList(offset + size, input.size()));
            }

            return items;
        }

        @Override
        public @NotNull StructCodec<? extends ListOperation> codec() {
            return CODEC;
        }
    }

}
