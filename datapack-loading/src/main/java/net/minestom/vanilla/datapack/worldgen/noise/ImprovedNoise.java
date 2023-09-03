package net.minestom.vanilla.datapack.worldgen.noise;

import net.minestom.vanilla.datapack.worldgen.util.Util;
import net.minestom.vanilla.datapack.worldgen.random.WorldgenRandom;

public class ImprovedNoise implements Noise {

    public final int[] p;
    public final double xo;
    public final double yo;
    public final double zo;

    public ImprovedNoise(WorldgenRandom random) {
        this.xo = random.nextDouble() * 256;
        this.yo = random.nextDouble() * 256;
        this.zo = random.nextDouble() * 256;
        this.p = new int[256];

        for (int i = 0; i < 256; i += 1) {
            this.p[i] = i > 127 ? i - 256 : i;
        }
        for (int i = 0; i < 256; i += 1) {
            int j = random.nextInt(256 - i);
            int b = this.p[i];
            this.p[i] = this.p[i + j];
            this.p[i + j] = b;
        }
    }

    public double sample(double x, double y, double z) {
        return this.sample(x, y, z, 0, 0);
    }

    @Override
    public double minValue() {
        return -1;
    }

    @Override
    public double maxValue() {
        return 1;
    }

    public double sample(double x, double y, double z, double yScale, double yLimit) {
        double x2 = x + this.xo;
        double y2 = y + this.yo;
        double z2 = z + this.zo;
        int x3 = (int) Math.floor(x2);
        int y3 = (int) Math.floor(y2);
        int z3 = (int) Math.floor(z2);
        double x4 = x2 - x3;
        double y4 = y2 - y3;
        double z4 = z2 - z3;

        double y6 = 0;
        if (yScale != 0) {
            double t = yLimit >= 0 && yLimit < y4 ? yLimit : y4;
            y6 = Math.floor(t / yScale + 1e-7) * yScale;
        }

        return this.sampleAndLerp(x3, y3, z3, x4, y4 - y6, z4, y4);
    }

    private double sampleAndLerp(int a, int b, int c, double d, double e, double f, double g) {
        int h = this.P(a);
        int i = this.P(a + 1);
        int j = this.P(h + b);
        int k = this.P(h + b + 1);
        int l = this.P(i + b);
        int m = this.P(i + b + 1);

        // import { lerp3, smoothstep } from '../Util.js'

        double n = gradDot(this.P(j + c), d, e, f);
        double o = gradDot(this.P(l + c), d - 1.0, e, f);
        double p = gradDot(this.P(k + c), d, e - 1.0, f);
        double q = gradDot(this.P(m + c), d - 1.0, e - 1.0, f);
        double r = gradDot(this.P(j + c + 1), d, e, f - 1.0);
        double s = gradDot(this.P(l + c + 1), d - 1.0, e, f - 1.0);
        double t = gradDot(this.P(k + c + 1), d, e - 1.0, f - 1.0);
        double u = gradDot(this.P(m + c + 1), d - 1.0, e - 1.0, f - 1.0);

        double v = Util.smoothstep(d);
        double w = Util.smoothstep(g);
        double x = Util.smoothstep(f);

        return Util.lerp3(v, w, x, n, o, p, q, r, s, t, u);
    }

    public static double gradDot(int a, double b, double c, double d) {
        var grad = SimplexNoise.GRADIENT[a & 15];
        return grad[0] * b + grad[1] * c + grad[2] * d;
    }

    private int P(int i) {
        return this.p[i & 0xFF] & 0xFF;
    }
}
