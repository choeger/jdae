/**
 * 
 */
package de.tuberlin.uebb.jdae.builtins;

import org.apache.commons.math3.analysis.differentiation.DerivativeStructure;

import de.tuberlin.uebb.jdae.dae.ADEquation;

/**
 * @author choeger
 * 
 */
public class ADLinearFunctionalEquation extends ADEquation {

    public final int index;
    public final double timeCoefficient;
    public final ADEquation[] variables;
    public final double[] coefficients;
    public final double constant;

    public ADLinearFunctionalEquation(final int order, final int target,
            final ADEquation[] unknowns, final double[] coefficients,
            final double timeCoefficient, final double constant) {
        super(order);
        this.index = target;
        this.timeCoefficient = timeCoefficient;
        this.variables = unknowns;
        this.coefficients = coefficients;
        this.constant = constant;
    }

    /*
     * (nicht-Javadoc)
     * 
     * @see
     * de.tuberlin.uebb.jdae.dae.ADEquation#compute(org.apache.commons.math3
     * .analysis.differentiation.DerivativeStructure)
     */
    @Override
    public DerivativeStructure compute(DerivativeStructure time) {
        DerivativeStructure val = time.multiply(this.timeCoefficient);

        double c = 0.0;

        for (int i = 0; i < variables.length; i++) {
            final ADEquation f = variables[i];
            if (f != null) // null in case i == unknown() !
                val = val.add(f.value(time).multiply(coefficients[i]));
            else
                c = coefficients[i];
        }

        return val.negate().add(constant).divide(c);
    }

    /*
     * (nicht-Javadoc)
     * 
     * @see de.tuberlin.uebb.jdae.dae.FunctionalEquation#unknown()
     */
    @Override
    public int unknown() {
        return index;
    }

}
