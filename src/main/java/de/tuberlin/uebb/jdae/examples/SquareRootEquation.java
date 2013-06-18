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
 * @author choeger x² + y² = 1
 * 
 */
public class SquareRootEquation implements
        MultivariateDifferentiableVectorFunction {
    final double y;

    public SquareRootEquation(double y) {
        super();
        this.y = y;
    }

    @Override
    public double[] value(double[] point) throws IllegalArgumentException {
        return new double[] { point[0] * point[0] + y * y - 1 };
    }

    @Override
    public DerivativeStructure[] value(DerivativeStructure[] point)
            throws MathIllegalArgumentException {
        return new DerivativeStructure[] { point[0].multiply(point[0])
                .add(y * y).subtract(1) };
    }

}
