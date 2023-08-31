package net.minestom.vanilla.datapack.worldgen.noise;

import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

class FutureNoise implements Noise {

    private final Supplier<Noise> getNoise;
    private @Nullable Noise noise = null;

    public FutureNoise(Supplier<Noise> getNoise) {
        this.getNoise = getNoise;
    }

    @Override
    public double sample(double x, double y, double z) {
        if (noise == null) {
            noise = getNoise.get();
        }
        return noise.sample(x, y, z);
    }

    @Override
    public double minValue() {
        if (noise == null) {
            noise = getNoise.get();
        }
        return noise.minValue();
    }

    @Override
    public double maxValue() {
        if (noise == null) {
            noise = getNoise.get();
        }
        return noise.maxValue();
    }
}
