package net.minestom.vanilla.datapack.number;

import com.squareup.moshi.JsonReader;
import net.minestom.vanilla.datapack.DatapackLoader;
import net.minestom.vanilla.datapack.json.JsonUtils;

import java.io.IOException;
import java.util.Map;
import java.util.random.RandomGenerator;

public interface NumberProvider {

    Int asInt();

    Double asDouble();

    interface Context {
        // TODO: Scoreboard query
        RandomGenerator random();
    }

    interface Int extends NumberProvider, IntNumberProviders {

        int apply(Context context);

        default Double asDouble() {
            return context -> (double) apply(context);
        }

        default Int asInt() {
            return this;
        }

        static NumberProvider.Int fromJson(JsonReader reader) throws IOException {
            return JsonUtils.typeMap(reader, Map.of(
                    JsonReader.Token.NUMBER, json -> constant(reader.nextInt()),
                    JsonReader.Token.BEGIN_OBJECT, json -> JsonUtils.unionNamespaceStringType(json, "type", Map.of(
                            "minecraft:constant", DatapackLoader.moshi(Constant.class),
                            "minecraft:uniform", DatapackLoader.moshi(Uniform.class),
                            "minecraft:binomial", DatapackLoader.moshi(Binomial.class)
                    ))
            ));
        }

        static NumberProvider.Int constant(int value) {
            return new IntNumberProviders.Constant(value);
        }

        static NumberProvider.Int uniform(NumberProvider.Int min, NumberProvider.Int max) {
            return new IntNumberProviders.Uniform(min, max);
        }

        static NumberProvider.Int binomial(NumberProvider.Int n, NumberProvider.Double p) {
            return new IntNumberProviders.Binomial(n, p);
        }
    }

    interface Double extends NumberProvider, DoubleNumberProviders {

        double apply(Context context);

        default Int asInt() {
            return context -> (int) apply(context);
        }

        default Double asDouble() {
            return this;
        }

        static NumberProvider.Double fromJson(JsonReader reader) throws IOException {
            return JsonUtils.typeMap(reader, Map.of(
                    JsonReader.Token.NUMBER, json -> constant(reader.nextDouble()),
                    JsonReader.Token.BEGIN_OBJECT, json -> JsonUtils.unionNamespaceStringType(json, "type", Map.of(
                            "minecraft:constant", DatapackLoader.moshi(Constant.class),
                            "minecraft:uniform", DatapackLoader.moshi(Uniform.class),
                            "minecraft:binomial", DatapackLoader.moshi(Binomial.class)
                    ))
            ));
        }

        static NumberProvider.Double constant(double value) {
            return new DoubleNumberProviders.Constant(value);
        }

        static NumberProvider.Double uniform(NumberProvider.Double min, NumberProvider.Double max) {
            return new DoubleNumberProviders.Uniform(min, max);
        }

        static NumberProvider.Double binomial(NumberProvider.Int n, NumberProvider.Double p) {
            return new DoubleNumberProviders.Binomial(n, p);
        }
    }
}
