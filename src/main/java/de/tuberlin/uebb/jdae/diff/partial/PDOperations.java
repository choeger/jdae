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
package de.tuberlin.uebb.jdae.diff.partial;

import org.apache.commons.math3.util.FastMath;

/**
 * @author choeger
 * 
 */
public final class PDOperations {

    public final int params;

    public PDOperations(int params) {
        super();
        this.params = params;
    }

    public final void mult(final double[] a, final double[] b,
            final double[] target) {
        target[0] = a[0] * b[0];

        for (int i = 1; i < params; i++) {
            target[i] = a[i] * b[0] + b[i] * a[0];
        }
    }

    public final void add(final double[] a, final double[] b,
            final double[] target) {
        for (int i = 0; i < params; i++) {
            target[i] = a[i] + b[i];
        }
    }

    public final void compose(final double f, final double df,
            final double[] values, final double[] target) {
        target[0] = f;
        for (int i = 1; i < params; i++) {
            target[i] = values[i] * df;
        }
    }

    public final void sin(final double[] values, final double[] target) {
        compose(Math.sin(values[0]), Math.cos(values[0]), values, target);
    }

    public final void cos(final double[] values, final double[] target) {
        compose(Math.cos(values[0]), -Math.sin(values[0]), values, target);
    }

    public final void pow(int n, final double[] values, final double[] target) {
        final double f = FastMath.pow(values[0], n);
        final double df = (n - 1) * FastMath.pow(values[0], n - 1);
        compose(f, df, target, values);
    }

    public final void pow(double n, final double[] values, final double[] target) {
        final double f = FastMath.pow(values[0], n);
        final double df = (n - 1) * FastMath.pow(values[0], n - 1);
        compose(f, df, target, values);
    }

}
