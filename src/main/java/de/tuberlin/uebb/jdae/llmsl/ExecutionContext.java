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

import org.apache.commons.math3.analysis.differentiation.DSCompiler;
import org.apache.commons.math3.analysis.differentiation.DerivativeStructure;

public final class ExecutionContext {

    final int order;

    final int params;
    final DSCompiler compiler;
    public final int[][] dtOrders;
    final double[][] data;

    public ExecutionContext(int order, int params, double[][] data) {
        super();

        this.order = order;
        this.params = params;
        this.data = data;

        this.compiler = DSCompiler.getCompiler(params, sumOrder());

        this.dtOrders = new int[sumOrder()][];
        for (int i = 1; i <= order; i++) {
            dtOrders[i] = new int[params];
            dtOrders[i][0] = i;
        }
    }

    public final void set(BlockVariable[] vars, double[] point) {
        for (BlockVariable v : vars) {
            data[v.absoluteIndex][v.derivationOrder] = point[v.relativeIndex];
        }
    }

    public double[] loadD(BlockVariable[] vars) {
        double[] ret = new double[vars.length + 1];
        ret[0] = data[0][0];
        for (int i = 0; i < vars.length; i++)
            ret[i + 1] = loadD(vars[i]);
        return ret;
    }

    public double loadD(BlockVariable v) {
        return data[v.absoluteIndex][v.derivationOrder];
    }

    public DerivativeStructure load(BlockVariable v) {
        if (v.relativeIndex < 0) {
            /* return a constant */
            return loadConstant(v);
        } else {
            return loadVariable(v);
        }
    }

    private final DerivativeStructure loadVariable(BlockVariable v) {

        double[] pd = new double[compiler.getSize()];

        for (int i = v.derivationOrder + 1; i <= order; i++)
            setDt(i - v.derivationOrder, data[v.absoluteIndex][i], pd);

        int n = 1;
        for (int j : v.relativeDerivatives) {
            setDer(n++, j, pd);
        }

        final DerivativeStructure zero = new DerivativeStructure(params,
                sumOrder(), pd);

        return new DerivativeStructure(params, sumOrder(), v.relativeIndex,
                data[v.absoluteIndex][v.derivationOrder]).add(zero);
    }

    /**
     * Set the d^nx/dt^n value for an abitrary n
     * 
     * @param order
     * @param value
     * @param pd
     */
    private void setDt(int order, double value, double[] pd) {
        pd[compiler.getPartialDerivativeIndex(dtOrders[order])] = value;
    }

    /**
     * Mark the n-th derivative of x as x_j (a variable in our block).
     * 
     * @param dt
     * @param derIndex
     */
    private void setDer(int n, int j, double[] pd) {
        if (j > 0 && n <= order) {
            final int[] order = new int[params];
            order[0] = n;
            order[j] = 1;
            final int partialDerivativeIndex = compiler
                    .getPartialDerivativeIndex(order);
            pd[partialDerivativeIndex] = 1.0;
        }
    }

    private final DerivativeStructure loadConstant(BlockVariable v) {
        double[] pd = new double[compiler.getSize()];

        pd[0] = data[v.absoluteIndex][v.derivationOrder];
        for (int i = v.derivationOrder + 1; i <= order; i++)
            setDt(i - v.derivationOrder, data[v.absoluteIndex][i], pd);

        int n = 1;
        for (int j : v.relativeDerivatives)
            setDer(n++, j, pd);

        return new DerivativeStructure(params, sumOrder(), pd);
    }

    public final DerivativeStructure constant(final double value) {
        return new DerivativeStructure(this.params, sumOrder(), value);
    }

    public final DerivativeStructure time() {
        final double[] pd = new double[compiler.getSize()];
        if (order > 0)
            setDt(1, 1.0, pd);
        pd[0] = data[0][0];
        return new DerivativeStructure(this.params, sumOrder(), pd);
    }

    /**
     * Calculate the Rall number order of this model view.
     * 
     * @return the total derivation order + 1
     */
    private int sumOrder() {
        return order + 1;
    }

    public ExecutionContext derived(int derOrder) {
        return new ExecutionContext(derOrder, params, data);
    }
}
