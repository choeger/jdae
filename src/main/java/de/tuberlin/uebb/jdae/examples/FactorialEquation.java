/**
 * 
 */
package de.tuberlin.uebb.jdae.examples;

import org.apache.commons.math3.analysis.differentiation.DerivativeStructure;
import org.apache.commons.math3.analysis.differentiation.MultivariateDifferentiableVectorFunction;
import org.apache.commons.math3.exception.MathIllegalArgumentException;

/**
 * @author choeger
 * 
 */
public class FactorialEquation implements
        MultivariateDifferentiableVectorFunction {

    private final double y;

    public FactorialEquation(double d) {
        y = d;
    }

    private final double factorial(final double x) {
        if (x <= 1.0)
            return 1.0;
        else
            return x * factorial(x - 1);
    }

    private final DerivativeStructure dfactorial(final DerivativeStructure ds) {
        final DerivativeStructure one = new DerivativeStructure(
                ds.getFreeParameters(), 1, 1.0);
        if (ds.getValue() <= 1.0) {
            return one;
        } else
            return ds.multiply(dfactorial(ds.subtract(one)));
    }

    @Override
    public double[] value(double[] point) throws IllegalArgumentException {
        return new double[] { factorial(point[0]) - y };
    }

    @Override
    public DerivativeStructure[] value(DerivativeStructure[] point)
            throws MathIllegalArgumentException {
        return new DerivativeStructure[] { dfactorial(point[0]).subtract(
                new DerivativeStructure(point[0].getFreeParameters(), 1, y)) };
    }

}
