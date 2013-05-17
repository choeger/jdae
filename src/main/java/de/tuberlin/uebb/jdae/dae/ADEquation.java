package de.tuberlin.uebb.jdae.dae;

import org.apache.commons.math3.analysis.differentiation.DerivativeStructure;
import org.apache.commons.math3.analysis.differentiation.UnivariateDifferentiableFunction;
import org.apache.commons.math3.exception.MathIllegalArgumentException;

public abstract class ADEquation extends FunctionalEquation implements
        UnivariateDifferentiableFunction {

    protected DerivativeStructure lastValue;
    protected final int order;

    public ADEquation(int order) {
        super();
        this.order = order;
        lastValue = new DerivativeStructure(1, order, 0, 0.0);
    }

    public abstract DerivativeStructure compute(DerivativeStructure time);

    @Override
    public double value(double time) {
        return value(time, 0);
    }

    public double value(double time, int derIndex) {
        if (derIndex <= order) {
            final DerivativeStructure ds = new DerivativeStructure(1, order, 0,
                    time);
            final DerivativeStructure v = value(ds);
            return v.getPartialDerivative(derIndex);
        }

        throw new RuntimeException("Unexpected derivative of order " + derIndex
                + " requested! Maximum: " + order);
    }

    @Override
    public void setValue(double t, double v) {
        super.setValue(t, v);
    }

    @Override
    public DerivativeStructure value(DerivativeStructure t)
            throws MathIllegalArgumentException {
        if (t.getValue() != lastTime) {
            lastTime = t.getValue();
            lastValue = compute(t);
        }
        return lastValue;
    }

    @Override
    public double compute(double time) {
        return 0;
    }

}
