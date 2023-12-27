package net.minestom.vanilla.datapack.worldgen.noise;

import net.minestom.vanilla.datapack.DatapackLoader;
import net.minestom.vanilla.datapack.DatapackUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

class LazyLoadedNoise implements Noise {
    private @Nullable Noise noise = null;

    public LazyLoadedNoise(String id, DatapackLoader.LoadingContext context) {
        context.whenFinished(finish -> noise = DatapackUtils.findNoise(finish.datapack(), id).orElseThrow());
    }

    private @NotNull Noise noise() {
        if (noise == null) {
            throw new IllegalStateException("Noise not loaded yet");
        }
        return noise;
    }

    @Override
    public double sample(double x, double y, double z) {
        return noise().sample(x, y, z);
    }

    @Override
    public double minValue() {
        return noise().minValue();
    }

    @Override
    public double maxValue() {
        return noise().maxValue();
    }
}
