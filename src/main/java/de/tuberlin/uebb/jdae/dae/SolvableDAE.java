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

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import de.tuberlin.uebb.jdae.builtins.StateEquation;
import de.tuberlin.uebb.jdae.simulation.ResultStorage.Step;
import de.tuberlin.uebb.jdae.simulation.SimulationOptions;
import de.tuberlin.uebb.jdae.simulation.SimulationRuntime;
import de.tuberlin.uebb.jdae.transformation.Causalisation.Computation;

public final class SolvableDAE implements FirstOrderDifferentialEquations {

    /**
     * A plain copy from a state to a derivative does not require any
     * computation. It is necessary, though, because Java arrays must not
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

    public final FunctionalEquation[] computationalOrder;

    private FunctionalEquation[] states;
    private FunctionalEquation[] derivatives;
    public final FunctionalEquation[] algebraics;

    public double stateVector[];
    public double time;

    public final ImmutableMap<Unknown, Integer> variables;

    public final Logger logger;
    public boolean storeResultFlag = false;
    public final SimulationRuntime runtime;

    /* inverse of the absolute event detection precision */
    public double eventPrecisionInv = 10e6; // TODO: find a way to make this

    // final

    protected SolvableDAE(final List<Unknown> states,
            final List<Computation> causalisation,
            final SimulationRuntime runtime,
            final ImmutableMap<Unknown, Integer> ordering, final Logger log) {
        this.logger = log;
        this.dimension = states.size();
        this.runtime = runtime;

        this.derivatives = new FunctionalEquation[dimension];
        this.states = new FunctionalEquation[dimension];
        this.algebraics = new FunctionalEquation[ordering.size() - 2
                * dimension];

        this.variables = ordering;

        this.computationalOrder = new FunctionalEquation[causalisation.size()];

        for (int s = 0; s < dimension; s++)
            this.states[s] = new StateEquation(s, this);

        /*
         * In case a derivative is equivalent to a state, the state will get
         * selected as representative. Therefore, the derivative is the same
         * function as the state.
         */
        for (Unknown state : states) {
            final Unknown der = runtime.der().apply(state);
            final Integer source = variables.get(der);
            if (source < dimension) {
                /* the derivative has the index of the state in the der-vector! */
                derivatives[variables.get(state)] = get(source);
            }
        }

        for (int i = 0; i < causalisation.size(); i++) {
            final Computation c = causalisation.get(i);
            computationalOrder[i] = c.eq.specializeFor(c.var, this);
            set(variables.get(c.var), computationalOrder[i]);
        }
    }

    public SolvableDAE(final List<Unknown> states,
            ImmutableMap<Unknown, Unknown> representatives,
            final List<Computation> causalisation, SimulationRuntime runtime,
            final Logger log) {
        this(states, causalisation, runtime, buildDefaultMapping(causalisation,
                states, runtime, representatives), log);
    }

    public static ImmutableMap<Unknown, Integer> buildDefaultMapping(
            final List<Computation> computations, final List<Unknown> states,
            SimulationRuntime runtime,
            ImmutableMap<Unknown, Unknown> representatives) {
        final Map<Unknown, Integer> b = Maps.newHashMap();
        int i = 0;
        final int dimension = states.size();

        /* states */
        for (Unknown state : states)
            b.put(state, i++);

        /* derivatives */
        for (Unknown state : states) {
            final Unknown d = runtime.der().apply(state);
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

        this.stateVector = states;

        for (int i = 0; i < dimension; i++) {
            derivatives[i] = this.derivatives[i].value(time);
        }

        if (storeResultFlag) {
            runtime.lastResults().addResult(time, derivatives, states);
        }
        // simulation_time += (System.nanoTime() - start);
    }

    public void computeDerivativesUpto(final int limit, final double time,
            final double[] states, final double[] derivatives) {

        this.stateVector = states;

        for (FunctionalEquation eq : computationalOrder) {
            eq.value(time);
            if (eq.unknown() == limit)
                break;
        }
    }

    @Override
    public int getDimension() {
        return dimension;
    }

    public final FunctionalEquation get(final Unknown u) {
        final int index = variables.get(u);
        return get(index);
    }

    public final FunctionalEquation get(final int index) {
        if (index >= 2 * dimension) {
            return algebraics[index - 2 * dimension];
        } else if (index >= dimension) {
            return derivatives[index - dimension];
        } else {
            return states[index];
        }
    }

    private void set(int index, FunctionalEquation f) {
        if (index >= 2 * dimension) {
            algebraics[index - 2 * dimension] = f;
        } else if (index >= dimension) {
            derivatives[index - dimension] = f;
        } else {
            throw new RuntimeException(
                    "Trying to set a computation for a state!");
        }
    }

    public final Map<Unknown, Double> integrate(final SimulationOptions options) {
        final List<Unknown> observables = Lists.newArrayList();

        time = options.startTime;
        stateVector = new double[dimension];

        for (Unknown v : variables.keySet()) {
            final Integer v_i = variables.get(v);
            if (v_i < dimension) {
                if (options.initialValues.containsKey(v.toString())) {
                    stateVector[v_i] = options.initialValues.get(v.toString());
                    observables.add(v);
                }
            }
        }

        logger.log(Level.INFO, "Done with initial values.");
        // integrator.addStepHandler(new Observer(observables));

        while (time < options.stopTime) {
            time = options.integrator.integrate(this, time, stateVector,
                    options.stopTime, stateVector);
        }

        final ImmutableMap.Builder<Unknown, Double> results = ImmutableMap
                .builder();
        for (Unknown v : variables.keySet())
            results.put(v, get(v).value(time));

        runtime.lastResults().addResult(options.stopTime, derVector(time),
                stateVector);

        return results.build();
    }

    private double[] derVector(double time) {
        double[] vector = new double[dimension];
        for (int i = 0; i < dimension; i++)
            vector[i] = derivatives[i].value(time);
        return vector;
    }

    public double value(int i, double time) {
        return get(i).value(time);
    }

    public double value(Unknown u, double time) {
        return get(variables.get(u)).value(time);
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
        this.stateVector = vars;
        this.time = t;
    }
}
