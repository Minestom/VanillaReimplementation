package net.minestom.vanilla.datapack.nbt;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.kyori.adventure.nbt.TagStringIO;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.StringReader;

/**
 * Contains various NBT-related utilities
 */
public class NBTUtils {

    /**
     * Checks to see if everything in {@code guarantee} is contained in {@code comparison}. The comparison is allowed to
     * have extra fields that are not contained in the guarantee.
     * @param guarantee the guarantee that the comparison must have all elements of
     * @param comparison the comparison, that is being compared against the guarantee. NBT compounds in this parameter,
     *                   whether deeper in the tree or not, are allowed to have keys that the guarantee does not - it's
     *                   basically compared against a standard.
     * @param assureListOrder whether to assure list order. When true, lists are directly compared, but when
     *                        false, the comparison is checked to see if it contains each item in the guarantee.
     * @return true if the comparison fits the guarantee, otherwise false
     */
    public static boolean compareNBT(@Nullable BinaryTag guarantee, @Nullable BinaryTag comparison, boolean assureListOrder) {
        if (guarantee == null) {
            // If there's no guarantee, it must always pass
            return true;
        }
        if (comparison == null) {
            // If it's null at this point, we already assured that guarantee is not null, so it must be invalid
            return false;
        }
        if (!guarantee.type().equals(comparison.type())) {
            // If the types aren't equal it can't fulfill the guarantee anyway
            return false;
        }
        // If the list order is assured, it will be handled with the simple #equals call later in the method
        if (!assureListOrder && guarantee instanceof ListBinaryTag guaranteeList) {
            ListBinaryTag comparisonList = ((ListBinaryTag) comparison);
            if (guaranteeList.size() == 0) {
                return comparisonList.size() == 0;
            }
            for (BinaryTag nbt : guaranteeList) {
                boolean contains = false;
                for (BinaryTag compare : comparisonList) {
                    if (compareNBT(nbt, compare, false)) {
                        contains = true;
                        break;
                    }
                }
                if (!contains) {
                    return false;
                }
            }
            return true;
        }

        if (guarantee instanceof CompoundBinaryTag guaranteeCompound) {
            CompoundBinaryTag comparisonCompound = ((CompoundBinaryTag) comparison);
            for (String key : guaranteeCompound.keySet()) {
                if (!compareNBT(guaranteeCompound.get(key), comparisonCompound.get(key), assureListOrder)) {
                    return false;
                }
            }
            return true;
        }

        return guarantee.equals(comparison);
    }

    /**
     * Reads a NBT compound from the provided reader.<br>
     * This implementation may be slow, as it may try to parse NBT many times, but this is unavoidable for now.
     */
    public static @Nullable CompoundBinaryTag readCompoundSNBT(@NotNull StringReader reader) throws IOException {
        if (reader.read() != '{') {
            return null;
        }
        StringBuilder string = new StringBuilder("{");

        while (true) {
            // Since this is a compound we should always read to at least the next closing curly brackets. However, we
            // can't count brackets and skip to until we think they should be valid because they could be escaped.

            int next;
            do {
                next = reader.read();

                if (next == -1) {
                    return null;
                }

                string.appendCodePoint(next);
            } while (next != '}');

            try {
                return TagStringIO.get().asCompound(string.toString());
            } catch (IOException ignored) {}
        }
    }
}
