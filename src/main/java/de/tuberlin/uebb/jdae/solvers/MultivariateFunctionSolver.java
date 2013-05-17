/**
 * 
 */
package de.tuberlin.uebb.jdae.solvers;

import org.apache.commons.math3.analysis.MultivariateVectorFunction;

/**
 * @author choeger
 * 
 */
public interface MultivariateFunctionSolver<FUNC extends MultivariateVectorFunction> {

    double[] solve(int maxEval, FUNC f, double[] startValue);

}
