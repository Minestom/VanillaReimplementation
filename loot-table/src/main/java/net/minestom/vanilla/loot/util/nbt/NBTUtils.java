package net.minestom.vanilla.loot.util.nbt;

import net.kyori.adventure.nbt.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.CharBuffer;
import java.util.Map;

public class NBTUtils {

    private NBTUtils() {}

    /**
     * Checks to see if everything in {@code standard} is contained in {@code comparison}. The comparison is allowed to
     * have extra fields that are not contained in the standard.
     * @param standard the standard that the comparison must have all elements of
     * @param comparison the comparison, that is being compared against the standard. NBT compounds in this parameter,
     *                   whether deeper in the tree or not, are allowed to have keys that the standard does not - it's
     *                   basically compared against a standard.
     * @param assureListOrder whether or not to assure list order. When true, lists are directly compared, but when
     *                        false, the comparison is checked to see if it contains each item in the standard.
     * @return true if the comparison fits the standard, otherwise false
     */
    public static boolean compareNBT(@Nullable BinaryTag standard, @Nullable BinaryTag comparison, boolean assureListOrder) {
        if (standard == null) {
            return true; // If there's no standard, it must always pass
        } else if (comparison == null) {
            return false; // If it's null at this point, we already assured that standard is null, so it must be invalid
        } else if (!standard.type().equals(comparison.type())) {
            return false; // If the classes aren't equal it can't fulfill the standard anyway
        }
        // If the list order is assured, it will be handled with the simple #equals call later in the method
        if (!assureListOrder && standard instanceof ListBinaryTag guaranteeList) {
            ListBinaryTag comparisonList = ((ListBinaryTag) comparison);
            if (guaranteeList.isEmpty()) {
                return comparisonList.isEmpty();
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

        if (standard instanceof CompoundBinaryTag standardCompound) {
            CompoundBinaryTag comparisonCompound = ((CompoundBinaryTag) comparison);
            for (String key : standardCompound.keySet()) {
                if (!compareNBT(comparisonCompound.get(key), comparisonCompound.get(key), assureListOrder)) {
                    return false;
                }
            }
            return true;
        }

        return standard.equals(comparison);
    }

    /**
     * Merges the two provided compounds, preferring the value of the {@code changes} compound and merging any nested
     * NBT compounds like it would for the first-level ones.
     * @param base the base compound, to be merged onto
     * @param changes the changes to make to the base compound
     * @return the merged compound
     */
    public static CompoundBinaryTag merge(@NotNull CompoundBinaryTag base, @NotNull CompoundBinaryTag changes) {
        CompoundBinaryTag.Builder result = CompoundBinaryTag.builder();

        result.put(base);

        changes.iterator().forEachRemaining((entry) -> {
            BinaryTag value = entry.getValue();

            if (base.get(entry.getKey()) instanceof CompoundBinaryTag baseCompound
                && entry.getValue() instanceof CompoundBinaryTag changeCompound) {
                value = NBTUtils.merge(baseCompound, changeCompound);
            }

            result.put(entry.getKey(), value);
        });

        return result.build();
    }

}
