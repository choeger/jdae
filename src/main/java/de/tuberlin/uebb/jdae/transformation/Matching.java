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

import java.util.logging.Logger;

import de.tuberlin.uebb.jdae.llmsl.GlobalVariable;
import de.tuberlin.uebb.jdae.utils.HungarianIntegerAlgorithm;

public final class Matching {

    public final int[] assignment;
    public final int[] inverse;
    private final int[][] sigma; // TODO: replace by sparse impl

    public Matching(final Reduction reduction, final Logger logger) {

        final int n = reduction.reduced.size();

        sigma = new int[n][];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                sigma[i] = new int[n];
                sigma[i][j] = Integer.MAX_VALUE;
            }
            for (GlobalVariable v : reduction.reduced.get(i).need()) {
                final int j = v.index - 1;
                sigma[i][j] = Math.min(sigma[i][j], -v.der);
            }
        }

        final HungarianIntegerAlgorithm hung = new HungarianIntegerAlgorithm(
                sigma);
        assignment = hung.execute();

        this.inverse = new int[n];
        for (int i = 0; i < n; i++)
            inverse[assignment[i]] = i;
    }

    public final int sigma(int i, int j) {
        return -sigma[i][j];
    }

}
