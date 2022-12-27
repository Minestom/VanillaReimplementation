package net.minestom.vanilla.generation.noise;

import com.google.gson.JsonObject;
import net.minestom.vanilla.generation.math.Util;

public interface VerticalAnchor {

    //    export type VerticalAnchor = (context: WorldgenContext) => number
    int apply(WorldgenContext context);

    //    export function fromJson(obj: unknown): VerticalAnchor {
//		const root = Json.readObject(obj) ?? {}
//        if (root.absolute !== undefined) {
//            return absolute(Json.readNumber(root.absolute) ?? 0)
//        } else if (root.above_bottom !== undefined) {
//            return aboveBottom(Json.readNumber(root.above_bottom) ?? 0)
//        } else if (root.below_top !== undefined) {
//            return belowTop(Json.readNumber(root.below_top) ?? 0)
//        }
//        return () => 0
//    }
    static VerticalAnchor fromJson(Object obj) {
        JsonObject json = Util.jsonObject(obj);
        if (json.has("absolute")) {
            return absolute(json.get("absolute").getAsInt());
        } else if (json.has("above_bottom")) {
            return aboveBottom(json.get("above_bottom").getAsInt());
        } else if (json.has("below_top")) {
            return belowTop(json.get("below_top").getAsInt());
        }
        return context -> 0;
    }

    //    function absolute(value: number): VerticalAnchor {
//        return () => value
//    }
    static VerticalAnchor absolute(int value) {
        return context -> value;
    }

    //    function aboveBottom(value: number): VerticalAnchor {
//        return context => context.minY + value
//    }
    static VerticalAnchor aboveBottom(int value) {
        return context -> context.minY() + value;
    }

    //    function belowTop(value: number): VerticalAnchor {
//        return context => context.maxY - value
//    }
    static VerticalAnchor belowTop(int value) {
        return context -> context.maxY() - value;
    }

    //    export interface WorldgenContext {
//        minY: number
//        height: number
//        maxY: number
//    }
    interface WorldgenContext {
        int minY();

        int height();

        int maxY();

        static WorldgenContext create(int minY, int height) {
            return new WorldgenContext() {
                @Override
                public int minY() {
                    return minY;
                }

                @Override
                public int height() {
                    return height;
                }

                @Override
                public int maxY() {
                    return minY + height - 1;
                }
            };
        }
    }

    //    export function create(minY: number, height: number) {
//        return {
//                minY,
//                height,
//                maxY: minY + height - 1,
//		}
//
}
