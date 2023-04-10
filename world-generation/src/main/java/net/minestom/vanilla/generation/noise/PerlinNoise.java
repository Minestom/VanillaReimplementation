package net.minestom.vanilla.generation.noise;

import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minestom.vanilla.generation.random.WorldgenRandom;
import net.minestom.vanilla.generation.random.XoroshiroRandom;

public class PerlinNoise implements Noise {
    public final ImprovedNoise[] noiseLevels;
    public final double[] amplitudes;
    public final double lowestFreqInputFactor;
    public final double lowestFreqValueFactor;
    public final double maxValue;

    public PerlinNoise(WorldgenRandom random, double firstOctave, double[] amplitudes) {
        this(random, firstOctave, DoubleList.of(amplitudes));
    }

    public PerlinNoise(WorldgenRandom random, double firstOctave, DoubleList amplitudes) {
        this.amplitudes = amplitudes.toDoubleArray();
        this.noiseLevels = new ImprovedNoise[amplitudes.size()];

        if (random instanceof XoroshiroRandom) {
            WorldgenRandom.Positional forkedRandom = random.forkPositional();

            for (int i = 0; i < amplitudes.size(); i++) {
                if (amplitudes.getDouble(i) != 0.0) {
                    double octave = firstOctave + i;
                    this.noiseLevels[i] = new ImprovedNoise(forkedRandom.fromHashOf("octave_" + octave));
                }
            }
        } else {
            if (1 - firstOctave < amplitudes.size()) {
                throw new RuntimeException("Positive octaves are not allowed when using LegacyRandom");
            }


            for (int i = (int) -firstOctave; i >= 0; i -= 1) {
                if (i < amplitudes.size() && amplitudes.getDouble(i) != 0) {
                    this.noiseLevels[i] = new ImprovedNoise(random);
                } else {
                    random.consume(262);
                }
            }
        }
        this.lowestFreqInputFactor = Math.pow(2, firstOctave);
        this.lowestFreqValueFactor = Math.pow(2, (amplitudes.size() - 1)) / (Math.pow(2, amplitudes.size()) - 1);
        this.maxValue = this.edgeValue(2);
    }

    public double sample(double x, double y, double z) {
        return sample(x, y, z, 0, 0, false);
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
                value += this.amplitudes[i] * x * valueF;
            }
            valueF /= 2;
        }
        return value;
    }

    public static double wrap(double value) {
        return value - Math.floor(value / 3.3554432E7 + 0.5) * 3.3554432E7;
    }
}