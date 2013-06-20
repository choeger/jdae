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

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.collect.Lists;

import de.tuberlin.uebb.jdae.llmsl.GlobalEquation;
import de.tuberlin.uebb.jdae.llmsl.GlobalVariable;
import de.tuberlin.uebb.jdae.llmsl.specials.ConstantGlobalEquation;
import de.tuberlin.uebb.jdae.utils.HungarianIntegerAlgorithm;

public final class InitializationMatching {

    private final Causalisation causalisation;

    public int[] assignment;
    public int[] inverse;
    public List<DerivedEquation> allEquations;
    public GlobalVariable[] variables;

    public InitializationMatching(final Reduction reduction,
            final Causalisation causalisation, final Matching matching,
            final List<GlobalEquation> initialEquations,
            final Map<GlobalVariable, Double> startValues, final Logger logger) {

        this.causalisation = causalisation;

        allEquations = Lists.newArrayList();

        /*
         * Keep derived equations together. This ordering is important, since
         * InitializationCausalisation depends on it!
         */
        for (int i = 0; i < reduction.reduced.size(); i++) {
            for (int d = 0; d <= causalisation.eqn_derivatives[i]; d++) {
                allEquations.add(new DerivedEquation(reduction.reduced.get(i),
                        d));
            }
        }
        for (GlobalEquation ei : initialEquations) {
            allEquations.add(new DerivedEquation(ei, 0));
        }

        final int n = allEquations.size();
        variables = allocVariables();
        final int m = variables.length;

        if (n > m)
            throw new RuntimeException(
                    "Cannot (yet) initialize overdetermined system! (got " + n
                            + " equations and " + m + " variables)");

        final int[][] sigma = new int[m][n];
        for (int i = 0; i < n; i++) {
            final DerivedEquation eq = allEquations.get(i);
            for (GlobalVariable v : eq.eqn.need()) {
                final GlobalVariable der = v.der(eq.derOrder);
                final int j = numberOf(der);
                variables[j] = der;
                sigma[j][i] = -1;
            }
        }

        final HungarianIntegerAlgorithm hung = new HungarianIntegerAlgorithm(
                sigma);
        this.inverse = hung.execute();

        this.assignment = new int[m];
        for (int j = 0; j < m; j++) {
            final int i = inverse[j];
            if (i < 0) {
                final GlobalVariable v = variables[j];
                final double start = startValues.containsKey(v) ? startValues
                        .get(v) : 0.0;
                logger.log(Level.INFO, "Fixing start value for {0} = {1}",
                        new Object[] { v, start });
                allEquations.add((new ConstantGlobalEquation(v, start).der(0)));
                final int i2 = allEquations.size() - 1;
                assignment[i2] = j;
                inverse[j] = i2;
            } else {
                assignment[i] = j;
            }
        }

        final StringBuilder match = new StringBuilder();
        for (int i = 0; i < n; i++) {
            match.append(String.format("%s -> %s", variables[assignment[i]],
                    allEquations.get(i)));
            match.append(", ");
        }

        logger.log(Level.INFO, "Initial equation matching: {0}", match);

    }

    private GlobalVariable[] allocVariables() {
        final List<GlobalVariable> vars = Lists.newArrayList();
        for (int idx = 0; idx < causalisation.layout.length; idx++) {
            for (int d = 0; d <= causalisation.layout[idx].derOrder; d++)
                vars.add(new GlobalVariable(causalisation.layout[idx].name,
                        causalisation.layout[idx].number, d));
        }
        return vars.toArray(new GlobalVariable[vars.size()]);
    }

    public int numberOf(GlobalVariable v) {
        int number = v.der;
        for (int idx = 0; idx < v.index - 1; idx++) {
            number += 1 + causalisation.layout[idx].derOrder;
        }
        return number;
    }
}
