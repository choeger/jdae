package de.tuberlin.uebb.jdae.dae;

import org.apache.commons.math3.analysis.UnivariateFunction;

public abstract class FunctionalEquation implements UnivariateFunction {

    protected double lastTime = Double.NaN;
    protected double lastValue = Double.NaN;

    public abstract int unknown();

    public double value(double time) {

        if (lastTime != time) {
            setValue(time, compute(time));
        }

        return lastValue;
    }

    abstract public double compute(double time);

    public void setValue(double t, double v) {
        lastTime = t;
        lastValue = v;
    }

}
