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
import java.util.Map;

import org.apache.commons.math3.util.FastMath;

import com.google.common.collect.Maps;
import com.google.common.math.IntMath;

import de.tuberlin.uebb.jdae.diff.partial.PDNumber;
import de.tuberlin.uebb.jdae.diff.partial.PDOperations;

/**
 * @author choeger
 * 
 */
public final class TDOperations {

    public static final class Binom {
        public final int[][] coefficients;

        private Binom(int order) {
            coefficients = new int[order + 1][];
            /**
             * prepare binomial coeff array TODO: make faster (and static?)
             */
            for (int n = 0; n <= order; n++) {
                coefficients[n] = new int[n + 1];

                for (int k = 0; k <= n; k++)
                    coefficients[n][k] = IntMath.binomial(n, k);
            }
        }

        final static Map<Integer, Binom> tables = Maps.newTreeMap();

        public static Binom getBinomTable(int order) {
            if (!tables.containsKey(order)) {
                tables.put(order, new Binom(order));
            }
            return tables.get(order);
        }
    }

    public final PDOperations subOps;
    public final int order;
    public final Binom binom;

    public TDOperations(int order, int params) {
        super();
        this.order = order;
        this.subOps = new PDOperations(params);
        this.binom = Binom.getBinomTable(order);
    }

    public final void add(final PDNumber[] a, final PDNumber[] b,
            final PDNumber[] target) {
        for (int i = 0; i <= order; i++)
            target[i] = a[i].add(b[i]);
    }

    // public final void mult(final PDNumber[] a, final PDNumber[] b,
    // final PDNumber[] target) {
    // /* implement leibniz rule */
    // final PDNumber tmp = new PDNumber(a[0].values.length - 1);
    //
    // for (int n = (order); n >= 0; n--) {
    // tmp.m_add(a[n].values);
    // tmp.m_mult(b[0].values);
    // tmp.m_mult(binom.coefficients[n][0]);
    //
    // if (target[n] == null)
    // target[n] = new PDNumber(tmp.values);
    // else {
    // for (int i = 0; i < target[n].values.length; i++)
    // target[n].values[i] = tmp.values[i];
    // }
    //
    // for (int k = 1; k <= n; k++) {
    // Arrays.fill(tmp.values, 0.0);
    // tmp.m_add(a[n - k].values);
    // tmp.m_mult(b[k].values);
    // tmp.m_mult(binom.coefficients[n][k]);
    // target[n].m_add(tmp.values);
    // }
    // }
    // }

    public final void mult(final PDNumber[] a, final PDNumber[] b,
            final PDNumber[] target) {
        if (target[0] == null)
            target[0] = new PDNumber(a[0].getParams());

        subOps.mult(a[0].values, b[0].values, target[0].values);

        if (target.length > 1) {
            final TDOperations sm = smaller();

            final PDNumber[] tmp1 = new PDNumber[target.length - 1];
            sm.mult(diff(a), antiDiff(b), tmp1);

            final PDNumber[] tmp2 = new PDNumber[target.length - 1];
            sm.mult(antiDiff(a), diff(b), tmp2);

            for (int i = 1; i < target.length; i++) {
                target[i] = new PDNumber(a[0].getParams());
                target[i].m_add(tmp1[i - 1].values);
                target[i].m_add(tmp2[i - 1].values);
            }
        }
    }

    public void compose(double f[], final PDNumber[] a, final PDNumber[] target) {
        compose(f, 0, a, target);
    }

    public void compose(double f[], int order, final PDNumber[] a,
            final PDNumber[] target) {
        if (target[0] == null)
            target[0] = new PDNumber(a[0].getParams());

        subOps.compose(f[order], f[order + 1], a[0].values, target[0].values);

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
        return new TDNumber(c_values);
    }

    public TDNumber variable(int idx, double... der) {
        return variable(idx, 0, der);
    }

    public TDNumber variable(int idx, int derivatives, double... der) {
        final PDNumber[] vals = new PDNumber[order + 1];
        for (int i = 0; i < order + 1; i++)
            vals[i] = subOps.constant(der[i]);

        for (int i = 0; i <= derivatives; i++)
            vals[i].values[1 + idx + i] = 1;

        return new TDNumber(vals);
    }

}
