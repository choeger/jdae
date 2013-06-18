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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;

import de.tuberlin.uebb.jdae.hlmsl.Equation;
import de.tuberlin.uebb.jdae.hlmsl.Unknown;
import de.tuberlin.uebb.jdae.hlmsl.specials.Equality;
import de.tuberlin.uebb.jdae.llmsl.GlobalEquation;
import de.tuberlin.uebb.jdae.llmsl.GlobalVariable;
import de.tuberlin.uebb.jdae.thirdparty.transitivityutils.Navigator;
import de.tuberlin.uebb.jdae.thirdparty.transitivityutils.Navigators;
import de.tuberlin.uebb.jdae.thirdparty.transitivityutils.Relations;
import de.tuberlin.uebb.jdae.thirdparty.transitivityutils.TransitiveRelation;

public final class Reduction {

    public final List<GlobalEquation> reduced;
    public final Set<GlobalVariable> unknowns;
    public final Map<Unknown, GlobalVariable> ctxt;
    public Map<Integer, String> names;

    public Reduction(final Collection<Equation> equations) {
        final ImmutableList.Builder<GlobalEquation> be = ImmutableList
                .builder();

        this.names = Maps.newTreeMap();

        final Map<Unknown, Integer> layout = Maps.newTreeMap();

        final TransitiveRelation<Unknown> equivalent = Relations
                .newTransitiveRelation();

        for (Equation eq : equations) {
            for (Unknown unknown : eq.unknowns()) {
                final Unknown base = unknown.base();
                final Integer order = layout.get(base);
                if (order == null || (unknown.der > order))
                    layout.put(base, unknown.der);
            }

            if (eq instanceof Equality) {
                final Unknown l = ((Equality) eq).lhs;
                final Unknown r = ((Equality) eq).rhs;
                Unknown min = Ordering.natural().min(l, r);
                Unknown max = Ordering.natural().max(l, r);

                if (min.der == 0) {
                    equivalent.relate(min, max);
                }
            }
        }

        final Navigator<Unknown> direct = equivalent.direct();
        final Map<Unknown, Unknown> repres = Maps.newTreeMap();

        for (Unknown base : layout.keySet()) {
            final Unknown repr = Collections.max(
                    Navigators.closure(direct, base), Ordering.natural());

            for (int i = 0; i <= layout.get(base); i++) {
                repres.put(base.der(i), repr.der(i));
            }
        }

        final Function<Unknown, GlobalVariable> pack_dense = new Function<Unknown, GlobalVariable>() {
            final Map<Integer, Integer> mem = Maps.newTreeMap();

            public GlobalVariable apply(Unknown u) {
                if (!mem.containsKey(u.nr)) {
                    mem.put(u.nr, (mem.size() + 1));
                    names.put(mem.get(u.nr), u.name);
                }
                return new GlobalVariable(u.name, mem.get(u.nr), u.der);
            }
        };

        ctxt = Maps.transformValues(repres, pack_dense);

        for (Equation eq : equations) {
            if (eq instanceof Equality) {
                final Unknown l = ((Equality) eq).lhs;
                final Unknown r = ((Equality) eq).rhs;
                final Unknown min = Ordering.natural().min(l, r);

                if (min.der != 0)
                    be.add(eq.bind(ctxt));
            } else {
                be.add(eq.bind(ctxt));
            }
        }

        reduced = be.build();

        this.unknowns = ImmutableSortedSet.copyOf(ctxt.values());

    }
}
