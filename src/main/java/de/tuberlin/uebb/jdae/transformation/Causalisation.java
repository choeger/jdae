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
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.ListenableDirectedGraph;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.traverse.GraphIterator;
import org.jgrapht.traverse.TopologicalOrderIterator;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import com.google.common.collect.UnmodifiableIterator;

import de.tuberlin.uebb.jdae.dae.Equation;
import de.tuberlin.uebb.jdae.dae.Unknown;
import de.tuberlin.uebb.jdae.thirdparty.HopcroftKarpBipartiteMatching;

/**
 * This class holds the causalisation algorithm. See {@link www.eoolt.org
 * /2011/presentations/presentation_13.pdf} for a presentation about the
 * concept.
 * 
 * @author Christoph Höger <christoph.hoeger@tu-berlin.de>
 * 
 */
public final class Causalisation {

    private final Logger logger;

    public Causalisation(final Logger l) {
        this.logger = l;
    }

    public static final class Computation {
        public final Unknown var;
        public final Equation eq;

        public Computation(Unknown var, Equation eq) {
            super();
            this.var = var;
            this.eq = eq;
        }

        public String toString() {
            return var + " := " + eq;
        }
    }

    public static class CausalisationResult {
        public final List<Computation> computations;
        public final List<Unknown> states;

        public CausalisationResult(List<Computation> computations,
                List<Unknown> states) {
            super();
            this.computations = computations;
            this.states = states;
        }
    }

    public static final List<IntegrationEquation> integrationEquations(
            Map<Unknown, Unknown> der) {
        final ImmutableList.Builder<IntegrationEquation> eq = ImmutableList
                .builder();
        for (Unknown v : der.keySet()) {
            eq.add(new IntegrationEquation(v));
        }
        return eq.build();
    }

    public Map<Unknown, Equation> matching(final Iterable<Equation> equations,
            final Map<Unknown, Unknown> der_map, final Map<Unknown, Unknown> repres) {

        final Function<Unknown, Unknown> der = Functions.forMap(der_map);

        final SimpleGraph<Object, DefaultEdge> g = new SimpleGraph<Object, DefaultEdge>(
                DefaultEdge.class);
        final Iterable<IntegrationEquation> integration = integrationEquations(der_map);

        for (Equation eq : equations) {
            g.addVertex(eq);

            for (Unknown v : eq.canSolveFor(der)) {
                final Unknown vtx = repres.containsKey(v) ? repres.get(v) : v;

                g.addVertex(vtx);
                g.addEdge(vtx, eq);
            }
        }

        final Map<Object, Object> index0integration = Maps.newHashMap();
        for (IntegrationEquation i : integration) {
            g.addVertex(i);
            final Unknown x = repres.containsKey(i.v) ? repres.get(i.v) : i.v;
            final Unknown dv = der_map.get(i.v);
            final Unknown dx = repres.containsKey(dv) ? repres.get(dv) : dv;

            g.addEdge(x, i);
            g.addEdge(dx, i);

            /* put the ideal result into our initial matching */
            index0integration.put(i, x);
        }

        logger.log(Level.INFO,
                "Running HopcroftKarp on {0} edges and {1} vertices",
                new Object[] { g.edgeSet().size(), g.vertexSet().size() });
        final long start = System.currentTimeMillis();

        final HopcroftKarpBipartiteMatching<Object, DefaultEdge> hopcroft = new HopcroftKarpBipartiteMatching<Object, DefaultEdge>(
                g, ImmutableSet.<Object> copyOf(Iterables.concat(equations,
                        integration)), ImmutableSet.<Object> copyOf(repres
                        .values()), index0integration);

        final Set<DefaultEdge> matching = hopcroft.getMatching();

        logger.log(Level.INFO, "HopcroftKarp done after {0}ms",
                System.currentTimeMillis() - start);

        final Map<Unknown, Equation> map = Maps.newHashMap();

        for (DefaultEdge edge : matching) {
            final Object edgeSource = g.getEdgeSource(edge);
            final Object edgeTarget = g.getEdgeTarget(edge);
            if (edgeSource instanceof Unknown && edgeTarget instanceof Equation) {
                map.put((Unknown) edgeSource, (Equation) edgeTarget);
            }
        }
        return map;
    }

    public final CausalisationResult causalise(
            final Map<Unknown, Equation> matching,
            final ImmutableMap<Unknown, Unknown> representatives,
            final Function<Unknown, Unknown> der) {
        final ListenableDirectedGraph<Object, DefaultEdge> g = new ListenableDirectedGraph<Object, DefaultEdge>(
                DefaultEdge.class);

        final ImmutableList.Builder<Unknown> states = ImmutableList.builder();

        /* first create a cycle free execution graph */
        for (Map.Entry<Unknown, Equation> e : matching.entrySet()) {
            final Equation eq = e.getValue();
            final Unknown var = e.getKey();

            if (eq instanceof IntegrationEquation) {
                /* ignore "correct" integrations */
                if (((IntegrationEquation) eq).v == var) {
                    states.add(var);
                    continue;
                }
            }

            g.addVertex(var);
            g.addVertex(eq);

            /* solutions */
            g.addEdge(eq, var);

            /* dependencies */
            for (Unknown v : eq.canSolveFor(der)) {
                if (v != var) {
                    final Unknown d = representatives.containsKey(v) ? representatives
                            .get(v) : v;
                    g.addVertex(d);
                    g.addEdge(d, eq);
                }
            }
        }

        // eraseCycles(g, g);

        final ImmutableList.Builder<Computation> comps = ImmutableList
                .builder();
        final GraphIterator<Object, DefaultEdge> iter = new TopologicalOrderIterator<Object, DefaultEdge>(
                g);

        final UnmodifiableIterator<Unknown> variables = Iterators.filter(iter,
                Unknown.class);

        while (variables.hasNext()) {
            final Unknown v = variables.next();
            final Equation eq = matching.get(v);

            if (eq instanceof IntegrationEquation)
                continue;

            /*
             * logger.log(Level.INFO, "Inspecting equation " + eq);
             * logger.log(Level.INFO, "Preparing computation of " + v);
             */
            if (!representatives.containsKey(v)) {
                comps.add(new Computation(v, eq));
            } else
                for (Unknown solver : eq.canSolveFor(der)) {
                    if (representatives.get(solver) == v) {
                        // TODO: check that it _really_ does not matter which
                        // one we
                        // choose!
                        comps.add(new Computation(solver, eq));
                        break;
                    }
                }
        }

        return new CausalisationResult(comps.build(), states.build());
    }
}
