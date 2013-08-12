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

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.util.FastMath;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.ObjectArrays;
import com.google.common.collect.Ordering;

import de.tuberlin.uebb.jdae.diff.partial.PDNumber;
import de.tuberlin.uebb.jdae.diff.partial.PDOperations;
import de.tuberlin.uebb.jdae.utils.IntPair;
import de.tuberlin.uebb.jdae.diff.total.TDOpsFactory.*;

/**
 * @author choeger
 * 
 */
public final class TDInterpreter implements TDOperations {

    public final PDOperations subOps;
    public final int order;

    final Product[][][] multOps;
    final CompositionProduct[][][] compOps;
    final TDInterpreter smaller;

    TDInterpreter(int order, int params, Product[][][] multOps, 
		  CompositionProduct[][][] compOps) {
        super();
        this.order = order;
        this.subOps = new PDOperations(params);
        this.multOps = multOps;
        this.compOps = compOps;
        smaller = order > 0 ? TDOpsFactory.getInstance(order - 1, subOps.params) : null;
        System.out.println(stats(compOps));
    }

    public int order() {
	return order;
    }

    public PDOperations subOps() {
	return subOps;
    }

    private String stats(CompositionProduct[][][] compositionProducts) {

        int keys = 0;
        int terms = 0;
        for (CompositionProduct[][] row : compositionProducts)
            for (CompositionProduct[] column : row)
                for (CompositionProduct prod : column) {
                    terms++;
                    keys += prod.key.keys.length;
                }
        return String.format("%d terms with %d keys in total.", terms, keys);
    }

    public final void add(final PDNumber[] a, final PDNumber[] b,
            final PDNumber[] target) {
        for (int i = 0; i <= order; i++) {
            if (target[i] == null)
                target[i] = a[i].add(b[i]);
            else {
                for (int j = 0; j < a[i].values.length; ++j)
                    target[i].values[j] = a[i].values[j] + b[i].values[j];
            }

        }
    }

 
    public final void compInd(final double[] f, final PDNumber[] a,
            final PDNumber[] target) {
        for (int i = 0; i < compOps.length; ++i) {
            if (target[i] == null)
                target[i] = new PDNumber(subOps, new double[subOps.params + 1]);

            for (int j = 0; j < compOps[i].length; ++j) {
                target[i].values[j] = 0;
                for (final CompositionProduct p : compOps[i][j]) {
                    double d = p.f_factor * f[p.key.f_order];
                    for (final IntPair k : p.key.keys) {
                        d *= a[k.x].values[k.y];
                    }
                    target[i].values[j] += d;
                }
            }
        }
    }

    public final void multInd(final PDNumber[] a, final PDNumber[] b,
            final PDNumber[] target) {

        for (int i = 0; i < multOps.length; ++i) {
            if (target[i] == null)
                target[i] = new PDNumber(subOps, new double[subOps.params + 1]);

            final Product[][] multOpsRow = multOps[i];

            for (int j = 0; j < multOpsRow.length; ++j) {
                target[i].values[j] = 0;
                for (Product product : multOpsRow[j]) {
                    target[i].values[j] += a[product.elements.lhs_row].values[product.elements.lhs_column]
                            * b[product.elements.rhs_row].values[product.elements.rhs_column]
                            * product.factor;
                }
            }
        }
    }

    public final void mult(final PDNumber[] a, final PDNumber[] b,
            final PDNumber[] target) {
        multInd(a, b, target);
    }

    public void compose(double f[], final PDNumber[] a, final PDNumber[] target) {
        compInd(f, a, target);
    }

    public TDOperations smaller() {
        return smaller;
    }

    public final void sin(final PDNumber[] a, final PDNumber[] target) {
        final double[] f = new double[order + 2];
        f[0] = Math.sin(a[0].values[0]);
        f[1] = Math.cos(a[0].values[0]);
        for (int n = 2; n < order + 2; n++)
            f[n] = -f[n - 2];

        compose(f, a, target);
    }

    public final void cos(final PDNumber[] a, final PDNumber[] target) {
        final double[] f = new double[order + 2];
        f[0] = Math.cos(a[0].values[0]);
        f[1] = -Math.sin(a[0].values[0]);
        for (int n = 2; n < order + 2; n++)
            f[n] = -f[n - 2];

        compose(f, a, target);
    }

    public final void pow(int n, final PDNumber[] a, final PDNumber[] target) {
        // create the power function value and derivatives
        // [x^n, nx^(n-1), n(n-1)x^(n-2), ... ]
        double[] f = new double[order + 2];

        if (n > 0) {
            // strictly positive power
            final int maxOrder = FastMath.min(order + 1, n);
            double xk = FastMath.pow(a[0].values[0], n - maxOrder);
            for (int i = maxOrder; i > 0; --i) {
                f[i] = xk;
                xk *= a[0].values[0];
            }
            f[0] = xk;
        } else {
            // strictly negative power
            final double inv = 1.0 / a[0].values[0];
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

    public final void pow(double n, final PDNumber[] a, final PDNumber[] target) {
        final double[] f = new double[order + 2];
        f[0] = FastMath.pow(a[0].values[0], n);
        f[1] = n * FastMath.pow(a[0].values[0], n - 1);
        compose(f, a, target);
    }

    public TDNumber constantVar(int offset, double... dt) {
        final PDNumber c = subOps.constant(dt[offset]);
        final PDNumber[] c_values = new PDNumber[order + 1];
        c_values[0] = c;

        for (int i = 1; i < Math.min(dt.length - offset, order + 1); ++i)
            c_values[i] = subOps.constant(dt[i + offset]);

        return new TDNumber(this, c_values);
    }

    public TDNumber constant(double d, double... dt) {
        final PDNumber c = subOps.constant(d);
        final PDNumber[] c_values = new PDNumber[order + 1];
        c_values[0] = c;

        for (int i = 0; i < Math.min(dt.length, order); ++i)
            c_values[i + 1] = subOps.constant(dt[i]);

        for (int i = dt.length; i < order; ++i)
            c_values[i + 1] = subOps.constant(0.0);

        return new TDNumber(this, c_values);
    }

    public TDNumber variable(int idx, double... der) {
        return variable(idx, 0, der);
    }

    public TDNumber variable(int idx, int derivatives, double... der) {
        final PDNumber[] vals = new PDNumber[order + 1];
        for (int i = 0; i < order + 1; i++)
            vals[i] = subOps.constant(der[i]);

        if (subOps.params > 0)
            for (int i = 0; i <= derivatives; i++)
                vals[i].values[1 + idx + i] = 1;

        return new TDNumber(this, vals);
    }

    public String toString() {
        return String.format("TD with %d parameters, %d-times derived",
                subOps.params, order);
    }

    public static String compStr(CompositionProduct[] compositionProducts) {
        return Joiner.on(" + ").join(compositionProducts).toString();
    }

}
