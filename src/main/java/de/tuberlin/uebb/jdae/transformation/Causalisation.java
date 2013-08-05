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

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;

import de.tuberlin.uebb.jdae.llmsl.DataLayout.VariableRow;
import de.tuberlin.uebb.jdae.llmsl.GlobalEquation;
import de.tuberlin.uebb.jdae.llmsl.GlobalVariable;
import de.tuberlin.uebb.jdae.thirdparty.transitivityutils.Relations;
import de.tuberlin.uebb.jdae.thirdparty.transitivityutils.TransitiveRelation;
import de.tuberlin.uebb.jdae.utils.IntPair;

/**
 * This class holds the causalisation algorithm. See {@link www.eoolt.org
 * /2011/presentations/presentation_13.pdf} for a presentation about the
 * concept.
 * 
 * @author Christoph Höger <christoph.hoeger@tu-berlin.de>
 * 
 */
public final class Causalisation {

    public final List<TIntObjectMap<Range<Integer>>> computations;
    public final List<Set<GlobalVariable>> iteratees;
    public final int[] eqn_derivatives;

    public final VariableRow[] layout;

    public final List<GlobalVariable> states;
    public final GlobalEquation[] equations;

    private final Matching matching;
    private final static Logger logger = Logger.getLogger("Pryce' Method");

    public Causalisation(final Reduction reduction, final Matching matching) {

        final Map<Integer, String> names = reduction.names;
        this.matching = matching;

        final int n = reduction.reduced.size();
        equations = reduction.reduced.toArray(new GlobalEquation[n]);

        final int[] point = solveByFixedPoint();

        logger.log(Level.INFO, "Result of Fixedpoint: {0}",
                Arrays.toString(point));

        final TransitiveRelation<IntPair> depends = Relations
                .newTransitiveRelation();

        this.layout = new VariableRow[n];
        this.eqn_derivatives = new int[n];

        for (int i = 0; i < n; i++) {
            layout[i] = new VariableRow(i + 1, point[i], names.get(i + 1));
            eqn_derivatives[i] = (int) point[n + i];
            final GlobalEquation eqn = reduction.reduced.get(i);

            for (GlobalVariable v : eqn.need()) {
                final int j = v.index - 1;

                for (int d = 0; d <= point[n + i]; d++) {
                    /* maximal degree of derivation of v in eqn */
                    final int max = matching.sigma(i, j) + d;

                    /* equation computing v */
                    final int other = matching.inverse[j];

                    for (int od = 0; od <= point[n + other]; od++) {
                        /* maximal degree of derivation of v in other */
                        final int otherMax = matching.sigma(other, j) + od;

                        /*
                         * if eqn only uses a lower derivative, there is no
                         * dependency (the lower derivative is integrated)
                         */

                        if (max >= otherMax) {
                            depends.relate(new IntPair(i, d), new IntPair(
                                    other, od));
                        }
                    }
                }
            }
        }

        states = Lists.newArrayList();
        final List<IntPair> eqns = Lists.newArrayList();

        setupStates(matching, n, states, eqns);
        logger.log(Level.INFO, "States: {0}", Joiner.on(", ").join(states));
        final Comparator<IntPair> comp = sortByDependency(depends, eqns);

        computations = Lists.newArrayList();
        iteratees = Lists.newArrayList();

        logger.log(Level.INFO, "Starting dependency analysis.");

        final Iterator<IntPair> k = eqns.iterator();
        if (k.hasNext())
            collectBlocks(matching, comp, k.next(), k);
    }

    private final void collectBlocks(final Matching matching,
            final Comparator<IntPair> comp, final IntPair componentStart,
            final Iterator<IntPair> k) {
        final Set<GlobalVariable> blockVars = Sets.newTreeSet();
        final TIntObjectMap<Range<Integer>> blockEqns = new TIntObjectHashMap<>();

        collectEquation(matching, componentStart, blockVars, blockEqns);

        IntPair eqNext = null;
        while (k.hasNext()
                && (comp.compare(componentStart, (eqNext = k.next())) == 0)) {
            collectEquation(matching, eqNext, blockVars, blockEqns);
        }

        logger.log(Level.INFO,
                "Simulation block of {0} equation(s) computing {1}",
                new Object[] { blockEqns.size(), blockVars });

        computations.add(blockEqns);
        iteratees.add(blockVars);

        if (eqNext != null)
            collectBlocks(matching, comp, eqNext, k);
    }

    public void collectEquation(final Matching matching, IntPair eqNext,
            final Set<GlobalVariable> blockVars,
            final TIntObjectMap<Range<Integer>> blockEqns) {
        final int i = eqNext.x;
        final int j = matching.assignment[i];

        final int c = eqNext.y;

        if (!blockEqns.containsKey(i))
            blockEqns.put(i, Range.closed(c, c));
        else {
            final Range<Integer> next = Range.closed(c, c);
            final Range<Integer> last = blockEqns.get(i);

            blockEqns.put(i, next.intersection(last));
        }

        blockVars.add(new GlobalVariable(layout[j].name, j + 1, matching.sigma(
                i, j) + c));
    }

    private void setupStates(final Matching matching, final int n,
            final List<GlobalVariable> states, final List<IntPair> eqns) {
        for (int j = 0; j < n; j++) {
            final int i = matching.inverse[j];
            final int sigma = matching.sigma(i, j);
            int d = 0;
            while (d < sigma) {
                states.add(new GlobalVariable(layout[j].name, j + 1, d++));

            }
            for (int der = 0; der <= eqn_derivatives[i]; der++) {
                eqns.add(new IntPair(i, der));
            }
        }
    }

    private Comparator<IntPair> sortByDependency(
            final TransitiveRelation<IntPair> depends, final List<IntPair> eqns) {
        final Comparator<IntPair> comp = new Comparator<IntPair>() {

            @Override
            public int compare(IntPair a, IntPair b) {
                final boolean a_dep_b = depends.areRelated(a, b);
                final boolean b_dep_a = depends.areRelated(b, a);
                if (a_dep_b && b_dep_a)
                    return 0;
                else if (a_dep_b) {
                    return 1;
                } else {
                    return -1;
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
