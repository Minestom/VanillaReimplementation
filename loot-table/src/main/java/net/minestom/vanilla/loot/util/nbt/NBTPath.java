package net.minestom.vanilla.loot.util.nbt;

import it.unimi.dsi.fastutil.ints.IntSet;
import net.kyori.adventure.nbt.*;
import net.minestom.server.adventure.MinestomAdventure;
import net.minestom.server.codec.Codec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * A NBTPath allows selecting specific elements from a NBT tree, based on a list of selectors. Each selector has the
 * ability to select any number of elements from its predecessor—this allows arbitrary item selection from any NBT type.
 * <br>
 * It also provides multiple ways to manipulate NBT results as there is no deeply mutable NBT implementation.
 */
public record NBTPath(@NotNull List<Selector> selectors) {

    @SuppressWarnings("UnstableApiUsage")
    public static final @NotNull Codec<NBTPath> CODEC = Parser.CODEC;

    /**
     * Selects an arbitrary number of elements from provided NBT.
     */
    public sealed interface Selector {

        /**
         * Passes each selected NBT element into the given consumer.
         */
        void get(@NotNull NBTReference source, @NotNull Consumer<NBTReference> consumer);

        /**
         * Modifies the provided {@code source} so that, if possible, this path selector will select at least one NBT
         * element from it. If a placeholder element is needed, {@code nextElement} is to be used.
         */
        void prepare(@NotNull NBTReference source, @NotNull Supplier<BinaryTag> nextElement);

        /**
         * Provides a tag that this selector may be willing to modify.
         */
        @NotNull BinaryTag preparedNBT();

        record RootKey(@NotNull String key) implements Selector {
            @Override
            public void get(@NotNull NBTReference source, @NotNull Consumer<NBTReference> consumer) {
                if (source.has(key)) {
                    consumer.accept(source.get(key));
                }
            }

            @Override
            public void prepare(@NotNull NBTReference source, @NotNull Supplier<BinaryTag> nextElement) {}

            @Override
            public @NotNull BinaryTag preparedNBT() {
                return CompoundBinaryTag.empty();
            }

            @Override
            public String toString() {
                return key;
            }
        }

        record Key(@NotNull String key) implements Selector {
            @Override
            public void get(@NotNull NBTReference source, @NotNull Consumer<NBTReference> consumer) {
                if (source.has(key)) {
                    consumer.accept(source.get(key));
                }
            }

            @Override
            public void prepare(@NotNull NBTReference source, @NotNull Supplier<BinaryTag> nextElement) {
                if (!source.has(key)) {
                    source.get(key).set(nextElement.get());
                }
            }

            @Override
            public @NotNull BinaryTag preparedNBT() {
                return CompoundBinaryTag.empty();
            }

            @Override
            public String toString() {
                return "." + key;
            }
        }

        record CompoundFilter(@NotNull CompoundBinaryTag filter) implements Selector {
            @Override
            public void get(@NotNull NBTReference source, @NotNull Consumer<NBTReference> consumer) {
                if (NBTUtils.compareNBT(filter, source.get(), false)) {
                    consumer.accept(source);
                }
            }

            @Override
            public void prepare(@NotNull NBTReference source, @NotNull Supplier<BinaryTag> nextElement) {
                if (!NBTUtils.compareNBT(filter, source.get(), false)) {
                    source.set(filter);
                }
            }

            @Override
            public @NotNull BinaryTag preparedNBT() {
                return CompoundBinaryTag.empty();
            }

            @Override
            public String toString() {
                try {
                    return TagStringIO.get().asString(filter);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        record EntireList() implements Selector {
            @Override
            public void get(@NotNull NBTReference source, @NotNull Consumer<NBTReference> consumer) {
                int size = source.listSize();
                for (int i = 0; i < size; i++) {
                    consumer.accept(source.get(i));
                }
            }

            @Override
            public void prepare(@NotNull NBTReference source, @NotNull Supplier<BinaryTag> nextElement) {
                if (source.listSize() == 0) {
                    source.listAdd(nextElement.get());
                }
            }

            @Override
            public @NotNull BinaryTag preparedNBT() {
                return ListBinaryTag.empty();
            }

            @Override
            public String toString() {
                return "[]";
            }
        }

        record Index(int index) implements Selector {
            @Override
            public void get(@NotNull NBTReference source, @NotNull Consumer<NBTReference> consumer) {
                var newIndex = index >= 0 ? index : source.listSize() + index;

                if (newIndex < 0 || newIndex >= source.listSize()) return;

                consumer.accept(source.get(newIndex));
            }

            @Override
            public void prepare(@NotNull NBTReference source, @NotNull Supplier<BinaryTag> nextElement) {}

            @Override
            public @NotNull BinaryTag preparedNBT() {
                return ListBinaryTag.empty();
            }

            @Override
            public String toString() {
                return "[" + index + "]";
            }
        }

        record ListFilter(@NotNull CompoundBinaryTag filter) implements Selector {
            @Override
            public void get(@NotNull NBTReference source, @NotNull Consumer<NBTReference> consumer) {
                int listSize = source.listSize();
                for (int i = 0; i < listSize; i++) {
                    NBTReference ref = source.get(i);
                    if (NBTUtils.compareNBT(filter, ref.get(), false)) {
                        consumer.accept(ref);
                    }
                }
            }

            @Override
            public void prepare(@NotNull NBTReference source, @NotNull Supplier<BinaryTag> nextElement) {
                int listSize = source.listSize();
                if (listSize == -1) return;

                for (int i = 0; i < listSize; i++) {
                    if (NBTUtils.compareNBT(filter, source.get(i).get(), false)) {
                        return;
                    }
                }

                source.listAdd(filter);
            }

            @Override
            public @NotNull BinaryTag preparedNBT() {
                return ListBinaryTag.empty();
            }

            @Override
            public String toString() {
                try {
                    return "[" + TagStringIO.get().asString(filter) + "]";
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

    }

    /**
     * Strings {@code source} through each selector in {@link #selectors()}, returning the selected results. It is
     * possible for there to be none. Modifying the resulting NBT references does nothing.
     * @param source the source, to get the NBT from
     * @return the list of selected NBT, which may be empty
     */
    public @NotNull List<NBTReference> get(@NotNull BinaryTag source) {
        List<NBTReference> references = List.of(NBTReference.of(source));
        for (var selector : selectors()) {
            List<NBTReference> newReferences = new ArrayList<>();

            references.forEach(nbt -> selector.get(nbt, newReferences::add));

            if (newReferences.isEmpty()) {
                return List.of();
            }
            references = newReferences;
        }
        return references;
    }

    /**
     * Strings {@code source} through each selector, making each selector
     * {@link Selector#prepare(NBTReference, Supplier) prepare} each element. This should result in this method
     * returning nothing much less often, although it is still possible. Modifying the resulting NBT references will
     * result in the provided {@code source} being modified.
     * @param source the source, to get the NBT from
     * @param finalDefault the default value for results produced from the last path selector
     * @return the list of selected NBT, which may be empty
     */
    public @NotNull List<NBTReference> getWithDefaults(@NotNull NBTReference source, @NotNull Supplier<BinaryTag> finalDefault) {
        List<NBTReference> references = List.of(source);

        for (int selectorIndex = 0; selectorIndex < selectors().size(); selectorIndex++) {
            var selector = selectors().get(selectorIndex);
            Supplier<BinaryTag> next = (selectorIndex < selectors().size() - 1) ? selectors().get(selectorIndex + 1)::preparedNBT : finalDefault;

            List<NBTReference> newNBT = new ArrayList<>();
            for (var nbt : references) {
                selector.prepare(nbt, next);
                selector.get(nbt, newNBT::add);
            }
            if (newNBT.isEmpty()) {
                return List.of();
            }

            references = newNBT;
        }

        return references;
    }

    /**
     * Strings {@code source} through each selector, making each selector
     * {@link Selector#prepare(NBTReference, Supplier) prepare} each element. This should result in this method
     * returning nothing much less often, although it is still possible. Modifying the resulting NBT references will
     * result in the provided {@code source} being modified. This is equivalent to calling
     * {@link #getWithDefaults(NBTReference, Supplier)} and setting all of the results to {@code setValue}.
     * @param source the source, to get the NBT from
     * @param setValue the value to set all selected elements to
     * @return the list of selected NBT, which may be empty
     */
    public @NotNull List<NBTReference> set(@NotNull NBTReference source, @NotNull BinaryTag setValue) {
        List<NBTReference> references = getWithDefaults(source, () -> setValue);
        for (var reference : references) {
            reference.set(setValue);
        }
        return references;
    }

    @Override
    public String toString() {
        return selectors().stream().map(Selector::toString).collect(Collectors.joining());
    }
}

@SuppressWarnings("UnstableApiUsage")
class Parser {

    static final @NotNull IntSet VALID_SELECTOR_STARTERS = IntSet.of('.', '{', '[');
    static final @NotNull IntSet VALID_INTEGER_CHARACTERS = IntSet.of('-', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9');
    static final @NotNull IntSet INVALID_UNQUOTED_CHARACTERS = IntSet.of(-1, '.', '\'', '\"', '{', '}', '[', ']');

    static final Codec<NBTPath> CODEC = Codec.STRING.transform(s -> {
        try {
            return readPath(new StringReader(s));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }, NBTPath::toString);

    static @NotNull NBTPath readPath(@NotNull StringReader reader) throws IOException {
        List<NBTPath.Selector> selectors = new ArrayList<>();

        if (!VALID_SELECTOR_STARTERS.contains(peek(reader))) {
            var key = readString(reader);
            if (key != null) {
                selectors.add(new NBTPath.Selector.RootKey(key));
            }
        }

        while (true) {
            reader.mark(0);

            if (!VALID_SELECTOR_STARTERS.contains(peek(reader))) {
                if (selectors.isEmpty()) {
                    throw new IllegalArgumentException("NBT paths must contain at least one selector");
                }
                return new NBTPath(List.copyOf(selectors));
            }

            var selector = readPathSelector(reader);
            if (selector == null) {
                throw new IllegalArgumentException("Invalid NBT path selector");
            }

            selectors.add(selector);
        }
    }

    // Returning null indicates a failure to read
    @SuppressWarnings("ResultOfMethodCallIgnored")
    static @Nullable NBTPath.Selector readPathSelector(@NotNull StringReader reader) throws IOException {
        var firstChar = peek(reader);
        return switch (firstChar) {
            case '.' -> {
                reader.skip(1); // Skip period

                var string = readString(reader);
                yield string != null ? new NBTPath.Selector.Key(string) : null;
            }
            case '{' -> {
                var compound = readCompound(reader);
                yield compound != null ? new NBTPath.Selector.CompoundFilter(compound) : null;
            }
            case '[' -> {
                reader.skip(1); // Skip opening square brackets

                var secondChar = peek(reader);
                var selector = switch(secondChar) {
                    case ']' -> new NBTPath.Selector.EntireList();
                    case '{' -> {
                        var compound = readCompound(reader);
                        yield compound != null ? new NBTPath.Selector.ListFilter(compound) : null;
                    }
                    default -> {
                        if (VALID_INTEGER_CHARACTERS.contains(secondChar)) {
                            var index = readInteger(reader);
                            yield index != null ? new NBTPath.Selector.Index(index) : null;
                        }
                        yield null;
                    }
                };

                reader.skip(1); // Skip closing square brackets
                yield selector;
            }
            default -> null;
        };
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static @Nullable Integer readInteger(@NotNull StringReader reader) throws IOException {
        StringBuilder builder = new StringBuilder();

        int peek;
        while (VALID_INTEGER_CHARACTERS.contains(peek = reader.read())) {
            builder.appendCodePoint(peek);
        }

        // Unread the one extra character that was read; this does nothing if the entire string has been read
        reader.skip(-1);

        try {
            return Integer.parseInt(builder.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static @Nullable String readString(@NotNull StringReader reader) throws IOException {
        var peek = peek(reader);
        if (peek == -1) {
            return null;
        }

        StringBuilder builder = new StringBuilder();

        if (peek == '"' || peek == '\'') { // Read quoted string
            reader.skip(1); // Skip the character we know already

            boolean escape = false;

            while (true) {
                var next = reader.read();

                if (next == '\\') { // Read escape character
                    escape = true;
                } else if (next == peek && !escape) { // Return if unescaped closing character
                    return builder.toString();
                } else {
                    if (escape) { // If there was an unused escape, re-add it; there's only one character it's used for
                        builder.appendCodePoint('\\');
                    }

                    if (next == -1) {
                        return null;
                    }

                    builder.appendCodePoint(next); // Add the next character always
                }
            }
        }

        // Read unquoted string
        int read;
        while (!INVALID_UNQUOTED_CHARACTERS.contains(read = reader.read())) {
            builder.appendCodePoint(read);
        }

        // Unread the one extra character that was read; this does nothing if the entire string has been read
        reader.skip(-1);

        return builder.isEmpty() ? null : builder.toString();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static int peek(@NotNull StringReader reader) throws IOException {
        var codePoint = reader.read();
        reader.skip(-1);
        return codePoint;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static BinaryTag readTag(@NotNull StringReader reader) throws IOException {
        StringBuilder builder = new StringBuilder();

        while (true) {
            int code = reader.read();

            if (code == -1) break;

            builder.appendCodePoint(code);
        }

        String dump = builder.toString();

        StringBuffer buffer = new StringBuffer();
        BinaryTag tag = MinestomAdventure.tagStringIO().asTag(dump, buffer);

        reader.skip(dump.length() - buffer.length()); // Skip (total chars) - (remaining chars) = read chars
        return tag;
    }

    private static CompoundBinaryTag readCompound(@NotNull StringReader reader) throws IOException {
        if (readTag(reader) instanceof CompoundBinaryTag compound) {
            return compound;
        } else {
            throw new IllegalArgumentException("Expected an inline compound");
        }
    }

}
