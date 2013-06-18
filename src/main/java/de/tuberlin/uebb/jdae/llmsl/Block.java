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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import org.apache.commons.math3.analysis.differentiation.DerivativeStructure;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import de.tuberlin.uebb.jdae.solvers.OptimalitySolver;
import de.tuberlin.uebb.jdae.transformation.DerivedEquation;

/**
 * @author choeger
 * 
 */
public class Block implements MultivariateVectorFunction, IBlock {

    public static final class Residual {
        public final BlockEquation eq;
        public final int derOrder;

        public Residual(BlockEquation eq, int derOrder) {
            super();
            this.eq = eq;
            this.derOrder = derOrder;
        }
    }

    public final BlockVariable[] variables;
    public final Residual[] equations;
    public final ExecutionContext[] views;

    /* memoization */
    private final DerivativeStructure[] residuals;
    private double[] lastPoint = null;
    private final Set<GlobalVariable> iteratees;

    /* equation solver */
    final OptimalitySolver solver = new OptimalitySolver();
    final ExecutionContext view;

    public Block(double[][] data, DataLayout layout,
            Set<GlobalVariable> variables, Set<DerivedEquation> equations) {
        this.iteratees = variables;

        final List<GlobalVariable> iteratees = ImmutableList.copyOf(variables);
        this.view = new ExecutionContext(1, iteratees.size() + 1, data);

        final Map<GlobalVariable, Integer> gvIndex = Maps.newHashMap();
        this.variables = new BlockVariable[iteratees.size()];

        for (int i = 0; i < iteratees.size(); i++)
            gvIndex.put(iteratees.get(i), i + 1);

        final Map<GlobalVariable, BlockVariable> blockVars = makeBlockVars(
                layout, equations, gvIndex);
        for (int i = 0; i < iteratees.size(); i++) {
            this.variables[i] = blockVars.get(iteratees.get(i));
        }

        this.views = new ExecutionContext[equations.size()];
        this.equations = new Residual[equations.size()];
        this.residuals = new DerivativeStructure[equations.size()];

        int index = 0;
        for (DerivedEquation e : equations) {
            this.equations[index] = new Residual(e.eqn.bind(blockVars),
                    e.derOrder);
            views[index++] = view.derived(e.derOrder);
        }

    }

    private Map<GlobalVariable, BlockVariable> makeBlockVars(DataLayout layout,
            Set<DerivedEquation> equations,
            final Map<GlobalVariable, Integer> gvIndex) {
        final Map<GlobalVariable, BlockVariable> blockVars = Maps.newHashMap();

        final Set<GlobalVariable> needed = Sets.newHashSet();
        for (DerivedEquation eq : equations) {
            final int order = eq.derOrder;
            for (int i = 0; i <= order; i++)
                for (GlobalVariable gv : eq.eqn.need())
                    needed.add(gv.der(i));
        }
        for (GlobalVariable gv : needed) {
            final GlobalVariable[] derivatives = gv.derivatives(layout);
            final int[] relativeDerivatives = new int[derivatives.length];
            for (int i = 0; i < derivatives.length; i++)
                relativeDerivatives[i] = gvIndex.containsKey(derivatives[i]) ? gvIndex
                        .get(derivatives[i]) : -1;
            final int relIndex = gvIndex.containsKey(gv) ? gvIndex.get(gv) : -1;
            final BlockVariable bv = new BlockVariable(gv.index, gv.der,
                    relIndex, relativeDerivatives);
            blockVars.put(gv, bv);
        }

        return blockVars;
    }

    /* Jacobian */
    private final MultivariateMatrixFunction jacobian = new MultivariateMatrixFunction() {

        @Override
        public double[][] value(double[] point) throws IllegalArgumentException {
            compute(point);

            double[][] ret = new double[variables.length + 1][variables.length + 1];
            ret[0][0] = 1.0;

            int index = 1;
            for (int i = 0; i < equations.length; i++)
                for (int di = 0; di <= equations[i].derOrder; di++) {
                    for (int j = 1; j <= variables.length; j++) {
                        ret[index][j] = getPartialDerivative(residuals[i], di,
                                j);
                    }
                    index++;
                }
            /*
             * System.out.println("==== Jacobian:"); for (double[] r : ret)
             * System.out.println(Arrays.toString(r));
             */

            return ret;
        }
    };

    public MultivariateMatrixFunction jacobian() {
        return jacobian;
    }

    private double getPartialDerivative(DerivativeStructure ds, int dt,
            int index) {
        final int[] orders = new int[variables.length + 1];
        orders[0] = dt;
        orders[index] = 1;
        return ds.getPartialDerivative(orders);
    }

    @Override
    public double[] value(double[] point) {
        compute(point);

        double[] ret = point.clone();

        ret[0] = 0.0;

        int index = 1;
        for (int i = 0; i < residuals.length; i++) {
            final DerivativeStructure r = residuals[i];
            final Residual eq = equations[i];
            ret[index++] = r.getValue();
            for (int di = 1; di <= eq.derOrder; di++) {
                final int[] orders = views[i].dtOrders[di];
                final double partialDerivative = r.getPartialDerivative(orders);
                ret[index++] = partialDerivative;
            }
        }

        return ret;
    }

    private void compute(double[] point) {
        if (!Arrays.equals(lastPoint, point)) {
            lastPoint = point.clone();
            views[0].set(variables, point);

            for (int i = 0; i < equations.length; i++) {
                residuals[i] = equations[i].eq.exec(views[i]);
            }
        }
    }

    public void exec() {
        double[] start = views[0].loadD(variables);

        final double[] point = solver.solve(1000, this, jacobian, start);

        views[0].set(variables, point);
    }

    @Override
    public Iterable<GlobalVariable> variables() {
        return iteratees;
    }
}
