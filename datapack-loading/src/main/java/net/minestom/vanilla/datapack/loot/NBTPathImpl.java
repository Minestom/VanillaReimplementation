package net.minestom.vanilla.datapack.loot;

import it.unimi.dsi.fastutil.ints.IntSet;
import net.kyori.adventure.nbt.*;
import net.minestom.vanilla.datapack.nbt.NBTUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

interface NBTPathImpl extends NBTPath {

    static NBTPathImpl readPath(StringReader reader) throws IOException {
        return Reader.readPath(reader);
    }

    static Single readSingle(StringReader stringReader) throws IOException {
        if (!(Reader.readPath(stringReader) instanceof Single single)) {
            throw new IllegalArgumentException("Expected a single nbt path, got a multi nbt path");
        }
        return single;
    }

    interface NbtPathCollector<T extends BinaryTag> extends BiConsumer<SingleSelector<T>, BinaryTag> {
    }

    /**
     * Selects an arbitrary number of elements from a provided NBT element.
     */
    interface Selector<T extends BinaryTag> {

        /**
         * Provides each selected NBT element from {@code source} into {@code selectedElements}.
         *
         * @param source           the reference that is the source NBT elemen  t
         * @param selectedElements the consumer for selected NBT references
         */
        void get(@NotNull T source, @NotNull NbtPathCollector<T> selectedElements);

        /**
         * Used to determine if this selector can be used to select the provided {@code type}.
         *
         * @param type the type to check
         * @return true if this selector can be used to select the provided {@code type}
         */
        boolean fitsGeneric(@NotNull BinaryTagType<?> type);
    }

    /**
     * Selects a single element (or none) from a provided NBT element.
     * @param <T>
     */
    interface SingleSelector<T extends BinaryTag> extends Selector<T> {

        /**
         * Provides the selected NBT element from {@code source}.
         *
         * @param source the reference that is the source NBT element
         * @return the selected NBT element
         */
        @Nullable BinaryTag get(@NotNull T source);

        @Override
        default void get(@NotNull T source, @NotNull NbtPathCollector<T> selectedElements) {
            BinaryTag selected = get(source);
            if (selected != null) selectedElements.accept(this, selected);
        }
    }
}

record NBTPathMultiImpl(@NotNull List<NBTPathImpl.Selector<?>> selectors) implements NBTPathImpl {

    public @NotNull Map<Single, BinaryTag> get(@NotNull BinaryTag source) {
        Map<List<NBTPathImpl.SingleSelector<?>>, BinaryTag> references = Map.of(List.of(), source);
        for (var selector : selectors()) {
            Map<List<NBTPathImpl.SingleSelector<?>>, BinaryTag> newReferences = new HashMap<>();

            references.forEach((list, nbt) -> {
                if (!selector.fitsGeneric(nbt.type())) return;

                //noinspection unchecked
                ((NBTPathImpl.Selector<BinaryTag>) selector).get(nbt, (newSelector, newNbt) -> {
                    List<NBTPathImpl.SingleSelector<?>> newKey = new ArrayList<>(list);
                    newKey.add(newSelector);
                    newReferences.put(newKey, newNbt);
                });
            });

            if (newReferences.isEmpty()) return Map.of();
            references = newReferences;
        }
        return references.entrySet().stream()
                .map(entry -> {
                    NBTPath.Single path = new NBTPathSingleImpl(Collections.unmodifiableList(entry.getKey()));
                    BinaryTag nbt = entry.getValue();
                    return Map.entry(path, nbt);
                }).collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public String toString() {
        return selectors().stream()
                .map(Selector::toString)
                .collect(Collectors.joining());
    }
}

record NBTPathSingleImpl(List<NBTPathImpl.SingleSelector<?>> selectors) implements NBTPathImpl, NBTPath.Single {

    @Override
    public @Nullable BinaryTag getSingle(BinaryTag nbt) {
        for (var selector : selectors()) {
            if (nbt == null || !selector.fitsGeneric(nbt.type())) {
                // path has failed
                return null;
            }
            //noinspection unchecked
            nbt = ((NBTPathImpl.SingleSelector<BinaryTag>) selector).get(nbt);
        }
        return nbt;
    }

    @Override
    public @Nullable BinaryTag set(BinaryTag nbt, BinaryTag value) {
        return retrieveModified(0, nbt, value);
    }

    /**
     * Retrieves the modified version of this nbt
     * @param i the index of the selector to use
     * @param container the container to modify
     * @param value the value to set
     * @return the modified version of the container
     */
    private @Nullable BinaryTag retrieveModified(int i, BinaryTag container, BinaryTag value) {
        if (i == selectors.size()) return value;
        if (container == null) return null;

        SingleSelector<?> selector = selectors.get(i);
        if (!selector.fitsGeneric(container.type())) return null;

        //noinspection unchecked
        BinaryTag nbt = ((SingleSelector<BinaryTag>) selector).get(container);
        if (nbt == null) return null;

        // handle the next id on a per-type basis
        if (nbt.type() == BinaryTagTypes.COMPOUND) {
            CompoundBinaryTag compound = (CompoundBinaryTag) nbt;

            String key;
            { // find the key
                if (selector instanceof RootKey root) key = root.key();
                else if (selector instanceof CompoundKey compoundKey) key = compoundKey.key();
                else if (selector instanceof CompoundFilter) key = null;
                else throw new IllegalStateException("Unknown selector type: " + selector.getClass());
            }

            if (key == null) {
                // key is null, which mean that this is a filter and we can return the next call directly
                return retrieveModified(i + 1, compound, value);
            }

            // key is not null, which means that we need to set the value in the compound
            BinaryTag nextValue = retrieveModified(i + 1, compound.get(key), value);
            if (nextValue == null) return null;
            Map<String, BinaryTag> mutCompound = StreamSupport.stream(compound.spliterator(), false)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            mutCompound.put(key, nextValue);
            return CompoundBinaryTag.from(mutCompound);
        }

        if (nbt.type() == BinaryTagTypes.LIST) {
            ListBinaryTag list = (ListBinaryTag) nbt;

            int index;
            { // find the index
                if (selector instanceof ListIndex listIndex) index = listIndex.index();
                else throw new IllegalStateException("Unknown selector type: " + selector.getClass());
            }

            if (index < 0 || index >= list.size()) return null;

            BinaryTag nextValue = retrieveModified(i + 1, list.get(index), value);
            if (nextValue == null) return null;

            List<BinaryTag> javaList = new ArrayList<>(list.stream().toList());
            javaList.set(index, nextValue);
            return ListBinaryTag.listBinaryTag(list.elementType(), javaList);
        }

        // the current nbt container is a value, which means we replace it directly with the value param
        return value;
    }
}


// selectors

/**
 * Selects, if possible, the element under the {@link #key()} key of the provided source.<br>
 *
 * @param key the key to select
 */
record RootKey(@NotNull String key) implements NBTPathImpl.SingleSelector<CompoundBinaryTag> {

    @Override
    public @Nullable BinaryTag get(@NotNull CompoundBinaryTag source) {
        if (!source.keySet().contains(key)) return null;
        return source.get(key);
    }

    @Override
    public boolean fitsGeneric(@NotNull BinaryTagType<?> type) {
        return type == BinaryTagTypes.COMPOUND;
    }

    @Override
    public String toString() {
        return key;
    }
}

/**
 * Selects, if possible, the element under the {@link #key()} key of the provided source.<br>
 *
 * @param key the key to select
 */
record CompoundKey(@NotNull String key) implements NBTPathImpl.SingleSelector<CompoundBinaryTag> {

    @Override
    public @Nullable BinaryTag get(@NotNull CompoundBinaryTag source) {
        if (!source.keySet().contains(key)) return null;
        return source.get(key);
    }

    @Override
    public boolean fitsGeneric(@NotNull BinaryTagType<?> type) {
        return type == BinaryTagTypes.COMPOUND;
    }

    @Contract(pure = true)
    @Override
    public @NotNull String toString() {
        return "." + key;
    }
}

/**
 * Selects the provided source if it passes the {@link #filter()}.<br>
 *
 * @param filter the filter to use
 */
record CompoundFilter(@NotNull CompoundBinaryTag filter) implements NBTPathImpl.SingleSelector<BinaryTag> {

    @Override
    public @Nullable BinaryTag get(@NotNull BinaryTag source) {
        return NBTUtils.compareNBT(filter, source, false) ? source : null;
    }

    @Override
    public boolean fitsGeneric(@NotNull BinaryTagType<?> type) {
        return true;
    }

    @Override
    public @NotNull String toString() {
        try {
            return TagStringIO.get().asString(filter);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

/**
 * Selects, if possible, the element of index {@link #index()} from the provided list, or, if the index is negative,
 * selects the nth element from the end of the list, where n is {@link #index()}.<br>
 *
 * @param index the index to select, or negative to select starting from the end
 */
record ListIndex(int index) implements NBTPathImpl.SingleSelector<ListBinaryTag> {

    @Override
    public @Nullable BinaryTag get(@NotNull ListBinaryTag source) {
        var newIndex = index >= 0 ? index : source.size() + index;
        if (newIndex < 0) return null;
        if (newIndex >= source.size()) return null;
        return source.get(newIndex);
    }

    @Override
    public boolean fitsGeneric(@NotNull BinaryTagType<?> type) {
        return type == BinaryTagTypes.LIST;
    }

    @Contract(pure = true)
    @Override
    public @NotNull String toString() {
        return "[" + index + "]";
    }
}

/**
 * Selects, if possible, each element from the provided list that fits the {@link #filter()}.<br>
 *
 * @param filter the filter for each element in the list
 */
record ListFilter(@NotNull CompoundBinaryTag filter) implements NBTPathImpl.Selector<ListBinaryTag> {

    @Override
    public void get(@NotNull ListBinaryTag source, NBTPathImpl.@NotNull NbtPathCollector<ListBinaryTag> selectedElements) {
        IntStream.range(0, source.size())
                .mapToObj(i -> Map.entry(i, source.get(i)))
                .filter(entry -> NBTUtils.compareNBT(filter, entry.getValue(), false))
                .forEach(entry -> {
                    int i = entry.getKey();
                    BinaryTag nbt = entry.getValue();
                    selectedElements.accept(new ListIndex(i), nbt);
                });
    }

    @Override
    public boolean fitsGeneric(@NotNull BinaryTagType<?> type) {
        return type == BinaryTagTypes.LIST;
    }

    @Override
    public @NotNull String toString() {
        try {
            return "[" + TagStringIO.get().asString(filter) + "]";
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

/**
 * Selects, if possible, every item from the provided list.<br>
 */
record EntireList() implements NBTPathImpl.Selector<ListBinaryTag> {

    @Override
    public void get(@NotNull ListBinaryTag source, NBTPathImpl.@NotNull NbtPathCollector<ListBinaryTag> selectedElements) {
        IntStream.range(0, source.size())
                .mapToObj(i -> Map.entry(i, source.get(i)))
                .forEach(entry -> {
                    int i = entry.getKey();
                    BinaryTag nbt = entry.getValue();
                    selectedElements.accept(new ListIndex(i), nbt);
                });
    }

    @Override
    public boolean fitsGeneric(@NotNull BinaryTagType<?> type) {
        return type == BinaryTagTypes.LIST;
    }

    @Contract(pure = true)
    @Override
    public @NotNull String toString() {
        return "[]";
    }
}

// Reading
interface Reader {

    @NotNull IntSet VALID_SELECTOR_STARTERS = IntSet.of('.', '{', '[');
    @NotNull IntSet VALID_INTEGER_CHARACTERS = IntSet.of('-', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9');
    @NotNull IntSet INVALID_UNQUOTED_CHARACTERS = IntSet.of(-1, '.', '\'', '\"', '{', '}', '[', ']');

    static @NotNull NBTPathImpl readPath(@NotNull StringReader reader) throws IOException {
        List<NBTPathImpl.Selector<?>> selectors = new ArrayList<>();

        if (!VALID_SELECTOR_STARTERS.contains(peek(reader))) {
            var key = readString(reader);
            if (key != null) {
                selectors.add(new RootKey(key));
            }
        }

        while (true) {
            reader.mark(0);

            if (!VALID_SELECTOR_STARTERS.contains(peek(reader))) {
                if (selectors.isEmpty()) {
                    reader.reset();
                    String message = "NBT paths must contain at least one selector (reading from " + reader + ")";
                    throw new IllegalArgumentException(message);
                }
                // if all selectors are single, return a NBTPath.Single
                boolean allSingle = selectors.stream().allMatch(selector -> selector instanceof NBTPathImpl.Single);
                List<NBTPathImpl.Selector<?>> selectorsView = Collections.unmodifiableList(selectors);
                //noinspection unchecked
                return allSingle ? new NBTPathSingleImpl((List<NBTPathImpl.SingleSelector<?>>) (List<?>) selectorsView) : new NBTPathMultiImpl(selectorsView);
            }

            var selector = readPathSelector(reader);
            if (selector == null) {
                reader.reset();
                String message = "Invalid NBT path selector (reading from " + reader + ")";
                throw new IllegalArgumentException(message);
            }

            selectors.add(selector);
        }
    }

    // Returning null indicates a failure to read
    @SuppressWarnings("ResultOfMethodCallIgnored")
    static @Nullable NBTPathImpl.Selector<?> readPathSelector(@NotNull StringReader reader) throws IOException {
        var firstChar = peek(reader);
        return switch (firstChar) {
            case '.' -> {
                reader.skip(1); // Skip period

                var string = readString(reader);
                yield string != null ? new CompoundKey(string) : null;
            }
            case '{' -> {
                var compound = NBTUtils.readCompoundSNBT(reader);
                yield compound != null ? new CompoundFilter(compound) : null;
            }
            case '[' -> {
                reader.skip(1); // Skip opening square brackets

                var secondChar = peek(reader);
                var selector = switch (secondChar) {
                    case ']' -> new EntireList();
                    case '{' -> {
                        var compound = NBTUtils.readCompoundSNBT(reader);
                        yield compound != null ? new ListFilter(compound) : null;
                    }
                    default -> {
                        if (VALID_INTEGER_CHARACTERS.contains(secondChar)) {
                            var index = readInteger(reader);
                            yield index != null ? new ListIndex(index) : null;
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
}
