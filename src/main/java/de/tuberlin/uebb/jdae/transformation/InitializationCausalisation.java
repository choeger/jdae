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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import de.tuberlin.uebb.jdae.llmsl.GlobalEquation;
import de.tuberlin.uebb.jdae.llmsl.GlobalVariable;
import de.tuberlin.uebb.jdae.thirdparty.transitivityutils.Relations;
import de.tuberlin.uebb.jdae.thirdparty.transitivityutils.TransitiveRelation;

public final class InitializationCausalisation {

    public final List<Set<DerivedEquation>> computations;
    public final List<Set<GlobalVariable>> iteratees;

    public InitializationCausalisation(final InitializationMatching matching,
            final Logger logger) {
        logger.log(Level.INFO, "Starting initial dependency analysis.");

        final TransitiveRelation<Integer> depends = Relations
                .newTransitiveRelation();

        final List<Integer> sorted = Lists.newArrayList();
        final int n = matching.assignment.length;
        final List<DerivedEquation> eqns = matching.allEquations;

        /* ensure derived equations are in same block as their originals */
        for (int i = 1; i < n; i++) {
            if (eqns.get(i).eqn == eqns.get(i - 1).eqn) {
                depends.relate(i, i - 1);
                depends.relate(i - 1, i);
            }
        }

        for (int i = 0; i < n; i++) {
            for (GlobalVariable v : eqns.get(i).need()) {
                final int j = matching.inverse[matching.numberOf(v)];
                depends.relate(i, j);
            }
            sorted.add(i);
        }

        final Comparator<Integer> depCompare = new Comparator<Integer>() {
            @Override
            public int compare(Integer a, Integer b) {
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

        Collections.sort(sorted, depCompare);

        computations = Lists.newArrayList();
        iteratees = Lists.newArrayList();

        int k = 0;
        while (k < n) {
            final Set<GlobalVariable> blockVars = Sets.newTreeSet();
            final Map<GlobalEquation, DerivedEquation> blockEqns = Maps
                    .newHashMap();

            final Integer eq = sorted.get(k);
            int l = 0;
            while ((k + l) < n
                    && depCompare.compare(eq, sorted.get(k + l)) == 0) {
                final int i = sorted.get(k + l);
                final int j = matching.assignment[i];

                final DerivedEquation derivedEquation = eqns.get(i);
                if (blockEqns.containsKey(derivedEquation.eqn)
                        && blockEqns.get(derivedEquation.eqn).derOrder > derivedEquation.derOrder) {
                    // Do nothing
                } else {
                    blockEqns.put(derivedEquation.eqn, derivedEquation);
                }
                blockVars.add(matching.variables[j]);
                l++;
            }
            k += l;

            logger.log(Level.INFO,
                    "Initialization block of {0} equation(s) computing {1}",
                    new Object[] { blockEqns.size(), blockVars });

            computations.add(ImmutableSet.copyOf(blockEqns.values()));
            iteratees.add(blockVars);
        }

    }
}
