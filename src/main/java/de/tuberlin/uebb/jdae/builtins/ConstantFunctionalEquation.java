package de.tuberlin.uebb.jdae.builtins;

import de.tuberlin.uebb.jdae.dae.FunctionalEquation;

public final class ConstantFunctionalEquation extends FunctionalEquation {

    private final double c;
    private final int u;

    @Override
    public double compute(double x) {
        return c;
    }

    public ConstantFunctionalEquation(int u, double c) {
        super();
        this.c = c;
        this.u = u;
    }

    @Override
    public int unknown() {
        return u;
    }

}
