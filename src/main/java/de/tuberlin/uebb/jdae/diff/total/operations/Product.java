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

package de.tuberlin.uebb.jdae.diff.total.operations;

import de.tuberlin.uebb.jdae.diff.partial.PDNumber;
import de.tuberlin.uebb.jdae.utils.IntTriple;

public final class Product {
    public final int factor;
    public final IntTriple key;

    public Product(int lhs, int rhs, boolean aDer, int factor) {
        super();
        this.key = new IntTriple(lhs, rhs, aDer ? 1 : 0);
        this.factor = factor;
    }

    protected Product(final IntTriple key, int factor) {
        this.key = key;
        this.factor = factor;
    }

    public Product plusOne() {
        return new Product(key, factor + 1);
    }

    public String toString() {
        if (key.z == 1) {
            return String.format("%d * a[%d][i] * b[%d][0]", factor, key.x,
                    key.y);
        } else
            return String.format("%d * a[%d][0] * b[%d][i]", factor, key.x,
                    key.y);
    }

    public double eval(final int col, final PDNumber[] a, final PDNumber[] b) {
        return factor * a[key.x].values[key.z * col]
                * b[key.y].values[(1 - key.z) * col];
    }
}