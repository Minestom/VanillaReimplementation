package net.minestom.vanilla.datapack.worldgen.noise;

import com.squareup.moshi.JsonReader;
import net.minestom.vanilla.datapack.DatapackLoader;
import net.minestom.vanilla.datapack.DatapackUtils;
import net.minestom.vanilla.datapack.json.JsonUtils;

import java.io.IOException;

public interface Noise {
    double sample(double x, double y, double z);

    double minValue();
    double maxValue();

    static Noise fromJson(JsonReader reader) throws IOException {
        var context = DatapackLoader.loading();
        return JsonUtils.typeMap(reader, token -> switch (token) {
            case STRING -> json -> { // string means use a json-defined noise. This will need to be lazily loaded.
                String id = json.nextString();
                return new LazyLoadedNoise(id, context);
            };
            case BEGIN_OBJECT -> json -> {
                NormalNoise.NoiseParameters params = DatapackLoader.moshi(NormalNoise.NoiseParameters.class).apply(json);
                return new NormalNoise(context.random(), params);
            };
            default -> null;
        });
    }
}
