package net.minestom.vanilla.generation;

import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.generation.biome.Climate;
import net.minestom.vanilla.generation.densityfunctions.DensityFunction;
import net.minestom.vanilla.generation.densityfunctions.DensityFunctions;
import net.minestom.vanilla.generation.noise.*;
import net.minestom.vanilla.generation.random.LegacyRandom;
import net.minestom.vanilla.generation.random.WorldGenRandom;
import net.minestom.vanilla.generation.random.XoroshiroRandom;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class RandomState {
    private final Map<String, NormalNoise> noiseCache;
    private final Map<String, WorldGenRandom.Positional> randomCache;

    public final WorldGenRandom.Positional random;
    public final WorldGenRandom.Positional aquiferRandom;
    public final WorldGenRandom.Positional oreRandom;
    public final SurfaceSystem surfaceSystem;
    public final NoiseRouter router;
    public final Climate.Sampler sampler;

    public final long seed;

    public RandomState(NoiseGeneratorSettings settings, long seed) {
        this.seed = seed;
        this.noiseCache = new HashMap<>();
        this.randomCache = new HashMap<>();

        this.random = (settings.legacyRandomSource() ? new LegacyRandom(seed) : XoroshiroRandom.create(seed)).forkPositional();
        this.aquiferRandom = this.random.fromHashOf(NamespaceID.from("aquifer").toString()).forkPositional();
        this.oreRandom = this.random.fromHashOf(NamespaceID.from("ore").toString()).forkPositional();
        this.surfaceSystem = new SurfaceSystem(settings.surfaceRule(), settings.defaultBlock(), seed);
        this.router = NoiseRouter.mapAll(settings.noiseRouter(), this.createVisitor(settings.noise(), settings.legacyRandomSource()));
        this.sampler = Climate.Sampler.fromRouter(this.router);
    }

    public DensityFunctions.Visitor createVisitor(NoiseSettings noiseSettings, boolean legacyRandom) {
        Map<String, DensityFunction> mapped = new HashMap<>();

        Function<Holder<NormalNoise.NoiseParameters>, NormalNoise> getNoise = (noise) -> {
            NamespaceID key = noise.key();
            if (key == null) {
                throw new Error("Cannot create noise without key");
            }
            if (legacyRandom) {
                if (key.equals(NamespaceID.from("temperature"))) {
                    return new NormalNoise(new LegacyRandom(this.seed), NormalNoise.NoiseParameters.create(-7, new double[]{1, 1}));
                }
                if (key.equals(NamespaceID.from("vegetation"))) {
                    return new NormalNoise(new LegacyRandom(this.seed + 1), NormalNoise.NoiseParameters.create(-7, new double[]{1, 1}));
                }
                if (key.equals(NamespaceID.from("offset"))) {
                    return new NormalNoise(this.random.fromHashOf("offset"), NormalNoise.NoiseParameters.create(0, new double[]{0}));
                }
            }
            return this.getOrCreateNoise(key);
        };

        return new DensityFunctions.Visitor() {
            @Override
            public Function<DensityFunction, DensityFunction> map() {
                return fn1 -> {
                    if (fn1 instanceof DensityFunction.HolderHolder holderHolder) {
                        NamespaceID key = holderHolder.holder().key();
                        if (key != null && mapped.containsKey(key.toString())) {
                            return Objects.requireNonNull(mapped.get(key.toString()));
                        } else {
                            DensityFunction value = holderHolder.holder().value().mapAll(this);
                            if (key != null) {
                                mapped.put(key.toString(), value);
                            }
                            return value;
                        }
                    }

//                        if (fn1 instanceof DensityFunction.Interpolated) {
//                            return fn1.withCellSize(NoiseSettings.cellWidth(noiseSettings), NoiseSettings.cellHeight(noiseSettings))
//                        }
                    if (fn1 instanceof DensityFunction.Interpolated interpolated) {
                        return interpolated.withCellSize(NoiseSettings.cellWidth(noiseSettings), NoiseSettings.cellHeight(noiseSettings));
                    }

//                        if (fn1 instanceof DensityFunction.ShiftedNoise) {
//                            return new DensityFunction.ShiftedNoise(fn1.shiftX, fn1.shiftY, fn1.shiftZ, fn1.xzScale, fn1.yScale, fn1.noiseData, getNoise(fn1.noiseData))
//                        }
                    if (fn1 instanceof DensityFunction.ShiftedNoise shiftedNoise) {
                        return new DensityFunction.ShiftedNoise(shiftedNoise.shiftX(), shiftedNoise.shiftY(), shiftedNoise.shiftZ(), shiftedNoise.xzScale(), shiftedNoise.yScale(), shiftedNoise.noiseData(), getNoise.apply(shiftedNoise.noiseData()));
                    }

//                        if (fn1 instanceof DensityFunction.Noise) {
//                            return new DensityFunction.Noise(fn1.xzScale, fn1.yScale, fn1.noiseData, getNoise(fn1.noiseData))
//                        }
                    if (fn1 instanceof DensityFunction.Noise noise) {
                        return new DensityFunction.Noise(noise.xzScale(), noise.yScale(), noise.noiseData(), getNoise.apply(noise.noiseData()));
                    }

//                        if (fn1 instanceof DensityFunction.ShiftNoise) {
//                            return fn1.withNewNoise(getNoise(fn1.noiseData))
//                        }
                    if (fn1 instanceof DensityFunction.ShiftNoise shiftNoise) {
                        return shiftNoise.withNewNoise(getNoise.apply(shiftNoise.noiseData()));
                    }

//                        if (fn1 instanceof DensityFunction.WeirdScaledSampler) {
//                            return new DensityFunction.WeirdScaledSampler(fn1.input, fn1.rarityValueMapper, fn1.noiseData, getNoise(fn1.noiseData))
//                        }
                    if (fn1 instanceof DensityFunction.WeirdScaledSampler weirdScaledSampler) {
                        return new DensityFunction.WeirdScaledSampler(weirdScaledSampler.input(), weirdScaledSampler.rarityValueMapper(), weirdScaledSampler.noiseData(), getNoise.apply(weirdScaledSampler.noiseData()));
                    }

//                        if (fn1 instanceof DensityFunction.OldBlendedNoise) {
//                            return new DensityFunction.OldBlendedNoise(fn1.xzScale, fn1.yScale, fn1.xzFactor, fn1.yFactor, fn1.smearScaleMultiplier, new BlendedNoise( this.random.fromHashOf(Identifier.create("terrain").toString()), fn1.xzScale, fn1.yScale, fn1.xzFactor, fn1.yFactor, fn1.smearScaleMultiplier))
//                        }
                    if (fn1 instanceof DensityFunction.OldBlendedNoise oldBlendedNoise) {
                        return new DensityFunction.OldBlendedNoise(oldBlendedNoise.xzScale(),
                                oldBlendedNoise.yScale(), oldBlendedNoise.xzFactor(), oldBlendedNoise.yFactor(),
                                oldBlendedNoise.smearScaleMultiplier(),
                                new BlendedNoise(random.fromHashOf(NamespaceID.from("terrain").toString()),
                                        oldBlendedNoise.xzScale(), oldBlendedNoise.yScale(), oldBlendedNoise.xzFactor(),
                                        oldBlendedNoise.yFactor(), oldBlendedNoise.smearScaleMultiplier()));
                    }

//                        if (fn1 instanceof DensityFunction.EndIslands) {
//                            return new DensityFunction.EndIslands(this.seed)
//                        }
                    if (fn1 instanceof DensityFunction.EndIslands endIslands) {
                        return new DensityFunction.EndIslands(seed);
                    }

//                        if (fn1 instanceof DensityFunction.Mapped) {
//                            return fn1.withMinMax()
//                        }
                    if (fn1 instanceof DensityFunction.Mapped mapped) {
                        return mapped.withMinMax();
                    }

//                        if (fn1 instanceof DensityFunction.Ap2) {
//                            return fn1.withMinMax()
//                        }
                    if (fn1 instanceof DensityFunction.Ap2 ap2) {
                        return ap2.withMinMax();
                    }
                    return fn1;
                };
            }
        };
    }

    //    public getOrCreateNoise(id: Identifier) {
//        const noises = Registry.REGISTRY.getOrThrow(Identifier.create("worldgen/noise")) as Registry<NoiseParameters>
//            return computeIfAbsent(this.noiseCache, id.toString(), key =>
//                    new NormalNoise(this.random.fromHashOf(key), noises.getOrThrow(id))
//        )
//    }
    public NormalNoise getOrCreateNoise(NamespaceID id) {
        //noinspection unchecked
        Registry<NormalNoise.NoiseParameters> noises = (Registry<NormalNoise.NoiseParameters>)
                Registry.REGISTRY.getOrThrow(NamespaceID.from("worldgen/noise"));
        return noiseCache.computeIfAbsent(id.toString(),
                key -> new NormalNoise(random.fromHashOf(key), noises.getOrThrow(id)));
    }

    //    public getOrCreateRandom(id:Identifier) {
//        return computeIfAbsent(this.randomCache, id.toString(), key =>
//            this.random.fromHashOf(key).forkPositional()
//        )
//    }
    public WorldGenRandom.Positional getOrCreateRandom(NamespaceID id) {
        return randomCache.computeIfAbsent(id.toString(),
                key -> random.fromHashOf(key).forkPositional());
    }
}
