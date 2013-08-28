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

import java.util.Arrays;

import de.tuberlin.uebb.jdae.utils.IntPair;

public final class CompositionKey {
    public final int f_order;
    public final IntPair[] keys;

    public CompositionKey(int f_order, IntPair[] keys) {
        super();
        this.f_order = f_order;
        this.keys = Arrays.copyOf(keys, keys.length);
        Arrays.sort(this.keys);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + f_order;
        result = prime * result + Arrays.hashCode(keys);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CompositionKey other = (CompositionKey) obj;
        if (f_order != other.f_order)
            return false;
        if (!Arrays.equals(keys, other.keys))
            return false;
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder b = new StringBuilder();
        b.append("f^(" + f_order + ") * P[");
        for (IntPair k : keys) {
            b.append("a_(");
            b.append(k.x);
            b.append(", ");
            b.append(k.y == 0 ? "0" : "i");
            b.append(") ");
        }
        b.append("]");
        return b.toString();
    }
}