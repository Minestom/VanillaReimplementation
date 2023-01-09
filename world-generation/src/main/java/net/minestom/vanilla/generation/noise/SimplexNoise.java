package net.minestom.vanilla.generation.noise;

import net.minestom.vanilla.generation.random.WorldgenRandom;
import org.jetbrains.annotations.NotNull;

record SimplexNoise(int[] p, double xOffset, double yOffset, double zOffset) implements Noise.TwoDimensional {

    private static final int[][] GRADIENT = new int[][]{{1, 1, 0}, {-1, 1, 0}, {1, -1, 0}, {-1, -1, 0}, {1, 0, 1},
            {-1, 0, 1}, {1, 0, -1}, {-1, 0, -1}, {0, 1, 1}, {0, -1, 1}, {0, 1, -1}, {0, -1, -1}, {1, 1, 0}, {0, -1, 1},
            {-1, 1, 0}, {0, -1, -1}};
    private static final double F2 = 0.5 * (Math.sqrt(3.0) - 1.0);
    private static final double G2 = (3.0 - Math.sqrt(3.0)) / 6.0;

    static @NotNull SimplexNoise create(@NotNull WorldgenRandom random) {
        double xo = random.nextDouble() * 256;
        double yo = random.nextDouble() * 256;
        double zo = random.nextDouble() * 256;
        int[] p = new int[256];

        for (int i = 0; i < 256; i += 1) {
            p[i] = i;
        }
        for (int i = 0; i < 256; i += 1) {
            int j = random.nextInt(256 - i);
            int b = p[i];
            p[i] = p[i + j];
            p[i + j] = b;
        }
        return new SimplexNoise(p, xo, yo, zo);
    }

    public double sample2D(double d, double d2) {
        double d3;
        int n3;
        double d4;
        var d6 = (d + d2) * SimplexNoise.F2;
        int n4 = (int) Math.floor(d + d6);
        double d7 = n4 - (d3 = (n4 + (n3 = (int) Math.floor(d2 + d6))) * SimplexNoise.G2);
        double d8 = d - d7;
        int a;
        int b;
        if (d8 > (d4 = d2 - (n3 - d3))) {
            a = 1;
            b = 0;
        } else {
            a = 0;
            b = 1;
        }
        double d9 = d8 - a + SimplexNoise.G2;
        double d10 = d4 - b + SimplexNoise.G2;
        double d11 = d8 - 1.0 + 2.0 * SimplexNoise.G2;
        double d12 = d4 - 1.0 + 2.0 * SimplexNoise.G2;
        int n5 = n4 & 0xFF;
        int n6 = n3 & 0xFF;
        int n7 = this.P(n5 + this.P(n6)) % 12;
        int n8 = this.P(n5 + a + this.P(n6 + b)) % 12;
        int n9 = this.P(n5 + 1 + this.P(n6 + 1)) % 12;
        double d13 = this.getCornerNoise3D(n7, d8, d4, 0.0, 0.5);
        double d14 = this.getCornerNoise3D(n8, d9, d10, 0.0, 0.5);
        double d15 = this.getCornerNoise3D(n9, d11, d12, 0.0, 0.5);
        return 70.0 * (d13 + d14 + d15);
    }

    public double sample(double x, double y, double z) {
        var d5 = (x + y + z) * (1.0 / 3.0);
        int x2 = (int) Math.floor(x + d5);
        int y2 = (int) Math.floor(y + d5);
        int z2 = (int) Math.floor(z + d5);
        var d7 = (x2 + y2 + z2) * (1.0 / 6.0);
        var x3 = x - (x2 - d7);
        var y3 = y - (y2 - d7);
        var z3 = z - (z2 - d7);
        int a, b, c, d, e, f;
        if (x3 >= y3) {
            if (y3 >= z3) {
                a = 1;
                b = 0;
                c = 0;
                d = 1;
                e = 1;
                f = 0;
            } else if (x3 >= z3) {
                a = 1;
                b = 0;
                c = 0;
                d = 1;
                e = 0;
                f = 1;
            } else {
                a = 0;
                b = 0;
                c = 1;
                d = 1;
                e = 0;
                f = 1;
            }
        } else if (y3 < z3) {
            a = 0;
            b = 0;
            c = 1;
            d = 0;
            e = 1;
            f = 1;
        } else if (x3 < z3) {
            a = 0;
            b = 1;
            c = 0;
            d = 0;
            e = 1;
            f = 1;
        } else {
            a = 0;
            b = 1;
            c = 0;
            d = 1;
            e = 1;
            f = 0;
        }
        double x4 = x3 - a + (1.0 / 6.0);
        double y4 = y3 - b + (1.0 / 6.0);
        double z4 = z3 - c + (1.0 / 6.0);
        double x5 = x3 - d + (1.0 / 3.0);
        double y5 = y3 - e + (1.0 / 3.0);
        double z5 = z3 - f + (1.0 / 3.0);
        double x6 = x3 - 0.5;
        double y6 = y3 - 0.5;
        double z6 = z3 - 0.5;
        int x7 = x2 & 0xFF;
        int y7 = y2 & 0xFF;
        int z7 = z2 & 0xFF;
        var g = this.P(x7 + this.P(y7 + this.P(z7))) % 12;
        var h = this.P(x7 + a + this.P(y7 + b + this.P(z7 + c))) % 12;
        var i = this.P(x7 + d + this.P(y7 + e + this.P(z7 + f))) % 12;
        var j = this.P(x7 + 1 + this.P(y7 + 1 + this.P(z7 + 1))) % 12;
        var k = this.getCornerNoise3D(g, x3, y3, z3, 0.6);
        var l = this.getCornerNoise3D(h, x4, y4, z4, 0.6);
        var m = this.getCornerNoise3D(i, x5, y5, z5, 0.6);
        var n = this.getCornerNoise3D(j, x6, y6, z6, 0.6);
        return 32.0 * (k + l + m + n);
    }

    private int P(int i) {
        return this.p[i & 0xFF];
    }

    private double getCornerNoise3D(int i, double a, double b, double c, double d) {
        double f;
        var e = d - a * a - b * b - c * c;
        if (e < 0.0) {
            f = 0.0;
        } else {
            e *= e;
            f = e * e * SimplexNoise.gradDot(i, a, b, c);
        }
        return f;
    }

    public static double gradDot(int a, double b, double c, double d) {
        var grad = SimplexNoise.GRADIENT[a & 15];
        return grad[0] * b + grad[1] * c + grad[2] * d;
    }
}
