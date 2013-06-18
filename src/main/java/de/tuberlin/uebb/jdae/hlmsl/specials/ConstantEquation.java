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
package de.tuberlin.uebb.jdae.hlmsl.specials;

import com.google.common.collect.ImmutableList;

import de.tuberlin.uebb.jdae.hlmsl.Unknown;

/**
 * An equation describing a constant, time-independent function. By convention
 * the lhs is the unknown, while the rhs yields the value. It is a special case
 * of @{link ConstantLinear}.
 * 
 * @author choeger
 * 
 */
public class ConstantEquation extends ConstantLinear {

    public ConstantEquation(Unknown lhs, double rhs) {
        super(0.0, rhs, new double[] { 1.0 }, ImmutableList.of(lhs));
    }
}
