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

package de.tuberlin.uebb.jdae.transformation;

import java.util.Collection;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;

import de.tuberlin.uebb.jdae.dae.Equation;
import de.tuberlin.uebb.jdae.dae.SolvableDAE;
import de.tuberlin.uebb.jdae.dae.Unknown;

/**
 * An integration equation symbolizes the relationship between a variable and
 * it's derivative. Such an equation should only be created during the
 * causalisation process.
 * 
 * @author Christoph Höger
 */
public final class IntegrationEquation implements Equation {

    public final Unknown v;

    public IntegrationEquation(Unknown v) {

        this.v = v;
    }

    @Override
    public Collection<Unknown> canSolveFor(Function<Unknown, Unknown> der) {
        // NASTY HACK FOR INDEX-0 SYSTEMS
        return ImmutableList.of(v); // der.apply(v));
    }

    @Override
    public double solveFor(final int index, Unknown v,
            final SolvableDAE systemState) {
        throw new RuntimeException(
                "Integration equations are not solved directly currently.");
    }

    @Override
    public double lhs(final SolvableDAE systemState) {
        throw new RuntimeException(
                "Integration equations are not solved directly currently.");
    }

    @Override
    public double rhs(final SolvableDAE systemState) {
        throw new RuntimeException(
                "Integration equations are not solved directly currently.");
    }

    @Override
    public Equation specialize(SolvableDAE system) {
        return this;
    }

}
