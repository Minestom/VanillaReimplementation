package net.minestom.vanilla.datapack.worldgen.noise;

import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minestom.vanilla.datapack.worldgen.random.WorldgenRandom;
import net.minestom.vanilla.datapack.worldgen.random.XoroshiroRandom;

import java.util.Arrays;
import java.util.Objects;

public class PerlinNoise implements Noise {
    public final ImprovedNoise[] noiseLevels;
    public final double[] amplitudes;
    public final double lowestFreqInputFactor;
    public final double lowestFreqValueFactor;
    public final double maxValue;

    public PerlinNoise(WorldgenRandom random, int firstOctave, double[] amplitudes) {
        this(random, firstOctave, DoubleList.of(amplitudes));
    }

    public PerlinNoise(WorldgenRandom random, int firstOctave, DoubleList amplitudes) {
        this.amplitudes = amplitudes.toDoubleArray();
        this.noiseLevels = new ImprovedNoise[this.amplitudes.length];

        WorldgenRandom.Positional forkedRandom = random.forkPositional();

        for (int i = 0; i < this.amplitudes.length; ++i) {
            if (this.amplitudes[i] != 0.0) {
                this.noiseLevels[i] = new ImprovedNoise(forkedRandom.fromHashOf("octave_" + (firstOctave + i)));
            }
        }

        this.lowestFreqInputFactor = Math.pow(2.0, firstOctave);
        this.lowestFreqValueFactor = Math.pow(2.0, this.amplitudes.length - 1) / (Math.pow(2.0, this.amplitudes.length) - 1.0);
        this.maxValue = this.edgeValue(2.0);
    }

    public double sample(double x, double y, double z) {
        return sample(x, y, z, 0, 0, false);
    }

    @Override
    public double minValue() {
        return 0;
    }

    @Override
    public double maxValue() {
        return this.maxValue;
    }

    public double sample(double x, double y, double z, double yScale, double yLimit, boolean fixY) {
        var value = 0.0;
        double inputF = this.lowestFreqInputFactor;
        double valueF = this.lowestFreqValueFactor;

        for (var i = 0; i < this.noiseLevels.length; i += 1) {
            ImprovedNoise noise = this.noiseLevels[i];
            if (noise != null) {
                value += this.amplitudes[i] * valueF * noise.sample(
                        PerlinNoise.wrap(x * inputF),
                        fixY ? -noise.yo : PerlinNoise.wrap(y * inputF),
                        PerlinNoise.wrap(z * inputF),
                        yScale * inputF,
                        yLimit * inputF);
            }
            inputF *= 2;
            valueF /= 2;
        }

        return value;
    }

    public ImprovedNoise getOctaveNoise(int i) {
        return this.noiseLevels[this.noiseLevels.length - 1 - i];
    }

    public double edgeValue(double x) {
        var value = 0;
        var valueF = this.lowestFreqValueFactor;
        for (int i = 0; i < this.noiseLevels.length; i += 1) {
            if (this.noiseLevels[i] != null) {
                value += (int) (this.amplitudes[i] * x * valueF);
            }
            valueF /= 2;
        }
        return value;
    }

    public static double wrap(double value) {
        return value - Math.floor(value / 3.3554432E7 + 0.5) * 3.3554432E7;
    }
}
