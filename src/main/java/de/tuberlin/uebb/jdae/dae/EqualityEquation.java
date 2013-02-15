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
import com.google.common.collect.ImmutableList;

public final class EqualityEquation implements Equation {

    public EqualityEquation(Unknown l, Unknown r) {
        lhs_obj = l;
        rhs_obj = r;
    }

    public String toString() {
        return String.format("%s = %s", lhs_obj, rhs_obj);
    }

    public final Unknown lhs_obj;
    public final Unknown rhs_obj;

    @Override
    public double lhs(final SolvableDAE systemState) {
        return systemState.apply(lhs_obj);
    }

    @Override
    public double rhs(final SolvableDAE systemState) {
        return systemState.apply(rhs_obj);
    }

    @Override
    public Collection<Unknown> canSolveFor(Function<Unknown, Unknown> der) {
        return ImmutableList.of(lhs_obj, rhs_obj);
    }

    @Override
    public double solveFor(final int index, Unknown v,
            final SolvableDAE systemState) {

        if (v == lhs_obj)
            return systemState.apply(rhs_obj);
        else if (v == rhs_obj)
            return systemState.apply(lhs_obj);

        throw new IllegalArgumentException("Cannot solve for" + v);
    }

    @Override
    public Equation specialize(SolvableDAE system) {
        return this;
    }
}