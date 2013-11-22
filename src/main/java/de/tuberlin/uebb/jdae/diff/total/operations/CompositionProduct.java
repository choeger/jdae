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

import de.tuberlin.uebb.jdae.utils.IntPair;

public final class CompositionProduct {

    public final int f_factor;
    public final CompositionKey key;

    public CompositionProduct(int f_factor, CompositionKey key) {
        this.key = key;
        this.f_factor = f_factor;
    }

    public CompositionProduct(int f_order, int f_factor, IntPair[] keys) {
        super();
        this.key = new CompositionKey(f_order, keys);
        this.f_factor = f_factor;
    }

    public CompositionProduct plus(CompositionProduct o) {
        return new CompositionProduct(f_factor + o.f_factor, key);
    }

    public String toString() {
        return "( " + f_factor + " * " + key + ")";
    }
}