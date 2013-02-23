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

package de.tuberlin.uebb.jdae.dae;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math3.ode.sampling.StepHandler;
import org.apache.commons.math3.ode.sampling.StepInterpolator;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import de.tuberlin.uebb.jdae.simulation.ResultStorage.Step;
import de.tuberlin.uebb.jdae.simulation.SimulationOptions;
import de.tuberlin.uebb.jdae.transformation.Causalisation.Computation;

public final class SolvableDAE implements FirstOrderDifferentialEquations,
        Function<Unknown, Double> {

    public final static class SpecializedComputation {
        public final Unknown var;
        public final int target;
        public final Equation source;

        public SpecializedComputation(Computation comp, SolvableDAE system) {
            this.var = comp.var;
            this.source = comp.eq.specialize(system);
            this.target = system.variables.get(var);
        }
    }

    public final int dimension;

    public long evaluations = 0;
    public long simulation_time = -1;
    public long equation_instantiation_time = -1;
    public long object_instantiation_time = -1;
    public long set_time = -1;

    public double currentTime = 0.0;

    /* current simulation state */
    private double[] states;
    private double[] derivatives;
    public final double[] algebraics;
    public final ImmutableMap<Unknown, Integer> variables;

    public final SpecializedComputation[] specialComputations;
    private final Function<Unknown, Unknown> der;
    public final Logger logger;

    final class Observer implements StepHandler {

        final Collection<Unknown> observed;

        private Observer(Collection<Unknown> observed) {
            super();
            this.observed = observed;
        }

        @Override
        public void handleStep(StepInterpolator i, boolean isLast) {
            final double res[] = i.getInterpolatedState();
            for (Unknown v : observed) {
                final Integer integer = variables.get(v);
                logger.log(Level.INFO, "{0} \t {1} \t = {2}",
                        new Object[] {
                                i.getInterpolatedTime(),
                                v,
                                integer < dimension ? res[integer]
                                        : algebraics[integer - 2 * dimension] });
            }
        }

        @Override
        public void init(double arg0, double[] arg1, double arg2) {

        }

    };

    protected SolvableDAE(final List<Unknown> states,
            final List<Computation> causalisation,
            final Function<Unknown, Unknown> der,
            final ImmutableMap<Unknown, Integer> ordering, final Logger log) {
        this.logger = log;
        this.dimension = states.size();
        this.derivatives = new double[dimension];
        this.states = new double[dimension];
        this.algebraics = new double[ordering.size() - 2 * dimension];

        this.der = der;
        this.variables = ordering;

        this.specialComputations = new SpecializedComputation[causalisation
                .size()];
        for (int i = 0; i < causalisation.size(); i++) {
            specialComputations[i] = new SpecializedComputation(
                    causalisation.get(i), this);
        }
    }

    public SolvableDAE(final List<Unknown> states,
            ImmutableMap<Unknown, Unknown> representatives,
            final List<Computation> causalisation,
            Function<Unknown, Unknown> der, final Logger log) {
        this(states, causalisation, der, buildDefaultMapping(causalisation,
                states, der, representatives), log);
    }

    public static ImmutableMap<Unknown, Integer> buildDefaultMapping(
            final List<Computation> computations, final List<Unknown> states,
            Function<Unknown, Unknown> der,
            ImmutableMap<Unknown, Unknown> representatives) {
        final ImmutableMap.Builder<Unknown, Integer> b = ImmutableMap.builder();
        int i = 0;

        /* states */
        for (Unknown state : states)
            b.put(state, i++);

        /* derivatives */
        for (Unknown state : states)
            b.put(der.apply(state), i++);

        final ImmutableMap<Unknown, Integer> build = b.build();
        /*
         * the rest, i.e. algebraic variables guava does not allow builder
         * inspection, but since the computations are unique, it suffices to
         * filter out states and derivatives
         */
        for (Computation c : computations)
            if (!build.containsKey(c.var))
                b.put(c.var, i++);
        final ImmutableMap<Unknown, Integer> build2 = b.build();

        for (Unknown equi : representatives.keySet())
            if (!build2.containsKey(equi))
                b.put(equi, build2.get(representatives.get(equi)));

        return b.build();
    }

    @Override
    public void computeDerivatives(final double time, final double[] states,
            final double[] derivatives) {

        // final long start = System.nanoTime();
        this.currentTime = time;
        this.states = states;
        this.derivatives = derivatives;

        for (final SpecializedComputation s : specialComputations) {
            evaluations++;
            final double value = s.source.solveFor(s.target, s.var, this);
            set(s.target, value);
        }
        // simulation_time += (System.nanoTime() - start);
    }

    @Override
    public int getDimension() {
        return dimension;
    }

    @Override
    public Double apply(Unknown v) {
        final int index = variables.get(v);
        return get(index);
    }

    private final void set(final Unknown v, final double d) {
        final int index = variables.get(v);
        set(index, d);
    }

    public final double get(final int index) {
        if (index >= 2 * dimension) {
            return algebraics[index - 2 * dimension];
        } else if (index >= dimension) {
            return derivatives[index - dimension];
        } else {
            return states[index];
        }
    }

    private final void set(final int index, final double d) {
        if (index >= 2 * dimension) {
            algebraics[index - 2 * dimension] = d;
        } else if (index >= dimension) {
            derivatives[index - dimension] = d;
        } else {
            states[index] = d;
        }
    }

    public final Map<Unknown, Double> integrate(
            final SimulationOptions options, Map<String, Double> initial_values) {
        final List<Unknown> observables = Lists.newArrayList();

        for (Unknown v : variables.keySet()) {
            if (initial_values.containsKey(v.toString())) {
                set(v, initial_values.get(v.toString()));
                observables.add(v);
            }
        }

        logger.log(Level.INFO, "Done with initial values.");
        // integrator.addStepHandler(new Observer(observables));
        options.integrator.integrate(this, options.startTime, states,
                options.stopTime, states);

        final ImmutableMap.Builder<Unknown, Double> results = ImmutableMap
                .builder();
        for (Unknown v : variables.keySet())
            results.put(v, apply(v));
        return results.build();
    }

    public double valueAt(Step step, Unknown v) {
        final int index = variables.get(v);

        if (index >= 2 * dimension) {
            return step.algebraics[index - 2 * dimension];
        } else if (index >= dimension) {
            return step.derivatives[index - dimension];
        } else {
            return step.states[index];
        }
    }
}
