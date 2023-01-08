package net.minestom.vanilla.generation.noise;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minestom.vanilla.generation.Util;

public interface NoiseSettings {

    int minY();

    int height();

    int xzSize();

    int ySize();

    record NoiseSettingsImpl(int minY, int height, int xzSize, int ySize) implements NoiseSettings {}

    static NoiseSettings create(int minY, int height) {
        return new NoiseSettingsImpl(minY, height, 1, 1);
    }

    static NoiseSettings fromJson(Object obj) {
        if (obj instanceof String str)
            return fromJson(new Gson().fromJson(str, JsonObject.class));
        if (!(obj instanceof JsonObject root))
            throw new IllegalStateException("Root is not a JsonObject");
        int minY = Util.<Integer>jsonElse(root, "min_y", 0, JsonElement::getAsInt);
        int height = Util.<Integer>jsonElse(root, "height", 256, JsonElement::getAsInt);
        int xzSize = Util.<Integer>jsonElse(root, "size_horizontal", 1, JsonElement::getAsInt);
        int ySize = Util.<Integer>jsonElse(root, "size_vertical", 1, JsonElement::getAsInt);
        return new NoiseSettingsImpl(minY, height, xzSize, ySize);
    }

    static int cellHeight(NoiseSettings settings) {
        return settings.ySize() << 2;
    }

    static int cellWidth(NoiseSettings settings) {
        return settings.xzSize() << 2;
    }

    static double cellCountY(NoiseSettings settings) {
        return settings.height() / cellHeight(settings);
    }

    static double minCellY(NoiseSettings settings) {
        return Math.floor(settings.minY() / cellHeight(settings));
    }

    interface SlideSettings {
        double target();

        double size();

        double offset();

        static SlideSettings fromJson(Object obj) {
            if (obj instanceof String str)
                return SlideSettings.fromJson(new Gson().fromJson(str, JsonObject.class));
            if (!(obj instanceof JsonObject root))
                throw new IllegalStateException("Root is not a JsonObject");
            double target = Util.<Double>jsonElse(root, "target", 0.0, JsonElement::getAsDouble);
            double size = Util.<Double>jsonElse(root, "size", 0.0, JsonElement::getAsDouble);
            double offset = Util.<Double>jsonElse(root, "offset", 0.0, JsonElement::getAsDouble);

            record SlideSettingsImpl(double target, double size, double offset) implements SlideSettings {}
            return new SlideSettingsImpl(target, size, offset);
        }

        static double apply(SlideSettings slide, double density, double y) {
            if (slide.size() <= 0) return density;
            double t = (y - slide.offset()) / slide.size();
            return Util.clampedLerp(slide.target(), density, t);
        }
    }
}