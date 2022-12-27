package net.minestom.vanilla.generation.noise;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minestom.vanilla.generation.math.Util;

public interface NoiseSettings {
//    export type NoiseSettings = {
//        minY: number,
//                height: number,
//                xzSize: number,
//                ySize: number,
//    }

    int minY();

    int height();

    int xzSize();

    int ySize();



    //    export namespace NoiseSettings {
//        export function fromJson(obj: any): NoiseSettings {
//		const root = Json.readObject(obj) ?? {}
//            return {
//                    minY: Json.readInt(root.min_y) ?? 0,
//                    height: Json.readInt(root.height) ?? 256,
//                    xzSize: Json.readInt(root.size_horizontal) ?? 1,
//                    ySize: Json.readInt(root.size_vertical) ?? 1,
//		}
//        }

    static NoiseSettings create(int minY, int height) {
        return new NoiseSettings() {
            @Override
            public int minY() {
                return minY;
            }

            @Override
            public int height() {
                return height;
            }

            @Override
            public int xzSize() {
                return 1;
            }

            @Override
            public int ySize() {
                return 1;
            }
        };
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
        return new NoiseSettings() {
            @Override
            public int minY() {
                return minY;
            }

            @Override
            public int height() {
                return height;
            }

            @Override
            public int xzSize() {
                return xzSize;
            }

            @Override
            public int ySize() {
                return ySize;
            }
        };
    }

    //        export function cellHeight(settings: NoiseSettings) {
//            return settings.ySize << 2
//        }
    static int cellHeight(NoiseSettings settings) {
        return settings.ySize() << 2;
    }

    //        export function cellWidth(settings: NoiseSettings) {
//            return settings.xzSize << 2
//        }
    static int cellWidth(NoiseSettings settings) {
        return settings.xzSize() << 2;
    }

    //        export function cellCountY(settings: NoiseSettings) {
//            return settings.height / cellHeight(settings)
//        }
    static double cellCountY(NoiseSettings settings) {
        return settings.height() / cellHeight(settings);
    }

    //        export function minCellY(settings: NoiseSettings) {
//            return Math.floor(settings.minY / cellHeight(settings))
//        }
    static double minCellY(NoiseSettings settings) {
        return Math.floor(settings.minY() / cellHeight(settings));
    }

    //    export type NoiseSlideSettings = {
//        target: number,
//                size: number,
//                offset: number,
//    }
    interface SlideSettings {
        double target();

        double size();

        double offset();

//        export function fromJson(obj: unknown): NoiseSlideSettings {
//		    const root = Json.readObject(obj) ?? {}
//            return {
//                    target: Json.readNumber(root.target) ?? 0,
//                    size: Json.readInt(root.size) ?? 0,
//                    offset: Json.readInt(root.offset) ?? 0,
//            }
//        }

        static SlideSettings fromJson(Object obj) {
            if (obj instanceof String str)
                return SlideSettings.fromJson(new Gson().fromJson(str, JsonObject.class));
            if (!(obj instanceof JsonObject root))
                throw new IllegalStateException("Root is not a JsonObject");
            double target = Util.<Double>jsonElse(root, "target", 0.0, JsonElement::getAsDouble);
            double size = Util.<Double>jsonElse(root, "size", 0.0, JsonElement::getAsDouble);
            double offset = Util.<Double>jsonElse(root, "offset", 0.0, JsonElement::getAsDouble);
            return new SlideSettings() {
                @Override
                public double target() {
                    return target;
                }

                @Override
                public double size() {
                    return size;
                }

                @Override
                public double offset() {
                    return offset;
                }
            };
        }

//        export function apply(slide: NoiseSlideSettings, density: number, y: number) {
//            if (slide.size <= 0) return density
//            const t = (y - slide.offset) / slide.size
//            return clampedLerp(slide.target, density, t)
//        }

        static double apply(SlideSettings slide, double density, double y) {
            if (slide.size() <= 0) return density;
            double t = (y - slide.offset()) / slide.size();
            return Util.clampedLerp(slide.target(), density, t);
        }
    }
}