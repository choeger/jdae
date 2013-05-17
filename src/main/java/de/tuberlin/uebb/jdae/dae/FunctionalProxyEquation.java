package de.tuberlin.uebb.jdae.dae;

public final class FunctionalProxyEquation extends FunctionalEquation {
    final int unknown;
    final int index;
    final FunctionalEquation src;

    public FunctionalProxyEquation(int unknown, int index,
            FunctionalEquation src) {
        super();
        this.unknown = unknown;
        this.index = index;
        this.src = src;
    }

    @Override
    public int unknown() {
        return unknown;
    }

    @Override
    public double compute(double time) {
        return src.value(time, index);
    }

}
