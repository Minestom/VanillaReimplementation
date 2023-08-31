package net.minestom.vanilla.datapack;

import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.datapack.worldgen.noise.Noise;

import java.util.Map;
import java.util.Optional;

public class DatapackUtils {
    public static Optional<Noise> findNoise(Datapack datapack, String id) {
        for (var data : datapack.namespacedData().values()) {
            var noises = data.world_gen().noise();
            for (String fileName : noises.files()) {

            }
        }
    }
}
