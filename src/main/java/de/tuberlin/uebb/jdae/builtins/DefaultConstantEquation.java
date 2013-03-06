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
import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;

import de.tuberlin.uebb.jdae.dae.ConstantEquation;
import de.tuberlin.uebb.jdae.dae.Equation;
import de.tuberlin.uebb.jdae.dae.SolvableDAE;
import de.tuberlin.uebb.jdae.dae.Unknown;

/**
 * Default implementation of {@link ConstantEquation}
 * 
 * @author Christoph Höger
 * 
 */
public class DefaultConstantEquation implements ConstantEquation {

    public final Unknown u;

    public final double c;

    public DefaultConstantEquation(Unknown u, double c) {
        super();
        this.u = u;
        this.c = c;
    }

    @Override
    public Equation specialize(SolvableDAE system) {
        return this;
    }

    @Override
    public double solveFor(int unknown, Unknown v, SolvableDAE system) {
        if (v == u)
            return c;

        throw new IllegalArgumentException("Cannot solve for: " + v);
    }

    @Override
    public double lhs(SolvableDAE systemState) {
        return systemState.apply(u);
    }

    @Override
    public double rhs(SolvableDAE systemState) {
        return c;
    }

    @Override
    public Unknown unknown() {
        return u;
    }

    @Override
    public double constant() {
        return c;
    }

    @Override
    public List<Unknown> unknowns() {
        return ImmutableList.of(u);
    }

    @Override
    public List<Double> coefficients() {
        return ImmutableList.of(1.0);
    }

    @Override
    public double timeCoefficient() {
        return 0;
    }

    @Override
    public Collection<Unknown> canSolveFor(Function<Unknown, Unknown> der) {
        return ImmutableList.of(u);
    }

}
