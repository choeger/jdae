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
import org.apache.commons.math3.analysis.differentiation.DSCompiler;
import org.apache.commons.math3.analysis.differentiation.DerivativeStructure;

import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
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

    public final GlobalVariable[] variables;
    public final Residual[] equations;
    public final ExecutionContext[] views;

    /* memoization */
    private final DerivativeStructure[] residuals;
    private double[] lastPoint = null;

    /* equation solver */
    final OptimalitySolver solver = new OptimalitySolver();
    final ExecutionContext view;

    final double test = 1.0 / 0.0;

    public Block(double[][] data, DataLayout layout,
            Set<GlobalVariable> variables, Set<DerivedEquation> equations) {

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
        this.residuals = new DerivativeStructure[equations.size()];

        int index = 0;
        for (DerivedEquation e : equations) {
            this.equations[index] = new Residual(e.eqn.bind(blockVars),
                    e.derOrder);
            views[index++] = view.derived(e.derOrder);
        }

    }

    private Map<GlobalVariable, BlockVariable> makeBlockVars(DataLayout layout,
            Set<DerivedEquation> equations, final Map<Integer, Integer> gvIndex) {
        final Map<GlobalVariable, BlockVariable> blockVars = Maps.newHashMap();

        final Set<GlobalVariable> needed = Sets.newTreeSet();
        for (DerivedEquation eq : equations) {
            final int order = eq.derOrder;
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

        final DSCompiler comp = DSCompiler.getCompiler(ds.getFreeParameters(),
                ds.getOrder());
        final int i = comp.getPartialDerivativeIndex(orders);

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
            views[0].set(1, variables, point);

            for (int i = 0; i < equations.length; i++) {
                residuals[i] = equations[i].eq.exec(views[i]);
            }
        }
    }

    public void exec() {
        System.out.println(Arrays.toString(variables));
        double[] start = views[0].loadD(variables);

        final double[] point = solver.solve(1000, this, jacobian, start);

        views[0].set(1, variables, point);
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
