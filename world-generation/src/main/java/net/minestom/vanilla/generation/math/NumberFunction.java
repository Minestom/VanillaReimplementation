package net.minestom.vanilla.generation.math;

public interface NumberFunction<C> {
    double compute(C c);

    interface Mapper<C> {
        NumberFunction<C> map(NumberFunction<C> function);
    }
}
