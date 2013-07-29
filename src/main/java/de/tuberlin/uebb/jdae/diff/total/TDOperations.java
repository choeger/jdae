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

import java.util.Arrays;

import org.apache.commons.math3.util.FastMath;

import com.google.common.collect.ObjectArrays;

import de.tuberlin.uebb.jdae.diff.partial.PDNumber;
import de.tuberlin.uebb.jdae.diff.partial.PDOperations;

/**
 * @author choeger
 * 
 */
public final class TDOperations {

    public final PDOperations subOps;
    public final int order;
    private final Product[][][] multOps;

    public TDOperations(int order, int params) {
        super();
        this.order = order;
        this.subOps = new PDOperations(params);
        multOps = compileMultIndirection();
    }

    public final void add(final PDNumber[] a, final PDNumber[] b,
            final PDNumber[] target) {
        for (int i = 0; i <= order; i++)
            target[i] = a[i].add(b[i]);
    }

    private final static class Product {
        public final int lhs_row;
        public final int lhs_column;
        public final int rhs_row;
        public final int rhs_column;
        public final int factor;

        public Product(int lhs_row, int lhs_column, int rhs_row,
                int rhs_column, int factor) {
            super();
            this.lhs_row = lhs_row;
            this.lhs_column = lhs_column;
            this.rhs_row = rhs_row;
            this.rhs_column = rhs_column;
            this.factor = factor;
        }
    }

    public final Product[][][] compileMultIndirection() {
        final Product[][][] multOps = new Product[order + 1][][];
        addMultOps(0, 0, 0, multOps);

        return multOps;
    }

    private final void addMultOps(int row, int a, int b, Product[][][] ops) {
        if (ops[row] == null) {
            ops[row] = new Product[subOps.params + 1][];
        }

        // (a*b)[0] == a[0] * b[0]
        final Product[] sum = new Product[] { new Product(a, 0, b, 0, 1) };
        if (ops[row][0] == null) {
            ops[row][0] = sum;
        } else {
            ops[row][0] = ObjectArrays.concat(sum, ops[row][0], Product.class);
        }

        // (a*b)[i] == a[0] * der(b)[i] + der(a)[i] * b[0]
        for (int i = 1; i <= subOps.params; ++i) {
            final Product[] sum2 = new Product[] { new Product(a, 0, b, i, 1),
                    new Product(a, i, b, 0, 1) };
            if (ops[row][i] == null) {
                ops[row][i] = sum2;
            } else {
                ops[row][i] = ObjectArrays.concat(sum2, ops[row][i],
                        Product.class);
            }
        }

        if (row < order) {
            addMultOps(row + 1, a + 1, b, ops);
            addMultOps(row + 1, a, b + 1, ops);
        }
    }

    public final void multInd(final PDNumber[] a, final PDNumber[] b,
            final PDNumber[] target) {

        for (int i = 0; i < multOps.length; ++i) {
            for (int j = 0; j < multOps[i].length; ++j) {
                for (Product product : multOps[i][j]) {
                    if (target[i] == null)
                        target[i] = new PDNumber(subOps.params);

                    target[i].values[j] += a[product.lhs_row].values[product.lhs_column]
                            * b[product.rhs_row].values[product.rhs_column]
                            * product.factor;
                }
            }
        }

    }

    public final void mult(final PDNumber[] a, final PDNumber[] b,
            final PDNumber[] target) {
        multInd(a, b, target);
    }

    // public final void mult(final PDNumber[] a, final PDNumber[] b,
    // final PDNumber[] target) {
    // if (target[0] == null)
    // target[0] = new PDNumber(a[0].getParams());
    //
    // final double a0 = a[0].values[0];
    // final double b0 = b[0].values[0];
    //
    // target[0].values[0] = a0 * b0;
    //
    // for (int i1 = 1; i1 <= subOps.params; i1++) {
    // target[0].values[i1] = a[0].values[i1] * b0 + b[0].values[i1] * a0;
    // }
    //
    // if (target.length > 1) {
    // final TDOperations sm = smaller();
    //
    // final PDNumber[] tmp1 = new PDNumber[target.length - 1];
    // sm.mult(diff(a), antiDiff(b), tmp1);
    //
    // final PDNumber[] tmp2 = new PDNumber[target.length - 1];
    // sm.mult(antiDiff(a), diff(b), tmp2);
    //
    // for (int i = 1; i < target.length; i++) {
    // target[i] = new PDNumber(a[0].getParams());
    // target[i].m_add(tmp1[i - 1].values);
    // target[i].m_add(tmp2[i - 1].values);
    // }
    // }
    // }

    public void compose(double f[], final PDNumber[] a, final PDNumber[] target) {
        compose(f, 0, a, target);
    }

    public void compose(double f[], int order, final PDNumber[] a,
            final PDNumber[] target) {
        if (target[0] == null)
            target[0] = new PDNumber(a[0].getParams());

        target[0].values[0] = f[order];
        for (int i1 = 1; i1 <= subOps.params; i1++) {
            target[0].values[i1] = a[0].values[i1] * f[order + 1];
        }

        if (target.length > 1) {
            final TDOperations sm = smaller();
            final PDNumber[] tmp1 = new PDNumber[target.length - 1];
            sm.compose(f, order + 1, antiDiff(a), tmp1);

            final PDNumber[] tmp2 = new PDNumber[target.length - 1];
            sm.mult(diff(a), tmp1, tmp2);

            for (int i = 1; i < target.length; i++)
                target[i] = tmp2[i - 1];
        }
    }

    public TDOperations smaller() {
        return new TDOperations(order - 1, subOps.params);
    }

    private PDNumber[] antiDiff(PDNumber[] a) {
        return Arrays.copyOfRange(a, 0, a.length - 1);
    }

    private PDNumber[] diff(PDNumber[] a) {
        return Arrays.copyOfRange(a, 1, a.length);
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

    public TDNumber constant(double d) {
        final PDNumber c = subOps.constant(d);
        final PDNumber[] c_values = new PDNumber[order + 1];
        c_values[0] = c;
        for (int i = 1; i < order + 1; ++i)
            c_values[i] = subOps.constant(0.0);

        return new TDNumber(c_values);
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

        return new TDNumber(vals);
    }

    public String toString() {
        return String.format("TD with %d parameters, %d-times derived",
                subOps.params, order);
    }

}
