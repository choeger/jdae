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

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.math3.optim.linear.LinearConstraint;
import org.apache.commons.math3.optim.linear.SimplexSolver;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import de.tuberlin.uebb.jdae.llmsl.DataLayout.VariableRow;
import de.tuberlin.uebb.jdae.llmsl.GlobalEquation;
import de.tuberlin.uebb.jdae.llmsl.GlobalVariable;
import de.tuberlin.uebb.jdae.thirdparty.transitivityutils.Relations;
import de.tuberlin.uebb.jdae.thirdparty.transitivityutils.TransitiveRelation;

/**
 * This class holds the causalisation algorithm. See {@link www.eoolt.org
 * /2011/presentations/presentation_13.pdf} for a presentation about the
 * concept.
 * 
 * @author Christoph Höger <christoph.hoeger@tu-berlin.de>
 * 
 */
public final class Causalisation {

    public final List<Set<Integer>> computations;
    public final List<Set<GlobalVariable>> iteratees;
    public final int[] eqn_derivatives;

    public final VariableRow[] layout;

    private SimplexSolver solver = new SimplexSolver();
    public final List<GlobalVariable> states;
    public final GlobalEquation[] equations;

    private final Matching matching;
    private final static Logger logger = Logger.getLogger("Pryce' Method");

    public Causalisation(final Reduction reduction, final Matching matching) {

        final List<LinearConstraint> constraints = Lists.newArrayList();
        final Map<Integer, String> names = reduction.names;
        this.matching = matching;

        final int n = reduction.reduced.size();
        equations = reduction.reduced.toArray(new GlobalEquation[n]);

        final int[] point = solveByFixedPoint();

        logger.log(Level.INFO, "Result of Fixedpoint: {0}",
                Arrays.toString(point));

        final TransitiveRelation<Integer> depends = Relations
                .newTransitiveRelation();

        this.layout = new VariableRow[n];
        this.eqn_derivatives = new int[n];

        for (int i = 0; i < n; i++) {
            layout[i] = new VariableRow(i + 1, (int) point[i], names.get(i + 1));
            eqn_derivatives[i] = (int) point[n + i];
            final GlobalEquation eqn = reduction.reduced.get(i);

            for (GlobalVariable v : eqn.need()) {
                final int j = v.index - 1;
                /* maximal degree of derivation of v in eqn */
                final int max = matching.sigma(i, j);

                /* equation computing v */
                final int other = matching.inverse[j];

                /*
                 * if eqn only uses a lower derivative, there is no dependency
                 * (the lower derivative is integrated)
                 */
                if (max >= matching.sigma(i, j)) {
                    depends.relate(i, other);
                }
            }
        }

        states = Lists.newArrayList();
        final List<Integer> eqns = Lists.newArrayListWithCapacity(n);

        setupStates(matching, n, states, eqns);
        logger.log(Level.INFO, "States: {0}", Joiner.on(", ").join(states));
        final Comparator<Integer> comp = sortByDependency(depends, eqns);

        computations = Lists.newArrayList();
        iteratees = Lists.newArrayList();

        logger.log(Level.INFO, "Starting dependency analysis.");

        int k = 0;
        while (k < n) {
            final Set<GlobalVariable> blockVars = Sets.newTreeSet();
            final Set<Integer> blockEqns = Sets.newTreeSet();

            final Integer eq = eqns.get(k);
            int l = 0;
            while ((k + l) < n && comp.compare(eq, eqns.get(k + l)) == 0) {
                final int i = eqns.get(k + l);
                final int j = matching.assignment[i];

                final int c = (int) point[n + i];

                blockEqns.add(i);

                addIterationVariables(matching, blockVars, i, j, c);

                l++;
            }
            k += l;

            logger.log(Level.INFO,
                    "Simulation block of {0} equation(s) computing {1}",
                    new Object[] { blockEqns.size(), blockVars });
            computations.add(blockEqns);
            iteratees.add(blockVars);
        }
    }

    private void addIterationVariables(final Matching matching,
            final Set<GlobalVariable> blockVars, final int i, final int j,
            final int c) {
        int d = 0;
        while (d <= c)
            blockVars.add(new GlobalVariable(layout[j].name, j + 1, matching
                    .sigma(i, j) + (d++)));
    }

    private void setupStates(final Matching matching, final int n,
            final List<GlobalVariable> states, final List<Integer> eqns) {
        for (int j = 0; j < n; j++) {
            final int i = matching.inverse[j];
            final int sigma = matching.sigma(i, j);
            int d = 0;
            while (d < sigma) {
                states.add(new GlobalVariable(layout[j].name, j + 1, d++));
            }
            eqns.add(i);
        }
    }

    private Comparator<Integer> sortByDependency(
            final TransitiveRelation<Integer> depends, final List<Integer> eqns) {
        final Comparator<Integer> comp = new Comparator<Integer>() {

            @Override
            public int compare(Integer a, Integer b) {
                final boolean a_dep_b = depends.areRelated(a, b);
                final boolean b_dep_a = depends.areRelated(b, a);
                if (a_dep_b && b_dep_a)
                    return 0;
                else if (a_dep_b) {
                    return -1;
                } else {
                    return 1;
                }
            }

        };

        Collections.sort(eqns, comp);
        return comp;
    }

    private final int[] solveByFixedPoint() {
        final int n = equations.length;
        final int cd[] = new int[2 * n];
        boolean converged = false;

        while (!converged) {
            converged = true;
            for (int j = 0; j < n; j++) {
                int max = 0;
                for (int i = 0; i < n; i++) {
                    max = Math.max(max, this.matching.sigma(i, j) + cd[i + n]);
                }
                cd[j] = max;
            }

            for (int i = 0; i < n; i++) {
                final int j = matching.assignment[i];
                final int c2 = cd[j] - this.matching.sigma(i, j);

                if (cd[i + n] != c2)
                    converged = false;

                cd[i + n] = c2;
            }
        }

        return cd;
    }
}
