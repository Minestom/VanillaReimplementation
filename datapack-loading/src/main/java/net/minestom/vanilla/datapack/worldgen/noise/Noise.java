package net.minestom.vanilla.datapack.worldgen.noise;

import com.squareup.moshi.JsonReader;
import net.minestom.vanilla.datapack.DatapackLoader;
import net.minestom.vanilla.datapack.DatapackUtils;

import java.io.IOException;

public interface Noise {
    double sample(double x, double y, double z);

    double minValue();
    double maxValue();

    static Noise fromJson(JsonReader reader) throws IOException {
        var context = DatapackLoader.loading();
        String type = reader.nextString();
        FutureNoise future = new FutureNoise(() -> DatapackUtils.findNoise(context.acquire()));
    }
}
