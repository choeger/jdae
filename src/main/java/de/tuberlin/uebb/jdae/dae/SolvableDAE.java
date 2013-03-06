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
import com.google.common.collect.Maps;

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

    /**
     * A plain copy from a state to a derivative does not require any
     * computation. It is necessary, though, because Java arrays may not
     * overlap.
     * 
     * @author choeger
     * 
     */
    public final static class CopyOp {
        /**
         * The index of the state variable.
         */
        public final int source;

        /**
         * The index of the derivative variable. Note: This is the index in the
         * derivatives array and not the index in the system!
         */
        public final int target;

        public CopyOp(int target, int source) {
            super();
            this.target = target;
            this.source = source;
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

    private final CopyOp[] copyOperations;

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

        /*
         * In case a derivative is equivalent to a state, the state will get
         * selected as representative. Therefore, we have to copy the state's
         * value to the derivative during der-computation to not confuse the
         * solver.
         */
        final List<CopyOp> copyOps = Lists.newArrayList();
        for (Unknown state : states) {
            final Integer source = variables.get(der.apply(state));
            if (source < dimension) {
                /*
                 * The derivative has the index of the state variable in the
                 * derivative-array
                 */
                copyOps.add(new CopyOp(variables.get(state), source));
            }
        }
        this.copyOperations = copyOps.toArray(new CopyOp[copyOps.size()]);

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
        final Map<Unknown, Integer> b = Maps.newHashMap();
        int i = 0;
        final int dimension = states.size();

        /* states */
        for (Unknown state : states)
            b.put(state, i++);

        /* derivatives */
        for (Unknown state : states) {
            final Unknown d = der.apply(state);
            final Unknown drep = representatives.get(d);
            final Integer k = b.get(drep);

            /*
             * Use a representatives key, if necessary.
             */
            if (k != null) {
                b.put(d, k);
            } else {
                b.put(d, b.get(state) + dimension);
            }
        }

        i = 0;
        /*
         * the rest, i.e. algebraic since the computations are unique, it
         * suffices to filter out states and derivatives
         */
        for (Computation c : computations)
            if (!b.containsKey(c.var))
                b.put(c.var, (2 * dimension + i++));

        /*
         * Everything, that does not need to be computed
         */
        for (Unknown equi : representatives.keySet())
            if (!b.containsKey(equi))
                b.put(equi, b.get(representatives.get(equi)));

        return ImmutableMap.copyOf(b);
    }

    @Override
    public void computeDerivatives(final double time, final double[] states,
            final double[] derivatives) {

        // logger.log(Level.INFO, "Evaluating system at t={0}", time);
        // final long start = System.nanoTime();
        this.currentTime = time;
        this.states = states;
        this.derivatives = derivatives;

        /*
         * Do the copying for equivalences between states and ders, The
         * integrator has calculated all states, so do that first!
         */
        for (CopyOp copy : copyOperations) {
            derivatives[copy.target] = states[copy.source];
        }

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

    public final Map<Unknown, Double> integrate(final SimulationOptions options) {
        final List<Unknown> observables = Lists.newArrayList();

        for (Unknown v : variables.keySet()) {
            if (options.initialValues.containsKey(v.toString())) {
                set(v, options.initialValues.get(v.toString()));
                observables.add(v);
            }
        }

        logger.log(Level.INFO, "Done with initial values.");
        // integrator.addStepHandler(new Observer(observables));

        double t = options.startTime;
        while (t < options.stopTime) {
            t = options.integrator.integrate(this, t, states, options.stopTime,
                    states);
        }

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

    public void stop(double t, double[] vars) {
        this.states = vars;
        this.currentTime = t;
    }
}
