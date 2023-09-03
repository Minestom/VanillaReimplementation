package net.minestom.vanilla.datapack.worldgen.noise;

import net.minestom.vanilla.datapack.worldgen.random.WorldgenRandom;

public class SimplexNoise implements Noise {

    static final int[][] GRADIENT = new int[][]{{1, 1, 0}, {-1, 1, 0}, {1, -1, 0}, {-1, -1, 0}, {1, 0, 1},
            {-1, 0, 1}, {1, 0, -1}, {-1, 0, -1}, {0, 1, 1}, {0, -1, 1}, {0, 1, -1}, {0, -1, -1}, {1, 1, 0}, {0, -1, 1},
            {-1, 1, 0}, {0, -1, -1}};
    private static final double F2 = 0.5 * (Math.sqrt(3.0) - 1.0);
    private static final double G2 = (3.0 - Math.sqrt(3.0)) / 6.0;

    public final int[] p = new int[512];
    public final double xo;
    public final double yo;
    public final double zo;

    public SimplexNoise(WorldgenRandom random) {
        this.xo = random.nextDouble() * 256.0;
        this.yo = random.nextDouble() * 256.0;
        this.zo = random.nextDouble() * 256.0;

        {
            int i = 0;
            while (i < 256) {
                this.p[i] = i++;
            }
        }
        for (int i = 0; i < 256; ++i) {
            int j = random.nextInt(256 - i);
            int b = this.p[i];
            this.p[i] = this.p[i + j];
            this.p[i + j] = b;
        }
    }

    public double sample2D(double x, double z) {
        double offset = (x + z) * F2;
        int offsetA = floor(x + offset);
        int offsetB = floor(z + offset);
        double diff = (double) (offsetA + offsetB) * G2;
        double diffA = (double) offsetA - diff;
        double diffB = (double) offsetB - diff;
        double adjustedA = x - diffA;
        double adjustedB = z - diffB;
        byte aIsLarger;
        byte bIsLarger;
        if (adjustedA > adjustedB) {
            aIsLarger = 1;
            bIsLarger = 0;
        } else {
            aIsLarger = 0;
            bIsLarger = 1;
        }

        double a1 = adjustedA - (double) aIsLarger + G2;
        double b1 = adjustedB - (double) bIsLarger + G2;
        double a2 = adjustedA - 1.0 + 2.0 * G2;
        double b2 = adjustedB - 1.0 + 2.0 * G2;
        int a3 = offsetA & 255;
        int b3 = offsetB & 255;
        int x1 = this.get(a3 + this.get(b3)) % 12;
        int y1 = this.get(a3 + aIsLarger + this.get(b3 + bIsLarger)) % 12;
        int z1 = this.get(a3 + 1 + this.get(b3 + 1)) % 12;
        double x2 = this.getCornerNoise3D(x1, adjustedA, adjustedB, 0.0, 0.5);
        double y2 = this.getCornerNoise3D(y1, a1, b1, 0.0, 0.5);
        double z2 = this.getCornerNoise3D(z1, a2, b2, 0.0, 0.5);
        return 70.0 * (x2 + y2 + z2);
    }

    private static int floor(double x) {
        int intX = (int) x;
        return x < (double) intX ? intX - 1 : intX;
    }

    public double sample(double x, double y, double z) {
        double offset = (x + y + z) * 0.3333333333333333;
        int x1 = floor(x + offset);
        int y1 = floor(y + offset);
        int z1 = floor(z + offset);
        double diff = (double) (x1 + y1 + z1) * 0.16666666666666666;
        double x2 = (double) x1 - diff;
        double y2 = (double) y1 - diff;
        double z2 = (double) z1 - diff;
        double x3 = x - x2;
        double y3 = y - y2;
        double z3 = z - z2;
        byte x4;
        byte y4;
        byte z4;
        byte x5;
        byte y5;
        byte z5;
        if (x3 >= y3) {
            if (y3 >= z3) {
                x4 = 1;
                y4 = 0;
                z4 = 0;
                x5 = 1;
                y5 = 1;
                z5 = 0;
            } else if (x3 >= z3) {
                x4 = 1;
                y4 = 0;
                z4 = 0;
                x5 = 1;
                y5 = 0;
                z5 = 1;
            } else {
                x4 = 0;
                y4 = 0;
                z4 = 1;
                x5 = 1;
                y5 = 0;
                z5 = 1;
            }
        } else if (y3 < z3) {
            x4 = 0;
            y4 = 0;
            z4 = 1;
            x5 = 0;
            y5 = 1;
            z5 = 1;
        } else if (x3 < z3) {
            x4 = 0;
            y4 = 1;
            z4 = 0;
            x5 = 0;
            y5 = 1;
            z5 = 1;
        } else {
            x4 = 0;
            y4 = 1;
            z4 = 0;
            x5 = 1;
            y5 = 1;
            z5 = 0;
        }

        double x6 = x3 - (double) x4 + 0.16666666666666666;
        double y6 = y3 - (double) y4 + 0.16666666666666666;
        double z6 = z3 - (double) z4 + 0.16666666666666666;
        double x7 = x3 - (double) x5 + 0.3333333333333333;
        double y7 = y3 - (double) y5 + 0.3333333333333333;
        double z7 = z3 - (double) z5 + 0.3333333333333333;
        double x8 = x3 - 1.0 + 0.5;
        double y8 = y3 - 1.0 + 0.5;
        double z8 = z3 - 1.0 + 0.5;
        int x9 = x1 & 255;
        int y9 = y1 & 255;
        int z9 = z1 & 255;
        int a = this.get(x9 + this.get(y9 + this.get(z9))) % 12;
        int b = this.get(x9 + x4 + this.get(y9 + y4 + this.get(z9 + z4))) % 12;
        int c = this.get(x9 + x5 + this.get(y9 + y5 + this.get(z9 + z5))) % 12;
        int d = this.get(x9 + 1 + this.get(y9 + 1 + this.get(z9 + 1))) % 12;
        double e = this.getCornerNoise3D(a, x3, y3, z3, 0.6);
        double f = this.getCornerNoise3D(b, x6, y6, z6, 0.6);
        double g = this.getCornerNoise3D(c, x7, y7, z7, 0.6);
        double h = this.getCornerNoise3D(d, x8, y8, z8, 0.6);
        return 32.0 * (e + f + g + h);
    }

    @Override
    public double minValue() {
        return 0;
    }

    @Override
    public double maxValue() {
        return 1;
    }

    private int get(int i) {
        return this.p[i & 255];
    }

    private double getCornerNoise3D(int i, double a, double b, double c, double d) {
        double e = d - a * a - b * b - c * c;
        double f;
        if (e < 0.0) {
            f = 0.0;
        } else {
            e *= e;
            f = e * e * dot(GRADIENT[i], a, b, c);
        }
        return f;
    }

    protected static double dot(int[] grad, double a, double b, double c) {
        return (double) grad[0] * a + (double) grad[1] * b + (double) grad[2] * c;
    }
}
