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

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealMatrixFormat;

import de.tuberlin.uebb.jdae.dae.ConstantLinear;
import de.tuberlin.uebb.jdae.dae.FunctionalEquation;
import de.tuberlin.uebb.jdae.dae.FunctionalVectorEquation;

public final class LinearFunctionalVectorEquation extends
        FunctionalVectorEquation {

    private final class LinearResidual {

        private final double[] coefficients;
        private final FunctionalEquation[] unknowns;
        private final double constant, timeCoefficient;

        public LinearResidual(ConstantLinear origin,
                FunctionalEquation[] unknowns) {
            this.constant = origin.constant();
            this.unknowns = unknowns;
            this.coefficients = origin.coefficients();
            this.timeCoefficient = origin.timeCoefficient();
        }

        public double value(double time) {
            double c = constant - timeCoefficient * time;

            for (int i = 0; i < unknowns.length; i++) {
                if (unknowns[i] != null) {
                    c -= coefficients[i] * unknowns[i].value(time);
                }
            }

            return c;
        }
    }

    public static long time_spent = 0;

    public final LinearResidual equations[];
    public final int unknowns[];

    private final RealMatrix M;
    private final DecompositionSolver solver;

    public LinearFunctionalVectorEquation(final int targets[],
            final FunctionalEquation[][] computed, final ConstantLinear[] parts) {
        unknowns = targets;
        equations = new LinearResidual[parts.length];

        M = new BlockRealMatrix(parts.length, parts.length);

        for (int i = 0; i < parts.length; i++) {
            equations[i] = new LinearResidual(parts[i], computed[i]);

            final double[] coefficients = parts[i].coefficients();

            int j = 0;
            int c = 0;
            while (j < parts.length) {

                while (computed[i][c] != null) {
                    c++;
                }

                ;

                M.setEntry(i, j, coefficients[c]);
                j++;
                c++;
            }
        }

        System.out.println(RealMatrixFormat.getInstance().format(M));
        final LUDecomposition comp = new LUDecomposition(M);
        solver = comp.getSolver();
    }

    @Override
    public double[] compute(double time) {
        final double d[] = new double[equations.length];
        for (int i = 0; i < equations.length; i++) {
            d[i] = equations[i].value(time);
        }
        return solver.solve(new ArrayRealVector(d)).toArray();
    }

    @Override
    public int[] unknown() {
        return unknowns;
    }

}
