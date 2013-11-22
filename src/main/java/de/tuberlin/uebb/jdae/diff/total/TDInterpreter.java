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
package de.tuberlin.uebb.jdae.diff.total;

import org.apache.commons.math3.util.FastMath;

import com.google.common.base.Joiner;

import de.tuberlin.uebb.jbop.optimizer.Optimizer;
import de.tuberlin.uebb.jdae.diff.total.operations.Composition;
import de.tuberlin.uebb.jdae.diff.total.operations.CompositionProduct;
import de.tuberlin.uebb.jdae.diff.total.operations.Multiplication;

/**
 * @author choeger
 * 
 */
public final class TDInterpreter implements TDOperations {

    private static final Optimizer OPTIMIZER = new Optimizer();

    public final int order;
    public final int width;
    
    final Multiplication mult;
    final Composition comp;

    final TDOperations smaller;

    public TDInterpreter(int order, int params,
            Multiplication multOps, Composition compOps, TDOperations smaller) {
        this.order = order;
        this.width = params + 1;
        try {
            this.mult = OPTIMIZER.optimize(multOps, "_" + order);
            this.comp = OPTIMIZER.optimize(compOps, "_" + order);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        this.smaller = smaller;
    }

    public int order() {
        return order;
    }
    
    public int params() {
        return width - 1;
    }
    
    public Multiplication multOps() {
        return this.mult;
    }

    public Composition compOps() {
        return this.comp;
    }

    public final void add(final double[] a, final double[] b,
            final double[] target) {
        for (int j = 0; j < a.length; ++j)
                    target[j] = a[j] + b[j];
                    
    }

    public final void mult(final double[] a, final double[] b,
            final double[] target) {
        mult.multInd(a, b, target, width);
    }

    public void compose(double f[], final double[] a, final double[] target) {
        comp.compInd(f, a, target, width);
    }

    public TDOperations smaller() {
        return smaller;
    }

    public final void sin(final double[] a, final double[] target) {
        final double[] f = new double[order + 2];
        f[0] = Math.sin(a[0]);
        f[1] = Math.cos(a[0]);
        for (int n = 2; n < order + 2; n++)
            f[n] = -f[n - 2];

        compose(f, a, target);
    }

    public final void cos(final double[] a, final double[] target) {
        final double[] f = new double[order + 2];
        f[0] = Math.cos(a[0]);
        f[1] = -Math.sin(a[0]);
        for (int n = 2; n < order + 2; n++)
            f[n] = -f[n - 2];

        compose(f, a, target);
    }

    public final void pow(int n, final double[] a, final double[] target) {
        // create the power function value and derivatives
        // [x^n, nx^(n-1), n(n-1)x^(n-2), ... ]
        double[] f = new double[order + 2];

        if (n > 0) {
            // strictly positive power
            final int maxOrder = FastMath.min(order + 1, n);
            double xk = FastMath.pow(a[00], n - maxOrder);
            for (int i = maxOrder; i > 0; --i) {
                f[i] = xk;
                xk *= a[0];
            }
            f[0] = xk;
        } else {
            // strictly negative power
            final double inv = 1.0 / a[0];
            double xk = FastMath.pow(inv, -n);
            for (int i = 0; i <= order + 1; ++i) {
                f[i] = xk;
                xk *= inv;
            }
        }

        double coefficient = n;
        for (int i = 1; i <= order + 1; ++i) {
            f[i] *= coefficient;
            coefficient *= n - i;
        }

        compose(f, a, target);
    }

    public final void pow(double n, final double[] a, final double[] target) {
        final double[] f = new double[order + 2];
        f[0] = FastMath.pow(a[0], n);
        f[1] = n * FastMath.pow(a[0], n - 1);
        compose(f, a, target);
    }

    public TDNumber constantVar(int offset, double... dt) {
        final double[] c_values = new double[(order + 1) * width];

        for (int i = 0; i < Math.min(dt.length - offset, order + 1); ++i)
            c_values[i * width] = dt[i + offset];

        return new TDNumber(this, c_values);
    }

    public TDNumber constant(double d, double... dt) {
        final double[] c_values = new double[(order + 1) * width];
        c_values[0] = d;

        for (int i = 0; i < Math.min(dt.length, order); ++i)
            c_values[(i + 1) * width] = dt[i];

        return new TDNumber(this, c_values);
    }

    public TDNumber variable(int idx, double... der) {
        return variable(idx, 0, der);
    }

    public TDNumber variable(int idx, int derivatives, double... der) {
        final double[] vals = new double[(order + 1) * width];
        for (int i = 0; i < order + 1; i++)
            vals[i * width] = der[i];

        if (width > 1)
            for (int i = 0; i <= derivatives; i++)
                vals[i * width + 1 + idx + i] = 1;

        return new TDNumber(this, vals);
    }

    public String toString() {
        return String.format("TD with %d parameters, %d-times derived",
                width - 1, order);
    }

    public static String compStr(CompositionProduct[] compositionProducts) {
        return Joiner.on(" + ").join(compositionProducts).toString();
    }

}
