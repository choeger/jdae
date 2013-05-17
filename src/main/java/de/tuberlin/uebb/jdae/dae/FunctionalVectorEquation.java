package de.tuberlin.uebb.jdae.dae;

import org.apache.commons.math3.analysis.differentiation.UnivariateDifferentiableVectorFunction;

public abstract class FunctionalVectorEquation implements
        UnivariateDifferentiableVectorFunction {

    protected double lastTime = Double.NaN;
    protected double[] lastValue = null;

    public abstract int[] unknown();

    public double[] value(double time) {

        if (lastTime != time) {
            setValue(time, compute(time));
        }

        return lastValue;
    }

    abstract public double[] compute(double time);

    public void setValue(double t, double[] v) {
        lastTime = t;
        lastValue = v;
    }
}
