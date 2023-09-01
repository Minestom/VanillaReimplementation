package net.minestom.vanilla.datapack;

import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.datapack.worldgen.DensityFunction;
import net.minestom.vanilla.datapack.worldgen.noise.Noise;
import net.minestom.vanilla.files.FileSystem;

import javax.xml.crypto.Data;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class DatapackUtils {
    public static Optional<Noise> findNoise(Datapack datapack, String file) {
        return findInJsonData(file, datapack, data -> data.world_gen().noise());
    }

    public static Optional<DensityFunction> findDensityFunction(Datapack datapack, String file) {
        return findInJsonData(file, datapack, data -> data.world_gen().density_function());
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
