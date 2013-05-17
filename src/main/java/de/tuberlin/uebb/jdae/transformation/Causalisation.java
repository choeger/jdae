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

import org.jgrapht.alg.StrongConnectivityInspector;
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
import com.google.common.collect.Maps;

import de.tuberlin.uebb.jdae.dae.Equation;
import de.tuberlin.uebb.jdae.dae.Unknown;
import de.tuberlin.uebb.jdae.simulation.DerivativeRelation;
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
        public final Unknown computed;
        public final Equation eq;
        public final int der_index;

        public Computation(Unknown var, Equation eq) {
            super();
            this.der_index = 0;
            this.computed = var;
            this.var = var;
            this.eq = eq;
        }

        /**
         * Algebraic derivative computation
         * 
         * @param var
         * @param dv
         * @param eq
         * @param der
         */
        public Computation(Unknown var, Unknown dv, Equation eq, int der) {
            super();
            this.der_index = der;
            this.computed = dv;
            this.var = var;
            this.eq = eq;
        }

        public String toString() {
            return var + "(dt^" + der_index + ") := " + eq;
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
            final Map<Unknown, Unknown> der_map,
            final Map<Unknown, Unknown> repres) {

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

            /* the unknowns might not be in the graph yet */
            g.addVertex(x);
            g.addVertex(dx);

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
            final DerivativeRelation der) {

        final ListenableDirectedGraph<Equation, DefaultEdge> g = new ListenableDirectedGraph<Equation, DefaultEdge>(
                DefaultEdge.class);

        final ImmutableList.Builder<Unknown> states = ImmutableList.builder();
        final Map<Equation, Unknown> inverseM = Maps.newHashMap();
        final Map<Equation, Integer> derOrder = Maps.newHashMap();

        /* first create an integration free execution graph */
        for (Map.Entry<Unknown, Equation> e : matching.entrySet()) {
            final Equation eq = e.getValue();
            final Unknown var = e.getKey();

            if (eq instanceof IntegrationEquation) {
                /* ignore "correct" integrations */
                final Unknown integrated = ((IntegrationEquation) eq).v;
                if (integrated == var) {
                    states.add(var);
                } else {
                    /* "wrong" integration */
                    final Equation equation = matching.get(integrated); // get
                                                                        // source
                                                                        // equation
                    derOrder.put(equation, 1); // increase derivation of source
                                               // equation
                }
                continue;
            }

            inverseM.put(eq, var);
            g.addVertex(eq);

            /* dependencies */
            for (Unknown v : eq.canSolveFor(der)) {
                if (v != var) {
                    final Unknown d = representatives.containsKey(v) ? representatives
                            .get(v) : v;
                    final Equation pred = matching.get(d);

                    /* The predecessor may be an integration equation */
                    if (pred instanceof IntegrationEquation) {
                        final IntegrationEquation ieq = (IntegrationEquation) pred;
                        if (ieq.v == v) {
                            // state, do nothing
                            continue;
                        } else {
                            // dummy derivative add dependency on predecessor
                            // TODO: may _this_ be again an integration
                            // equation?
                            final Equation stateCalc = matching.get(ieq.v);
                            g.addVertex(stateCalc);
                            g.addEdge(stateCalc, eq);
                        }
                    } else {
                        g.addVertex(pred);
                        g.addEdge(pred, eq);
                    }
                }
            }
        }

        /* remove cycles */

        final StrongConnectivityInspector<Equation, DefaultEdge> conn = new StrongConnectivityInspector<>(
                g);

        final List<Set<Equation>> sets = conn.stronglyConnectedSets();

        for (Set<Equation> eqs : sets) {
            if (eqs.size() > 1
                    || selfDepends(eqs.iterator().next(),
                            inverseM.get(eqs.iterator().next()),
                            representatives, der, derOrder))
                ;
            throw new RuntimeException(
                    "Sorry, algebraic loops are not yet implemented!");
        }

        /* sanitize derivative dependencies */
        for (Equation derEq : derOrder.keySet()) {
            markDerivation(derEq, g, derOrder, inverseM);
        }

        final ImmutableList.Builder<Computation> comps = ImmutableList
                .builder();

        final GraphIterator<Equation, DefaultEdge> iter = new TopologicalOrderIterator<Equation, DefaultEdge>(
                g);

        while (iter.hasNext()) {
            final Equation eq = iter.next();

            /*
             * logger.log(Level.INFO, "Inspecting equation " + eq);
             * logger.log(Level.INFO, "Preparing computation of " + v);
             */
            final Unknown v = inverseM.get(eq);
            if (!representatives.containsKey(v)) {
                comps.add(new Computation(v, eq));
            } else
                for (Unknown solver : eq.canSolveFor(der)) {
                    if (representatives.get(solver) == v) {
                        // TODO: check that it _really_ does not matter which
                        // one we
                        // choose!
                        if (derOrder.containsKey(eq)
                                && der.domain().contains(v)) {
                            final Unknown dv = der.apply(v);
                            der.remove(v);
                            comps.add(new Computation(solver, dv, eq, 1));
                        }
                        comps.add(new Computation(solver, eq));
                        break;
                    }
                }
        }

        return new CausalisationResult(comps.build(), states.build());
    }

    /**
     * check, whether eq depends on the derivative of the variable it shall
     * compute (indirectly)
     * 
     * @param eq
     * @param x
     * @param representatives
     * @param der
     * @param derOrder
     * @return
     */
    private boolean selfDepends(Equation eq, Unknown x,
            ImmutableMap<Unknown, Unknown> representatives,
            DerivativeRelation der, Map<Equation, Integer> derOrder) {

        if (der.domain().contains(x) && derOrder.containsKey(eq)
                && derOrder.get(eq) >= 1) {
            for (Unknown u : eq.canSolveFor(der)) {
                if (der.domain().contains(u)) {
                    if (representatives.get(der.apply(u)) == representatives
                            .get(der.apply(x)))
                        return true;
                }
            }
        }
        return false;
    }

    private void markDerivation(Equation derEq,
            ListenableDirectedGraph<Equation, DefaultEdge> g,
            Map<Equation, Integer> derOrder, Map<Equation, Unknown> inverseM) {

        for (DefaultEdge edge : g.incomingEdgesOf(derEq)) {
            final Equation succ = g.getEdgeSource(edge);
            final int orderSucc = derOrder.containsKey(succ) ? derOrder
                    .get(succ) + 1 : 1;
            derOrder.put(succ, orderSucc);
            markDerivation(succ, g, derOrder, inverseM);
        }

    }
}
