package net.minestom.vanilla.generation.noise;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minestom.server.instance.block.Block;
import net.minestom.vanilla.generation.math.Util;

public record NoiseGeneratorSettings(
        NoiseSettings noise,
        SurfaceSystem.SurfaceRule surfaceRule,
        Block defaultBlock,
        Block defaultFluid,
        NoiseRouter noiseRouter,
        int seaLevel,
        boolean disableMobGeneration,
        boolean aquifersEnabled,
        boolean oreVeinsEnabled,
        boolean legacyRandomSource) {

//    interface NoiseGeneratorSettings {
//        noise:NoiseSettings,
//        surfaceRule:SurfaceRule,
//        defaultBlock:BlockState,
//        defaultFluid:BlockState,
//        noiseRouter:NoiseRouter,
//        seaLevel:number,
//        disableMobGeneration:boolean,
//        aquifersEnabled:boolean,
//        oreVeinsEnabled:boolean,
//        legacyRandomSource:boolean,
//    }


//    NoiseGeneratorSettings {
//        export function fromJson(obj:unknown):NoiseGeneratorSettings {
//		const root = Json.readObject(obj) ??{
//            }
//            return {
//                    surfaceRule:SurfaceRule.fromJson(root.surface_rule),
//                    noise:NoiseSettings.fromJson(root.noise),
//                    defaultBlock:BlockState.fromJson(root.default_block),
//                    defaultFluid:BlockState.fromJson(root.default_fluid),
//                    noiseRouter:NoiseRouter.fromJson(root.noise_router),
//                    seaLevel:Json.readInt(root.sea_level) ??0,
//                    disableMobGeneration:Json.readBoolean(root.disable_mob_generation) ??false,
//                    aquifersEnabled:Json.readBoolean(root.aquifers_enabled) ??false,
//                    oreVeinsEnabled:Json.readBoolean(root.ore_veins_enabled) ??false,
//                    legacyRandomSource:Json.readBoolean(root.legacy_random_source) ??false,
//		}
//        }
//
//        export function create(settings:Partial<NoiseGeneratorSettings>):NoiseGeneratorSettings {
//            return {
//                    surfaceRule:SurfaceRule.NOOP,
//                    noise:NoiseSettings.create({}),
//                    defaultBlock:BlockState.STONE,
//                    defaultFluid:BlockState.WATER,
//                    noiseRouter:NoiseRouter.create({}),
//                    seaLevel:0,
//                    disableMobGeneration:false,
//                    aquifersEnabled:false,
//                    oreVeinsEnabled:false,
//                    legacyRandomSource:false,
//			...settings,
//		}
//        }
//    }


    public static NoiseGeneratorSettings fromJson(Object obj) {
        JsonObject root = Util.jsonObject(obj);
        return new NoiseGeneratorSettings(
                Util.jsonRequire(root, "noise", NoiseSettings::fromJson),
                Util.jsonRequire(root, "surfaceRule", SurfaceSystem.SurfaceRule::fromJson),
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