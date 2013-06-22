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
        for (int i = 0; i <= order; i++) {
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

    /**
     * Set the d^nx/dt^n value for an abitrary n
     * 
     * @param order
     * @param value
     * @param pd
     */
    public void setDt(int order, double value, double[] pd) {
        pd[compiler.getPartialDerivativeIndex(dtOrders[order])] = value;
    }

    /**
     * Mark the n-th derivative of x as x_j (a variable in our block).
     * 
     * @param dt
     * @param derIndex
     */
    public final void setDer(int dt, int j, double[] pd) {
        final int[] order = new int[compiler.getFreeParameters()];
        order[0] = dt;
        order[j + 1] = 1;
        final int partialDerivativeIndex = compiler
                .getPartialDerivativeIndex(order);
        pd[partialDerivativeIndex] = 1.0;
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
        return build(pd);
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

    public double[] allocate() {
        return new double[compiler.getSize()];
    }

    public DerivativeStructure build(double[] number) {
        return new DerivativeStructure(compiler.getFreeParameters(),
                sumOrder(), number);
    }
}
