package net.minestom.vanilla.loot;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.IntBinaryTag;
import net.kyori.adventure.nbt.StringBinaryTag;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.vanilla.loot.util.RelevantEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Returns NBT data from the provided context.
 */
@SuppressWarnings("UnstableApiUsage")
public interface LootNBT {

    @NotNull Codec<LootNBT> CODEC = Codec.RegistryTaggedUnion(registries -> {
        class Holder {
            static final @NotNull DynamicRegistry<StructCodec<? extends LootNBT>> CODEC = createDefaultRegistry();
        }
        return Holder.CODEC;
    }, LootNBT::codec, "type").orElse(Codec.STRING.transform(
            str -> new Context(Context.Target.fromString(str)),
            nbt -> ((Context) nbt).target.toString()
    ));

    static @NotNull DynamicRegistry<StructCodec<? extends LootNBT>> createDefaultRegistry() {
        final DynamicRegistry<StructCodec<? extends LootNBT>> registry = DynamicRegistry.create(Key.key("loot_nbt"));
        registry.register("context", Context.CODEC);
        registry.register("storage", Storage.CODEC);
        return registry;
    }

    /**
     * Generates some NBT based on the provided context.
     * @param context the context to use for NBT
     * @return the NBT, or null if there is none
     */
    @Nullable BinaryTag getNBT(@NotNull LootContext context);

    /**
     * @return the codec that can encode this function
     */
    @NotNull StructCodec<? extends LootNBT> codec();

    record Storage(@NotNull Key source) implements LootNBT {
        public static final @NotNull StructCodec<Storage> CODEC = StructCodec.struct(
                "source", Codec.KEY, Storage::source,
                Storage::new
        );

        @Override
        public @Nullable BinaryTag getNBT(@NotNull LootContext context) {
            if (true) throw new UnsupportedOperationException("TODO: Implement command storage (Key -> CompoundBinaryTag)");

            return null;
        }

        @Override
        public @NotNull StructCodec<? extends LootNBT> codec() {
            return CODEC;
        }
    }

    record Context(@NotNull Target target) implements LootNBT {
        public static final @NotNull StructCodec<Context> CODEC = StructCodec.struct(
                "target", Codec.STRING.transform(Context.Target::fromString, Context.Target::toString), Context::target,
                Context::new
        );

        public sealed interface Target {
            @Nullable BinaryTag getNBT(@NotNull LootContext context);

            static @NotNull Target fromString(@NotNull String input) {
                if (input.equals("block_entity")) return new BlockEntity();

                for (var target : RelevantEntity.values()) {
                    if (target.id().equals(input)) return new Entity(target);
                }

                throw new IllegalArgumentException("Expected block_entity or a valid entity target name");
            }

            record BlockEntity() implements Target {
                @SuppressWarnings("DataFlowIssue")
                @Override
                public @NotNull BinaryTag getNBT(@NotNull LootContext context) {
                    Block block = context.require(LootContext.BLOCK_STATE);
                    Point pos = context.require(LootContext.ORIGIN);

                    CompoundBinaryTag nbt = block.hasNbt() ? block.nbt() : CompoundBinaryTag.empty();

                    return nbt.put(Map.of(
                            "x", IntBinaryTag.intBinaryTag(pos.blockX()),
                            "y", IntBinaryTag.intBinaryTag(pos.blockY()),
                            "z", IntBinaryTag.intBinaryTag(pos.blockZ()),
                            "id", StringBinaryTag.stringBinaryTag(block.key().asString())
                    ));
                }

                @Override
                public String toString() {
                    return "block_entity";
                }
            }

            record Entity(@NotNull RelevantEntity target) implements Target {
                @Override
                public @NotNull BinaryTag getNBT(@NotNull LootContext context) {
                    var entity = context.require(target.key());

                    if (true) throw new UnsupportedOperationException("TODO: Implement entity serialization (Entity entity -> BinaryTag)");
                    return null;
                }

                @Override
                public String toString() {
                    return target.id();
                }
            }
        }

        @Override
        public @Nullable BinaryTag getNBT(@NotNull LootContext context) {
            return target.getNBT(context);
        }

        @Override
        public @NotNull StructCodec<? extends LootNBT> codec() {
            return CODEC;
        }
    }

}

