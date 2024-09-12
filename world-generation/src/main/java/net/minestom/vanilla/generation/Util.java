package net.minestom.vanilla.generation;

public class Util {

    private static final int[] MULTIPLY_DE_BRUIJN_BIT_POSITION = new int[]{0, 1, 28, 2, 29, 14, 24, 3, 30, 22, 20, 15, 25, 17, 4, 8, 31, 27, 13, 23, 21, 19, 16, 7, 26, 12, 18, 6, 11, 5, 10, 9};

    public static boolean isPowerOfTwo(int number) {
        return number != 0 && (number & number - 1) == 0;
    }

    public static int smallestEncompassingPowerOfTwo(int number) {
        int temp = number - 1;
        temp |= temp >> 1;
        temp |= temp >> 2;
        temp |= temp >> 4;
        temp |= temp >> 8;
        temp |= temp >> 16;
        return temp + 1;
    }

    public static int ceillog2(int number) {
        number = isPowerOfTwo(number) ? number : smallestEncompassingPowerOfTwo(number);
        return MULTIPLY_DE_BRUIJN_BIT_POSITION[(int)((long)number * 125613361L >> 27) & 31];
    }

    public static int quartFromBlock(int block) {
        return block >> 2;
    }

    public static int blockFromQuart(int quart) {
        return quart << 2;
    }

    public static int quartFromSection(int section) {
        return section << 2;
    }

    public static double lerp(double t, double start, double end) {
        return start + t * (end - start);
    }

    public static double clampedLerp(double start, double end, double t) {
        if (t < 0.0) {
            return start;
        } else {
            return t > 1.0 ? end : lerp(t, start, end);
        }
    }

    public static double inverseLerp(double value, double start, double end) {
        return (value - start) / (end - start);
    }

    public static double clampedMap(double value, double start1, double end1, double start2, double end2) {
        return clampedLerp(start2, end2, inverseLerp(value, start1, end1));
    }
}
