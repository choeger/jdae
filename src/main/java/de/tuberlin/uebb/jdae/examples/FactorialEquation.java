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

package de.tuberlin.uebb.jdae.examples;

import org.apache.commons.math3.analysis.differentiation.DerivativeStructure;
import org.apache.commons.math3.analysis.differentiation.MultivariateDifferentiableVectorFunction;
import org.apache.commons.math3.exception.MathIllegalArgumentException;

/**
 * @author choeger
 * 
 */
public class FactorialEquation implements
        MultivariateDifferentiableVectorFunction {

    private final double y;

    public FactorialEquation(double d) {
        y = d;
    }

    private final double factorial(final double x) {
        if (x <= 1.0)
            return 1.0;
        else
            return x * factorial(x - 1);
    }

    private final DerivativeStructure dfactorial(final DerivativeStructure ds) {
        if (ds.getValue() <= 1.0) {
            final DerivativeStructure divide = ds.divide(ds);
            return divide;
        } else
            return ds.multiply(dfactorial(ds.subtract(1.0)));
    }

    @Override
    public double[] value(double[] point) throws IllegalArgumentException {
        return new double[] { factorial(point[0]) - y };
    }

    @Override
    public DerivativeStructure[] value(DerivativeStructure[] point)
            throws MathIllegalArgumentException {
        return new DerivativeStructure[] { dfactorial(point[0]).subtract(
                new DerivativeStructure(point[0].getFreeParameters(), 1, y)) };
    }

}
