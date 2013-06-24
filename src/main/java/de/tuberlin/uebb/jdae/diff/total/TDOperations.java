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

import com.google.common.math.IntMath;

import de.tuberlin.uebb.jdae.diff.partial.MutablePDNumber;
import de.tuberlin.uebb.jdae.diff.partial.PDNumber;
import de.tuberlin.uebb.jdae.diff.partial.PDOperations;

/**
 * @author choeger
 * 
 */
public final class TDOperations {

    public final PDOperations subOps;
    public final int order;
    public final int[][] coefficients;

    public TDOperations(int order, int params) {
        super();
        this.order = order;
        this.subOps = new PDOperations(params);

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

    public final void add(final PDNumber[] a, final PDNumber[] b,
            final PDNumber[] target) {
        for (int i = 0; i <= order; i++)
            target[i] = a[i].add(b[i]);
    }

    public final void mult(final PDNumber[] a, final PDNumber[] b,
            final PDNumber[] target) {
        /* implement leibniz rule */

        for (int n = order; n >= 0; n--) {
            final MutablePDNumber ret = new MutablePDNumber(a[0].values.length);
            final MutablePDNumber tmp = new MutablePDNumber(a[0].values.length);
            for (int k = 0; k <= n; k++) {
                tmp.zero();
                tmp.add(a[k].values);
                tmp.mult(b[n - k].values);
                tmp.mult(coefficients[n][k]);
                ret.add(tmp.values);
            }
            target[n] = ret.asNumber();
        }
    }

    public final void compose(final double[] f, final PDNumber[] a,
            final PDNumber[] target) {
        for (int n = 0; n <= order; n++)
            subOps.compose(f[n], f[n + 1], a[n].values, target[n].values);
    }

    public final void sin(final PDNumber[] a, final PDNumber[] target) {
        final double[] f = new double[order + 2];
        f[0] = Math.sin(a[0].values[0]);
        f[1] = Math.cos(a[0].values[0]);
        for (int n = 2; n < order + 2; n++)
            f[n] = -f[n - 1];

        compose(f, a, target);
    }

    public final void cos(final PDNumber[] a, final PDNumber[] target) {
        final double[] f = new double[order + 2];
        f[0] = Math.cos(a[0].values[0]);
        f[1] = -Math.sin(a[0].values[0]);
        for (int n = 2; n < order + 2; n++)
            f[n] = -f[n - 1];

        compose(f, a, target);
    }

    public final void pow(int n, final PDNumber[] a, final PDNumber[] target) {
        final double[] f = new double[order + 2];
        f[0] = FastMath.pow(a[0].values[0], n);
        f[1] = (n - 1) * FastMath.pow(a[0].values[0], n - 1);
        compose(f, a, target);
    }

    public final void pow(double n, final PDNumber[] a, final PDNumber[] target) {
        final double[] f = new double[order + 2];
        f[0] = FastMath.pow(a[0].values[0], n);
        f[1] = (n - 1) * FastMath.pow(a[0].values[0], n - 1);
        compose(f, a, target);
    }

}
