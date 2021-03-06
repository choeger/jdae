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

package de.tuberlin.uebb.jdae.llmsl;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import org.apache.commons.math3.exception.ConvergenceException;
import org.ejml.data.DenseMatrix64F;
import org.ejml.factory.LinearSolver;
import org.ejml.factory.LinearSolverFactory;

import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;

import de.tuberlin.uebb.jdae.diff.total.TDNumber;
import de.tuberlin.uebb.jdae.simulation.SimulationOptions;
import de.tuberlin.uebb.jdae.transformation.DerivedEquation;

/**
 * @author choeger
 * 
 */
public final class Block implements MultivariateVectorFunction, IBlock {

    private static final double TOLERANCE = 1e-8;
    public static long evals;

    public static final class Residual {
        public final BlockEquation eq;
        public final int maxOrder;
        public final int minOrder;

        public Residual(BlockEquation eq, int minOrder, int maxOrder) {
            super();
            this.eq = eq;
            this.maxOrder = maxOrder;
            this.minOrder = minOrder;
        }
    }

    public final GlobalVariable[] variables;
    public final Residual[] equations;
    public final ExecutionContext[] views;

    /* memoization */
    private final TDNumber[] residuals;
    private double[] lastPoint = null;

    /* simulation flags */
    final SimulationOptions options;

    /* equation solver */
    final ExecutionContext view;

    public Block(double[][] data, DataLayout layout,
            Set<GlobalVariable> variables, Set<DerivedEquation> equations,
            SimulationOptions options) {

        this.options = options;
        this.variables = variables
                .toArray(new GlobalVariable[variables.size()]);
        Arrays.sort(this.variables, Ordering.natural());

        this.view = new ExecutionContext(0, this.variables, data);

        final Map<Integer, Integer> gvIndex = Maps.newTreeMap();

        for (int i = 0; i < this.variables.length; i++) {
            if (!gvIndex.containsKey(this.variables[i].index))
                gvIndex.put(this.variables[i].index, i);
        }
        final Map<GlobalVariable, BlockVariable> blockVars = makeBlockVars(
                layout, equations, gvIndex);

        this.views = new ExecutionContext[equations.size()];
        this.equations = new Residual[equations.size()];
        this.residuals = new TDNumber[equations.size()];

        int index = 0;
        for (DerivedEquation e : equations) {
            this.equations[index] = new Residual(e.eqn.bind(blockVars),
                    e.minOrder, e.maxOrder);
            views[index++] = view.derived(e.maxOrder);
        }

        jacobianMatrix = new DenseMatrix64F(this.variables.length,
                this.variables.length);
        residual = new DenseMatrix64F(this.variables.length, 1);
        x = new DenseMatrix64F(this.variables.length);
        solver = LinearSolverFactory.linear(this.variables.length);
        this.point = new double[this.variables.length];
    }

    private Map<GlobalVariable, BlockVariable> makeBlockVars(DataLayout layout,
            Set<DerivedEquation> equations, final Map<Integer, Integer> gvIndex) {
        final Map<GlobalVariable, BlockVariable> blockVars = Maps.newHashMap();

        final Set<GlobalVariable> needed = Sets.newTreeSet();
        for (DerivedEquation eq : equations) {
            final int order = eq.maxOrder;
            for (int i = 0; i <= order; i++)
                for (GlobalVariable gv : eq.eqn.need())
                    needed.add(gv.der(i));
        }
        for (GlobalVariable gv : needed) {
            if (gvIndex.containsKey(gv.index)) {
                final int relIndex = gvIndex.get(gv.index);
                final BlockVariable bv;
                if (variables[relIndex].der > gv.der) {
                    bv = new BlockState(gv, relIndex);
                } else {
                    final int derDiff = gv.der - variables[relIndex].der;
                    bv = new BlockIteratee(gv, relIndex + derDiff);
                }
                blockVars.put(gv, bv);
            } else {
                blockVars.put(gv, new BlockConstant(gv));
            }
        }

        return blockVars;
    }

    @Override
    public double[] value(double[] point) {
        compute(point);

        double[] ret = point.clone();

        writeResidual(ret);

        return ret;
    }

    private final void writeResidual(double[] ret) {
        int index = 0;
        for (int i = 0; i < residuals.length; i++) {
            final TDNumber r = residuals[i];
            final Residual eq = equations[i];
            for (int di = eq.minOrder; di <= eq.maxOrder; di++) {
                ret[index++] = r.der(di);
            }
        }
    }

    private final boolean writeNegResidual(double[] ret) {
        boolean converged = true;

        int index = 0;
        for (int i = 0; i < residuals.length; i++) {
            final TDNumber r = residuals[i];
            final Residual eq = equations[i];

            for (int di = eq.minOrder; di <= eq.maxOrder; di++) {
                final double dres = -r.der(di);
                converged &= dres <= TOLERANCE && dres >= -TOLERANCE;
                ret[index++] = dres;
            }
        }

        return converged;
    }

    private void compute(double point[]) {

        if (!Arrays.equals(point, this.point) || residuals[0] == null) {
            for (int i = 0; i < point.length; i++)
                this.point[i] = point[i];
            forceCompute();
        }
    }

    private void forceCompute() {
        final long s = System.currentTimeMillis();

        views[0].set(0, variables, point);

        for (int i = 0; i < equations.length; i++) {
            residuals[i] = equations[i].eq.exec(views[i]);
        }

        evals += (System.currentTimeMillis() - s);
    }

    private final void writeJacobian() {
        int index = 0;

        for (int i = 0; i < equations.length; i++)
            for (int di = equations[i].minOrder; di <= equations[i].maxOrder; di++) {
                for (int j = 0; j < variables.length; j++) {
                    jacobianMatrix.set(index, j, residuals[i].der(di, j));
                }
                index++;
            }
    }

    /* Jacobian */
    private final MultivariateMatrixFunction jacobian = new MultivariateMatrixFunction() {

        @Override
        public double[][] value(double[] point) throws IllegalArgumentException {
            compute(point);

            double[][] ret = new double[variables.length][variables.length];
            int index = 0;

            for (int i = 0; i < equations.length; i++)
                for (int di = 0; di <= equations[i].maxOrder; di++) {
                    for (int j = 0; j < variables.length; j++) {
                        ret[index][j] = residuals[i].der(di, j);
                    }
                    index++;
                }

            return ret;
        }
    };
    private final DenseMatrix64F jacobianMatrix;
    private final DenseMatrix64F residual;
    private final DenseMatrix64F x;
    private LinearSolver<DenseMatrix64F> solver;
    private final double[] point;

    public MultivariateMatrixFunction jacobian() {
        return jacobian;
    }

    public void exec() {

        views[0].loadD(point, variables);
        forceCompute();

        int steps = options.maxIterations;
        while (!writeNegResidual(residual.data)) {
            if (steps-- <= 0) {
                views[0].loadD(point, variables);
                forceCompute();
                writeJacobian();
                System.out.println("Convergence Error:");
                System.out.println(jacobianMatrix.toString());

                throw new ConvergenceException();
            }
            writeJacobian();
            if (!solver.setA(jacobianMatrix))
                throw new ConvergenceException();
            solver.solve(residual, x);

            for (int i = 0; i < point.length; ++i)
                point[i] += x.data[i];

            forceCompute();
        }

        views[0].set(0, variables, point);
    }

    @Override
    public Iterable<GlobalVariable> variables() {
        return new Iterable<GlobalVariable>() {
            public Iterator<GlobalVariable> iterator() {
                return Iterators.forArray(variables);
            }
        };
    }
}
