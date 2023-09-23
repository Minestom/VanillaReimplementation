package net.minestom.vanilla.datapack.worldgen.noise;

import com.squareup.moshi.JsonReader;
import net.minestom.vanilla.datapack.DatapackLoader;
import net.minestom.vanilla.datapack.json.JsonUtils;

import java.io.IOException;

public interface Noise {
    Noise ZERO = new NoiseZero();

    double sample(double x, double y, double z);

    double minValue();
    double maxValue();

    static Noise fromJson(JsonReader reader) throws IOException {
        return JsonUtils.typeMap(reader, token -> switch (token) {
            case STRING -> json -> { // string means use a json-defined noise. This will need to be lazily loaded.
                String id = json.nextString();
                return new LazyLoadedNoise(id, DatapackLoader.loading());
            };
            case BEGIN_OBJECT -> json -> {
                NormalNoise.Config params = DatapackLoader.moshi(NormalNoise.Config.class).apply(json);
                if (DatapackLoader.loading().isStatic()) {
                    // if we're static, to match vanilla we return zero.
                    return Noise.ZERO;
                }
                return new NormalNoise(DatapackLoader.loading().random(), params);
            };
            default -> null;
        });
    }
}


record NoiseZero() implements Noise {

    @Override
    public double sample(double x, double y, double z) {
        return 0;
    }

    @Override
    public double minValue() {
        return 0;
    }

    @Override
    public double maxValue() {
        return 0;
    }
}