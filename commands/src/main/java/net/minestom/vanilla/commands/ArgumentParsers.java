package net.minestom.vanilla.commands;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.utils.entity.EntityFinder;
import net.minestom.server.utils.location.RelativeVec;
import net.minestom.vanilla.commands.data.Anchor;
import net.minestom.vanilla.commands.data.Swizzle;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;
import java.util.stream.Stream;

public interface ArgumentParsers<R> {
    ArgumentParsers<RelativeVec> RELATIVE_VEC_3 = (Args3<RelativeVec>) input -> {
        return ArgumentType.RelativeVec3("dummy").parse(input);
    };
    ArgumentParsers<RelativeVec> RELATIVE_VEC_2 = (Args2<RelativeVec>) input -> ArgumentType.RelativeVec2("dummy").parse(input);
    ArgumentParsers<Swizzle> SWIZZLE = Swizzle.ARGUMENT::parse;
    ArgumentParsers<Anchor> ANCHOR = Anchor.ARGUMENT::parse;
    ArgumentParsers<EntityFinder> ENTITY = input -> ArgumentType.Entity("dummy").parse(input);
    ArgumentParsers<String> RESOURCE_LOCATION = input -> ArgumentType.ResourceLocation("dummy").parse(input);

    default int elements() {
        return 1;
    }

    @NotNull R parse(@NotNull String input);

    default @NotNull R parse(@NotNull Supplier<String> input) {
        return Stream.generate(input)
                .limit(elements())
                .reduce((a, b) -> a + " " + b)
                .map(this::parse)
                .orElseThrow();
    }

    default @NotNull R parse(Supplier<String> input, String... given) {
        return Stream.concat(Stream.of(given), Stream.generate(input))
                .limit(elements())
                .reduce((a, b) -> a + " " + b)
                .map(this::parse)
                .orElseThrow();
    }

    interface Args2<R> extends ArgumentParsers<R> {
        @Override
        default int elements() {
            return 2;
        }
    }

    interface Args3<R> extends ArgumentParsers<R> {
        @Override
        default int elements() {
            return 3;
        }
    }
}
