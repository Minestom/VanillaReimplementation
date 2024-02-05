package net.minestom.vanilla.datapack;

import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.datapack.worldgen.DensityFunction;
import net.minestom.vanilla.datapack.worldgen.noise.Noise;
import net.minestom.vanilla.files.FileSystem;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DatapackUtils {
    public static Optional<Noise> findNoise(Datapack datapack, String file) {
        return findInJsonData(file, datapack, data -> data.world_gen().noise());
    }

    public static Optional<DensityFunction> findDensityFunction(Datapack datapack, String file) {
        return findInJsonData(file, datapack, data -> data.world_gen().density_function());
    }

    public static Set<NamespaceID> findTags(Datapack datapack, String tagType, NamespaceID namespaceID) {
        Datapack.NamespacedData data = datapack.namespacedData().get(namespaceID.namespace());
        if (data == null) return Set.of();

        var itemTags = data.tags().folder(tagType);
        var itemTag = itemTags.file(namespaceID.value() + ".json");
        if (itemTag == null) return Set.of();

        return resolveTagItems(datapack, itemTag);
    }


    private static Set<NamespaceID> resolveTagItems(Datapack datapack, Datapack.Tag tag) {
        Set<NamespaceID> materials = new HashSet<>();
        for (Datapack.Tag.TagValue value : tag.values()) {
            resolveTagValue(datapack, value, materials::add);
        }
        return Set.copyOf(materials);
    }

    private static void resolveTagValue(Datapack datapack, Datapack.Tag.TagValue value, Consumer<NamespaceID> out) {
        if (value instanceof Datapack.Tag.TagValue.ObjectOrTagReference objectOrTagReference) {
            if (objectOrTagReference.tag().domain().startsWith("#")) {
                // starting with a hashtag means this is a reference to another tag
                // first remove the hashtag
                NamespaceID newNamespace = NamespaceID.from(objectOrTagReference.tag().domain().substring(1), objectOrTagReference.tag().path());
                var mats = resolveReferenceTag(datapack, newNamespace);
                if (mats != null) {
                    mats.forEach(out);
                    return;
                }
                throw new UnsupportedOperationException("Unable to resolve where tag " + objectOrTagReference.tag() + " is pointing to");
            }

            // found the material
            out.accept(objectOrTagReference.tag());
            return;
        }
        if (value instanceof Datapack.Tag.TagValue.TagEntry tagEntry) {
            try {
                resolveTagValue(datapack, tagEntry.id(), out);
            } catch (UnsupportedOperationException e) {
                if (tagEntry.required() == null || tagEntry.required()) {
                    throw e;
                }
            }
            return;
        }
        throw new UnsupportedOperationException("Unknown tag value type " + value.getClass().getName());
    }

    private static @Nullable Set<NamespaceID> resolveReferenceTag(Datapack datapack, NamespaceID tagNamespace) {
        // otherwise resolve to another tag
        for (var entry : datapack.namespacedData().entrySet()) {
            String namespace = entry.getKey();
            Datapack.NamespacedData data = entry.getValue();
            var itemTags = data.tags().folder("items");
            for (var itemEntry : itemTags.files().stream()
                    .collect(Collectors.toUnmodifiableMap(Function.identity(), itemTags::file)).entrySet()) {
                String tagName = itemEntry.getKey().replace(".json", "");
                Datapack.Tag itemTag = itemEntry.getValue();

                NamespaceID namespacedTag = NamespaceID.from(namespace, tagName);
                if (namespacedTag.equals(tagNamespace)) {
                    return resolveTagItems(datapack, itemTag);
                }
            }
        }

        return null;
    }

    private static <T> Optional<T> findInJsonData(String file, Datapack datapack, Function<Datapack.NamespacedData, FileSystem<T>> getFolder) {
        NamespaceID namespaceID = NamespaceID.from(file);
        for (var entry : datapack.namespacedData().entrySet()) {

            // Ensure the namespaces match
            String namespace = entry.getKey();
            if (!namespaceID.namespace().equals(namespace)) {
                continue;
            }

            // get the folder
            var data = entry.getValue();
            FileSystem<T> folder = getFolder.apply(data);

            String targetFile = namespaceID.value();
            while (targetFile.contains("/")) {
                String targetFolder = targetFile.substring(0, targetFile.indexOf('/'));
                targetFile = targetFile.substring(targetFile.indexOf('/') + 1);
                folder = folder.folder(targetFolder);
            }

            for (String fileName : folder.files()) {
                // ensure we are working with a json file
                if (!fileName.endsWith(".json")) {
                    continue;
                }
                String fileId = fileName.substring(0, fileName.length() - 5);

                if (fileId.equals(targetFile)) {
                    return Optional.of(folder.file(fileName));
                }
            }
        }
        return Optional.empty();
    }
}
