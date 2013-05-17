/**
 * 
 */
package de.tuberlin.uebb.jdae.examples;

import org.apache.commons.math3.analysis.differentiation.DerivativeStructure;
import org.apache.commons.math3.analysis.differentiation.MultivariateDifferentiableVectorFunction;
import org.apache.commons.math3.exception.MathIllegalArgumentException;

/**
 * @author choeger x² + y² = 1
 * 
 */
public class SquareRootEquation implements
        MultivariateDifferentiableVectorFunction {
    final double y;

    public SquareRootEquation(double y) {
        super();
        this.y = y;
    }

    @Override
    public double[] value(double[] point) throws IllegalArgumentException {
        System.out.println("Value at " + point[0] + " = "
                + (point[0] * point[0] + y * y - 1));
        return new double[] { point[0] * point[0] + y * y - 1 };
    }

    @Override
    public DerivativeStructure[] value(DerivativeStructure[] point)
            throws MathIllegalArgumentException {
        return new DerivativeStructure[] { point[0].multiply(point[0])
                .add(y * y).subtract(1) };
    }

}
