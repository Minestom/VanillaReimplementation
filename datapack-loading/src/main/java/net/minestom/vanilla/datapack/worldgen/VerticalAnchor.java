package net.minestom.vanilla.datapack.worldgen;

import com.squareup.moshi.JsonReader;

import java.io.IOException;

sealed public interface VerticalAnchor {

    // context here is for optimization potential
    interface Context {
        int minY();
        int maxY();
    }

    int apply(Context context);

    static VerticalAnchor fromJson(JsonReader reader) throws IOException {
        // Vertical anchor is a special case...
        // thx mojang!
        reader.beginObject();
        String type = reader.nextName();
        int value = reader.nextInt();
        reader.endObject();

        return switch (type) {
            case "absolute" -> new Absolute(value);
            case "above_bottom" -> new AboveBottom(value);
            case "below_top" -> new BelowTop(value);
            default -> throw new IllegalStateException("Unexpected value: " + type);
        };
    }

    record Absolute(int value) implements VerticalAnchor {
        @Override
        public int apply(Context context) {
            return value;
        }
    }

    record AboveBottom(int offset) implements VerticalAnchor {
        @Override
        public int apply(Context context) {
            return context.minY() + offset;
        }
    }

    record BelowTop(int offset) implements VerticalAnchor {
        @Override
        public int apply(Context context) {
            return context.maxY() - offset;
        }
    }
}
