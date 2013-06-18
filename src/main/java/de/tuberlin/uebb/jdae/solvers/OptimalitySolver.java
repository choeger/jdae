/*
 * Copyright (C) 2012 uebb.tu-berlin.de.
 *
 * This file is part of modim
 *
 * modim is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * modim is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with modim. If not, see <http://www.gnu.org/licenses/>.
 */

package de.tuberlin.uebb.jdae.solvers;

import java.util.Arrays;

import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import org.apache.commons.math3.analysis.differentiation.JacobianFunction;
import org.apache.commons.math3.analysis.differentiation.MultivariateDifferentiableVectorFunction;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.SimpleVectorValueChecker;
import org.apache.commons.math3.optim.nonlinear.vector.ModelFunction;
import org.apache.commons.math3.optim.nonlinear.vector.ModelFunctionJacobian;
import org.apache.commons.math3.optim.nonlinear.vector.Target;
import org.apache.commons.math3.optim.nonlinear.vector.Weight;
import org.apache.commons.math3.optim.nonlinear.vector.jacobian.GaussNewtonOptimizer;

/**
 * @author choeger
 * 
 */
public class OptimalitySolver implements
        MultivariateFunctionSolver<MultivariateDifferentiableVectorFunction> {

    final GaussNewtonOptimizer optim = new GaussNewtonOptimizer(
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

    public double[] solve(int maxEval, MultivariateVectorFunction residual,
            MultivariateMatrixFunction jacobian, double[] startValue) {
        final double[] zeros = startValue.clone();
        final double[] ones = startValue.clone();
        Arrays.fill(zeros, 0.0);
        Arrays.fill(ones, 1.0);

        return optim.optimize(new MaxEval(maxEval),
                new InitialGuess(startValue), new Target(zeros),
                new Weight(ones), new ModelFunction(residual),
                new ModelFunctionJacobian(jacobian)).getPoint();
    }

}
