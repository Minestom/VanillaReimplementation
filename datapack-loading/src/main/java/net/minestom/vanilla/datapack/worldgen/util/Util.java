package net.minestom.vanilla.datapack.worldgen.util;

import com.google.gson.*;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Util {
    public static double square(double x) {
        return x * x;
    }

    public static double cube(double x) {
        return x * x * x;
    }

    public static double clamp(double x, double min, double max) {
        return x < min ? min : Math.min(x, max);
    }

    public static double lerp(double a, double b, double c) {
        return b + a * (c - b);
    }

    public static double lerp2(double a, double b, double c, double d, double e, double f) {
        return lerp(b, lerp(a, c, d), lerp(a, e, f));
    }

    public static double lerp3(double a, double b, double c, double d, double e, double f, double g, double h, double i, double j, double k) {
        return lerp(c, lerp2(a, b, d, e, f, g), lerp2(a, b, h, i, j, k));
    }

    public static double lazyLerp(double a, DoubleSupplier b, DoubleSupplier c) {
        if (a == 1) return c.getAsDouble();
        if (a == 0) return b.getAsDouble();
        double b_value = b.getAsDouble();
        return b_value + a * (c.getAsDouble() - b_value);
    }

    public static double lazyLerp2(double a, double b, DoubleSupplier c, DoubleSupplier d, DoubleSupplier e,
                                   DoubleSupplier f) {
        return lazyLerp(b, () -> lazyLerp(a, c, d), () -> lazyLerp(a, e, f));
    }

    public static double lazyLerp3(double a, double b, double c, DoubleSupplier d, DoubleSupplier e, DoubleSupplier f,
                                   DoubleSupplier g, DoubleSupplier h, DoubleSupplier i, DoubleSupplier j,
                                   DoubleSupplier k) {
        return lazyLerp(c, () -> lazyLerp2(a, b, d, e, f, g), () -> lazyLerp2(a, b, h, i, j, k));
    }

    public static double clampedLerp(double a, double b, double c) {
        if (c < 0) {
            return a;
        } else if (c > 1) {
            return b;
        } else {
            return lerp(c, a, b);
        }
    }

    public static double inverseLerp(double a, double b, double c) {
        return (a - b) / (c - b);
    }

    public static double smoothstep(double x) {
        return x * x * x * (x * (x * 6.0 - 15.0) + 10.0);
    }

    public static double map(double a, double b, double c, double d, double e) {
        return lerp(inverseLerp(a, b, c), d, e);
    }

    public static double clampedMap(double a, double b, double c, double d, double e) {
        return clampedLerp(d, e, inverseLerp(a, b, c));
    }

    /**
     * Finds the index of the first value that matches the predicate
     * @param min inclusive
     * @param max exclusive
     */
    public static int binarySearch(int min, int max, IntPredicate predicate) {
        // TODO: Make this an actual binary search
        // slow version
        for (int i = min; i < max; i++) {
            if (predicate.test(i)) {
                return i;
            }
        }
        return -1;
    }

    public static long getSeed(int x, int y, int z) {
        long seed = (x * 3129871L) ^ (long) z * 116129781L ^ (long) y;
        seed = seed * seed * 42317861L + seed * 11L;
        return seed >> 16;
    }

    public static long longfromBytes(byte a, byte b, byte c, byte d, byte e, byte f, byte g, byte h) {
        return (long) (a) << (long) (56)
                | (long) (b) << (long) (48)
                | (long) (c) << (long) (40)
                | (long) (d) << (long) (32)
                | (long) (e) << (long) (24)
                | (long) (f) << (long) (16)
                | (long) (g) << (long) (8)
                | (long) (h);
    }

    public static boolean isPowerOfTwo(int x) {
        return (x & (x - 1)) == 0;
    }

    public static long upperPowerOfTwo(long x) {
        x -= 1;
        x |= x >> 1;
        x |= x >> 2;
        x |= x >> 4;
        x |= x >> 8;
        x |= x >> 18;
        x |= x >> 32;
        return x + 1;
    }

    public static BigInteger fromBytes(byte[] digest) {
        return new BigInteger(1, digest);
    }

    public static <T> @NotNull T jsonRequire(JsonObject root, String key, Function<JsonElement, T> mapper) {
        JsonElement element = root.get(key);
        if (element == null) {
            throw new IllegalArgumentException("Missing required key " + key);
        }
        return mapper.apply(element);
    }

    public static JsonArray jsonArray(JsonElement element) {
        if (element.isJsonArray()) {
            return element.getAsJsonArray();
        }
        throw new IllegalArgumentException("Expected array, got " + element);
    }

    public static <T> List<T> jsonArray(JsonElement element, Function<JsonElement, T> mapper) {
        if (element.isJsonArray()) {
            return StreamSupport.stream(element.getAsJsonArray().spliterator(), false)
                    .map(mapper)
                    .toList();
        }
        throw new IllegalArgumentException("Expected array, got " + element);
    }

    public static <T> @NotNull T jsonElse(JsonObject root, String key, T defaultValue, Function<JsonElement, T> mapper) {
        JsonElement element = root.get(key);
        if (element == null) {
            return defaultValue;
        }
        return mapper.apply(element);
    }

    public static <T> Supplier<T> lazy(Supplier<T> supplier) {
        return new Supplier<>() {
            private T value;

            @Override
            public T get() {
                if (value == null) {
                    value = supplier.get();
                }
                return value;
            }
        };
    }

    public static IntSupplier lazyInt(IntSupplier supplier) {
        return new IntSupplier() {
            private int value;

            @Override
            public int getAsInt() {
                if (value == 0) {
                    value = supplier.getAsInt();
                }
                return value;
            }
        };
    }

    public static DoubleSupplier lazyDouble(DoubleSupplier supplier) {
        return new DoubleSupplier() {
            private double value;

            @Override
            public double getAsDouble() {
                if (value == 0) {
                    value = supplier.getAsDouble();
                }
                return value;
            }
        };
    }

    public static JsonObject jsonObject(Object obj) {
        if (obj instanceof String str)
            return jsonObject(new Gson().fromJson(str, JsonElement.class));
        if (!(obj instanceof JsonObject object))
            throw new IllegalArgumentException("Expected a JsonObject, got " + obj.getClass().getName());
        return object;
    }

    public static <A> Function<JsonElement, List<A>> jsonReadArray(Function<JsonElement, A> mapper) {
        return element -> {
            if (element.isJsonArray()) {
                return StreamSupport.stream(element.getAsJsonArray().spliterator(), false)
                        .map(mapper)
                        .collect(Collectors.toList());
            } else {
                return Collections.singletonList(mapper.apply(element));
            }
        };
    }

    public static int chunkMinX(Point chunkPos) {
        int chunkX = chunkPos.chunkX();
        return chunkX * Chunk.CHUNK_SIZE_X;
    }

    public static int chunkMinZ(Point chunkPos) {
        int chunkZ = chunkPos.chunkZ();
        return chunkZ * Chunk.CHUNK_SIZE_Z;
    }

    public static int chunkMaxX(Point chunkPos) {
        return chunkMinX(chunkPos) + Chunk.CHUNK_SIZE_X;
    }

    public static int chunkMaxZ(Point chunkPos) {
        return chunkMinZ(chunkPos) + Chunk.CHUNK_SIZE_Z;
    }

    public static byte[] md5(String name) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            return md.digest(name.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static Function<JsonElement, Block> jsonVanillaBlock() {
        return element -> {
            if (element.isJsonPrimitive()) {
                String name = element.getAsString();
                return Block.fromNamespaceId(name);
            }

            JsonObject object = element.getAsJsonObject();
            String name = object.get("Name").getAsString();
            Block block = Block.fromNamespaceId(name);
            if (block == null) {
                throw new IllegalArgumentException("Unknown block " + name);
            }
            if (object.has("properties")) {
                JsonObject properties = object.getAsJsonObject("properties");
                for (var entry : properties.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue().getAsString();
                    block = block.withProperty(key, value);
                }
            }
            return block;
        };
    }

    public static int chunkMinY(Chunk chunk) {
        return chunk.getInstance().getDimensionType().getMinY();
    }

    public static int chunkMaxY(Chunk chunk) {
        return chunk.getInstance().getDimensionType().getMaxY();
    }

    public static boolean jsonIsString(Object obj) {
        if (obj instanceof String)
            return true;
        if (obj instanceof JsonPrimitive primitive)
            return primitive.isString();
        return false;
    }

    public static String jsonToString(Object obj) {
        if (obj instanceof String str)
            return str;
        if (obj instanceof JsonPrimitive primitive)
            return primitive.getAsString();
        throw new IllegalArgumentException("Expected a string, got " + obj.getClass().getName());
    }

    public static @NotNull Map<String, String> noiseParametersJsons() {
        return allFilesWithin(Paths.get("vanilla_worldgen/worldgen/noise"));
    }

    public static @NotNull Map<String, String> densityFunctionJsons() {
        return allFilesWithin(Paths.get("vanilla_worldgen/worldgen/density_function"));
    }

    public static @NotNull Map<String, String> noiseSettingsJsons() {
        return allFilesWithin(Paths.get("vanilla_worldgen/worldgen/noise_settings"));
    }

    private static @NotNull Map<String, String> allFilesWithin(Path path) {
        try (Stream<Path> stream = Files.walk(path)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().endsWith(".json"))
                    .collect(Collectors.toMap(
                            p -> p.toString()
                                    .substring(path.toString().length() + 1)
                                    // remove .json
                                    .replace(".json", "")
                                    .replace("\\", "/"),
                            p -> {
                                try {
                                    return Files.readString(p);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    ));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static @Nullable Double parseDouble(String str) {
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static long cantor(long a, long b) {
        return (a + b) * (a + b + 1L) / 2L + b;
    }

    public static long hash(int x, int y, int z) {
        return cantor(x, cantor(y, z));
    }

    public record Seed(long low, long high) {
        public Seed mixed() {
            return new Seed(staffordMix13(low), staffordMix13(high));
        }
    }

    public static Seed extract128Seed(long $$0) {
        long $$1 = $$0 ^ 0x6a09e667f3bcc909L;
        long $$2 = $$1 - 0x61c8864680b583ebL;
        return new Seed($$1, $$2);
    }


    /* David Stafford's (http://zimbry.blogspot.com/2011/09/better-bit-mixing-improving-on.html)
     * "Mix13" variant of the 64-bit finalizer in Austin Appleby's MurmurHash3 algorithm. */
    public static long staffordMix13(long z) {
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }
}