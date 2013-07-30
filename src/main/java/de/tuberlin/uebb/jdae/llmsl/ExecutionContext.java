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

import de.tuberlin.uebb.jdae.diff.total.TDNumber;
import de.tuberlin.uebb.jdae.diff.total.TDOperations;

public final class ExecutionContext {

    final int order;

    final GlobalVariable[] params;
    public final TDOperations compiler;
    final double[][] data;

    public ExecutionContext(int order, GlobalVariable[] vars, double[][] data) {
        super();

        this.order = order;
        this.params = vars.clone();
        this.data = data.clone();

        this.compiler = TDOperations.getInstance(order, params.length);
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

    public final TDNumber constant(final GlobalVariable var) {
        return compiler.constantVar(var.der, data[var.index]);
    }

    public final TDNumber constant(final double value) {
        return compiler.constant(value);
    }

    public final TDNumber time() {
        return compiler.constant(data[0][0], 1.0);
    }

    public ExecutionContext derived(int derOrder) {
        return new ExecutionContext(order + derOrder, params, data);
    }
}
