package net.minestom.vanilla.generation.noise;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minestom.server.instance.block.Block;
import net.minestom.vanilla.generation.Util;
import net.minestom.vanilla.generation.surface.Surface;

public record NoiseGeneratorSettings(
        NoiseSettings noise,
        Surface.Rule surfaceRule,
        Block defaultBlock,
        Block defaultFluid,
        NoiseRouter noiseRouter,
        int seaLevel,
        boolean disableMobGeneration,
        boolean aquifersEnabled,
        boolean oreVeinsEnabled,
        boolean legacyRandomSource) {

    public static NoiseGeneratorSettings fromJson(Object obj) {
        JsonObject root = Util.jsonObject(obj);
        return new NoiseGeneratorSettings(
                Util.jsonRequire(root, "noise", NoiseSettings::fromJson),
                Util.jsonRequire(root, "surface_rule", Surface.Rule::fromJson),
                Util.jsonRequire(root, "default_block", Util.jsonVanillaBlock()),
                Util.jsonRequire(root, "default_fluid", Util.jsonVanillaBlock()),
                Util.jsonRequire(root, "noise_router", NoiseRouter::fromJson),
                Util.<Integer>jsonElse(root, "sea_level", 0, JsonElement::getAsInt),
                Util.<Boolean>jsonElse(root, "disable_mob_generation", false, JsonElement::getAsBoolean),
                Util.<Boolean>jsonElse(root, "aquifers_enabled", false, JsonElement::getAsBoolean),
                Util.<Boolean>jsonElse(root, "ore_veins_enabled", false, JsonElement::getAsBoolean),
                Util.<Boolean>jsonElse(root, "legacy_random_source", false, JsonElement::getAsBoolean)
        );
    }
}