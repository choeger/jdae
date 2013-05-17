/**
 * 
 */
package de.tuberlin.uebb.jdae.solvers;

import java.util.Arrays;

import org.apache.commons.math3.analysis.differentiation.JacobianFunction;
import org.apache.commons.math3.analysis.differentiation.MultivariateDifferentiableVectorFunction;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.SimpleVectorValueChecker;
import org.apache.commons.math3.optim.nonlinear.vector.ModelFunction;
import org.apache.commons.math3.optim.nonlinear.vector.ModelFunctionJacobian;
import org.apache.commons.math3.optim.nonlinear.vector.Target;
import org.apache.commons.math3.optim.nonlinear.vector.Weight;
import org.apache.commons.math3.optim.nonlinear.vector.jacobian.LevenbergMarquardtOptimizer;

/**
 * @author choeger
 * 
 */
public class OptimalitySolver implements
        MultivariateFunctionSolver<MultivariateDifferentiableVectorFunction> {

    final LevenbergMarquardtOptimizer optim = new LevenbergMarquardtOptimizer(
            new SimpleVectorValueChecker(1e-8, 1e-8));

    @Override
    public double[] solve(int maxEval,
            MultivariateDifferentiableVectorFunction f, double[] startValue) {
        final double[] zeros = startValue.clone();
        final double[] ones = startValue.clone();
        Arrays.fill(zeros, 0.0);
        Arrays.fill(ones, 1.0);

        return optim.optimize(new MaxEval(maxEval),
                new InitialGuess(startValue), new Target(zeros),
                new Weight(ones), new ModelFunction(f),
                new ModelFunctionJacobian(new JacobianFunction(f))).getPoint();
    }

}
