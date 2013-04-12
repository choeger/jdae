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

package de.tuberlin.uebb.jdae.builtins;

import java.util.Collection;

import org.apache.commons.math3.analysis.UnivariateFunction;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;

import de.tuberlin.uebb.jdae.dae.Equality;
import de.tuberlin.uebb.jdae.dae.FunctionalEquation;
import de.tuberlin.uebb.jdae.dae.SolvableDAE;
import de.tuberlin.uebb.jdae.dae.Unknown;

public final class EqualityEquation implements Equality {

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
    public Unknown lhs() {
        return lhs_obj;
    }

    @Override
    public Unknown rhs() {
        return rhs_obj;
    }

    @Override
    public FunctionalEquation specializeFor(Unknown unknown, SolvableDAE system) {
        /* to prevent cycling, this should actually not happen ?? */
        return system.get(unknown);
    }

    @Override
    public Collection<Unknown> canSolveFor(Function<Unknown, Unknown> der) {
        return ImmutableList.of(lhs_obj, rhs_obj);
    }

    @Override
    public UnivariateFunction residual(SolvableDAE system) {
        final UnivariateFunction l = system.get(lhs_obj);
        final UnivariateFunction r = system.get(rhs_obj);

        return new UnivariateFunction() {

            @Override
            public double value(double t) {
                return l.value(t) - r.value(t);
            }

        };
    }
}