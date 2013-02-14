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

package de.tuberlin.uebb.jdae.utils;

import java.util.Comparator;
import java.util.Map;

import de.tuberlin.uebb.jdae.dae.Unknown;

public final class VarComparator implements Comparator<Unknown> {
    private final Map<Unknown, Unknown> derivatives;

    public VarComparator(Map<Unknown, Unknown> derivatives) {
        super();
        this.derivatives = derivatives;
    }

    /* state < derivative < algebraic */
    @Override
    public int compare(Unknown o1, Unknown o2) {
        if (derivatives.containsKey(o1) && derivatives.containsKey(o2)) {
            return o1.toString().compareTo(o2.toString());
        } else if (derivatives.containsKey(o1)) {
            return -1;
        } else if (derivatives.containsKey(o2)) {
            return 1;
        } else if (o1.isDerivative()) {
            if (o2.isDerivative()) {
                return o1.toString().compareTo(o2.toString());
            } else {
                return -1;
            }
        } else if (o2.isDerivative()) {
            return 1;
        } else {
            return o1.toString().compareTo(o2.toString());
        }
    }
}