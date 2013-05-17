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

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.differentiation.DerivativeStructure;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;

import de.tuberlin.uebb.jdae.dae.ADEquation;
import de.tuberlin.uebb.jdae.dae.ConstantEquation;
import de.tuberlin.uebb.jdae.dae.FunctionalEquation;
import de.tuberlin.uebb.jdae.dae.SolvableDAE;
import de.tuberlin.uebb.jdae.dae.Unknown;

/**
 * Default implementation of {@link ConstantEquation}
 * 
 * @author Christoph Höger
 * 
 */
public class DefaultConstantEquation implements ConstantEquation {

    public static final double[] SINGLE_ONE = { 1.0 };

    final class ZeroDerivative extends ADEquation {

        final int u;
        final DerivativeStructure constant;

        public ZeroDerivative(int u, int order, double c) {
            super(order);
            this.u = u;
            constant = new DerivativeStructure(1, order, c);
        }

        @Override
        public DerivativeStructure compute(DerivativeStructure time) {
            return constant;
        }

        @Override
        public int unknown() {
            return u;
        }

    };

    public final Unknown u;

    public final double c;

    public DefaultConstantEquation(Unknown u, double c) {
        super();
        this.u = u;
        this.c = c;
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
    public double[] coefficients() {
        return SINGLE_ONE;
    }

    @Override
    public double timeCoefficient() {
        return 0;
    }

    @Override
    public Collection<Unknown> canSolveFor(Function<Unknown, Unknown> der) {
        return ImmutableList.of(u);
    }

    @Override
    public FunctionalEquation specializeFor(Unknown unknown, SolvableDAE system) {
        assert (u == unknown);
        return new ConstantFunctionalEquation(system.variables.get(u), c);
    }

    @Override
    public UnivariateFunction residual(SolvableDAE system) {
        return null; // who needs a constant's residual?
    }

    @Override
    public FunctionalEquation specializeFor(Unknown unknown,
            SolvableDAE system, int der_index) {
        if (der_index == 0)
            return specializeFor(unknown, system);
        else
            return new ZeroDerivative(system.variables.get(unknown), der_index,
                    c);
    }

}
