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

import org.apache.commons.math3.linear.RealMatrix;

import de.tuberlin.uebb.jdae.dae.FunctionalEquation;

public final class LinearFunctionalEquation extends FunctionalEquation {

    public static long time_spent = 0;

    public final int index;
    public final double timeCoefficient;
    public final FunctionalEquation[] variables;
    public final double[] coefficients;
    public final double constant;

    public LinearFunctionalEquation(final int target,
            final FunctionalEquation[] unknowns, final double[] coefficients,
            final double timeCoefficient, final double constant) {

        this.index = target;
        this.timeCoefficient = timeCoefficient;
        this.variables = unknowns;
        this.coefficients = coefficients;
        this.constant = constant;
    }

    @Override
    public double compute(double time) {
        // final long start = System.nanoTime();
        double val = time * this.timeCoefficient;
        double c = 0;

        for (int i = 0; i < variables.length; i++) {
            final FunctionalEquation f = variables[i];
            if (f != null) // null in case i == unknown() !
                val += coefficients[i] * f.value(time);
            else
                c += coefficients[i];
        }

        final double d = (constant - val) / c;
        // time_spent += System.nanoTime() - start;
        return d;
    }

    @Override
    public int unknown() {
        return index;
    }

    public void setMatrix(double t, RealMatrix m) {
        for (int i = 0; i < variables.length; i++) {
            m.setEntry(index, i, constant - t * timeCoefficient);
        }
    }

}
