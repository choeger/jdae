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

public final class SpecializedConstantLinearEquation implements Equation {

    public static long time_spent = 0;

    public final double time;
    public final int[] variables;
    public final double[] coefficients;
    public final double constant;

    private final ConstantLinearEquation origin;

    public SpecializedConstantLinearEquation(
            ConstantLinearEquation constantLinearEquation, SolvableDAE system) {
        this.origin = constantLinearEquation;

        time = constantLinearEquation.time;
        variables = new int[origin.variables.size()];
        coefficients = new double[origin.variables.size()];

        for (int i = 0; i < origin.variables.size(); i++) {
            variables[i] = system.variables.get(origin.variables.get(i));
            coefficients[i] = origin.coefficients.get(i);
        }
        constant = origin.constant;
    }

    @Override
    public Collection<Unknown> canSolveFor(Function<Unknown, Unknown> der) {
        return origin.canSolveFor(der);
    }

    @Override
    public double solveFor(final int index, Unknown v, SolvableDAE systemState) {
        // final long start = System.nanoTime();
        double val = systemState.currentTime * this.time;
        double c = 1.0;

        for (int i = 0; i < variables.length; i++) {
            final int v_index = variables[i];
            if (v_index != index)
                val += coefficients[i] * systemState.get(variables[i]);
            else
                c = coefficients[i];
        }

        final double d = (constant - val) / c;
        // time_spent += System.nanoTime() - start;
        return d;
    }

    @Override
    public double lhs(SolvableDAE systemState) {
        return origin.lhs(systemState);
    }

    @Override
    public double rhs(SolvableDAE systemState) {
        return origin.rhs(systemState);
    }

    @Override
    public Equation specialize(SolvableDAE system) {
        return this;
    }

}
