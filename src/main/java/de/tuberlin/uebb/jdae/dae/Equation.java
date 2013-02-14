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

package de.tuberlin.uebb.jdae.dae;

import java.util.Collection;

import com.google.common.base.Function;

public interface Equation {

    public Equation specialize(final SolvableDAE system);

    public Collection<Unknown> canSolveFor(final Function<Unknown, Unknown> der);

    public double solveFor(final int unknown, final Unknown v,
            final SolvableDAE system);

    public double lhs(final SolvableDAE systemState);

    public double rhs(final SolvableDAE systemState);

}
