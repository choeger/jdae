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

    final GlobalVariable[] params;
    final DSCompiler compiler;
    public final int[][] dtOrders;
    final double[][] data;

    public ExecutionContext(int order, GlobalVariable[] vars, double[][] data) {
        super();

        this.order = order;
        this.params = vars;
        this.data = data;

        this.compiler = DSCompiler.getCompiler(params.length + 1, sumOrder());

        this.dtOrders = new int[sumOrder()][];
        for (int i = 1; i <= order; i++) {
            dtOrders[i] = new int[params.length + 1];
            dtOrders[i][0] = i;
        }
    }

    public final void set(int start, GlobalVariable[] vars, double[] point) {
        int i = start;
        for (GlobalVariable v : vars) {
            data[v.index][v.der] = point[i++];
        }
    }

    public double[] loadD(GlobalVariable[] vars) {
        double[] ret = new double[vars.length + 1];
        ret[0] = data[0][0];
        for (int i = 0; i < vars.length; i++)
            ret[i + 1] = loadD(vars[i]);
        return ret;
    }

    public double loadD(GlobalVariable v) {
        return data[v.index][v.der];
    }

    public DerivativeStructure load(BlockVariable v) {
        if (v.relativeIndex <= 0) {
            /* return a constant */
            return loadConstant(v.absoluteIndex, v.derivationOrder,
                    v.relativeIndex);
        } else {
            return loadVariable(v.absoluteIndex, v.derivationOrder,
                    v.relativeIndex);
        }
    }

    public DerivativeStructure load(final BlockVariable v, int der) {
        final int next = v.relativeIndex + der;
        final int dnext = v.derivationOrder + der;
        if (next < params.length && params[next].index == v.absoluteIndex
                && params[next].der == dnext)
            return loadVariable(v.absoluteIndex, dnext, next);
        else
            return loadConstant(v.absoluteIndex, dnext, v.relativeIndex);
    }

    private final double[] loadValAndDer(final int index, final int der,
            final int relative) {
        double[] pd = new double[compiler.getSize()];

        pd[0] = data[index][der];
        for (int i = der + 1; i <= order; i++)
            setDt(i - der, data[index][i], pd);

        int j = relative - 1;
        while (++j < params.length && params[j].index == index) {
            setDer(params[j].der - der, j, pd);
        }

        return pd;
    }

    private final DerivativeStructure loadVariable(final int index,
            final int der, final int relative) {
        final double[] pd = loadValAndDer(index, der, relative);

        final int[] orders = new int[compiler.getFreeParameters()];
        orders[relative] = 1;
        pd[compiler.getPartialDerivativeIndex(orders)] = 1;

        return new DerivativeStructure(compiler.getFreeParameters(),
                sumOrder(), pd);
    }

    private final DerivativeStructure loadConstant(final int index,
            final int der, final int relative) {
        final double[] pd = loadValAndDer(index, der, -relative);

        return new DerivativeStructure(compiler.getFreeParameters(),
                sumOrder(), pd);
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
            final int[] order = new int[compiler.getFreeParameters()];
            order[0] = n;
            order[j + 1] = 1;
            final int partialDerivativeIndex = compiler
                    .getPartialDerivativeIndex(order);
            pd[partialDerivativeIndex] = 1.0;
        }
    }

    public final DerivativeStructure constant(final double value) {
        return new DerivativeStructure(compiler.getFreeParameters(),
                sumOrder(), value);
    }

    public final DerivativeStructure time() {
        final double[] pd = new double[compiler.getSize()];
        if (order > 0)
            setDt(1, 1.0, pd);
        pd[0] = data[0][0];
        return new DerivativeStructure(compiler.getFreeParameters(),
                sumOrder(), pd);
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
        return new ExecutionContext(order + derOrder, params, data);
    }

    public BlockVariable der(final BlockVariable v) {
        final int next = v.relativeIndex + 1;
        final int dnext = v.derivationOrder + 1;
        if (next < params.length && params[next].index == v.absoluteIndex
                && params[next].der == dnext)
            return new BlockVariable(v.absoluteIndex, dnext, next);
        else
            return new BlockVariable(v.absoluteIndex, dnext, -1);
    }
}
