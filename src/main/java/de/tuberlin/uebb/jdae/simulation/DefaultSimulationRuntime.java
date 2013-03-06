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
package de.tuberlin.uebb.jdae.simulation;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.math3.ode.FirstOrderIntegrator;
import org.apache.commons.math3.ode.events.EventHandler;
import org.apache.commons.math3.ode.nonstiff.DormandPrince54Integrator;
import org.apache.commons.math3.ode.nonstiff.EulerIntegrator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import de.tuberlin.uebb.jdae.builtins.DerivativeCollector;
import de.tuberlin.uebb.jdae.builtins.SpecializedConstantLinearEquation;
import de.tuberlin.uebb.jdae.dae.Equality;
import de.tuberlin.uebb.jdae.dae.Equation;
import de.tuberlin.uebb.jdae.dae.SolvableDAE;
import de.tuberlin.uebb.jdae.dae.Unknown;
import de.tuberlin.uebb.jdae.transformation.Causalisation;
import de.tuberlin.uebb.jdae.transformation.Causalisation.CausalisationResult;
import de.tuberlin.uebb.jdae.utils.UnionFindEquivalence;
import de.tuberlin.uebb.jdae.utils.VarComparator;

/**
 * Simulation entry point. This class contains a unique
 * {@link DerivativeCollector} which allows {@link Causalisation} to create an
 * explicit ODE. The actual calculation is done in a newly allocated
 * {@link SolvableDAE}.
 * 
 * @author Christoph Höger <christoph.hoeger@tu-berlin.de>
 * 
 */
public final class DefaultSimulationRuntime implements SimulationRuntime {

    public final Logger logger;
    public final DerivativeRelation derivative_collector;
    private ResultStorage results;

    public DefaultSimulationRuntime(Logger l,
            DerivativeRelation derivative_collector) {
        super();
        this.logger = l;
        this.derivative_collector = derivative_collector;
        logger.setLevel(Level.INFO);
    }

    public DefaultSimulationRuntime(DerivativeRelation derivative_collector) {
        this(Logger.getLogger("simulation"), derivative_collector);
    }

    public DefaultSimulationRuntime() {
        this(Logger.getLogger("simulation"), new DerivativeCollector());
    }

    public DefaultSimulationRuntime(Logger l) {
        this(l, new DerivativeCollector());
    }

    /*
     * (nicht-Javadoc)
     * 
     * @see
     * de.tuberlin.uebb.jdae.simulation.ISimulationRuntime#causalise(java.util
     * .List)
     */
    @Override
    public SolvableDAE causalise(Collection<? extends Equation> equations) {
        final UnionFindEquivalence<Unknown> equiv = UnionFindEquivalence
                .create();
        final ImmutableSet.Builder<Unknown> variable_b = ImmutableSet.builder();

        /*
         * start with the der collector so we do not accidentally leave out some
         * states
         */
        variable_b.addAll(derivative_collector.domain());

        for (Equation eq : equations)
            variable_b.addAll(eq.canSolveFor(derivative_collector));
        final Set<Unknown> variables = variable_b.build();

        final ImmutableList.Builder<Equation> optimized_equations_b = ImmutableList
                .builder();

        /* TODO also merge the derivatives of equal states */
        for (Equation meq : equations) {
            if (meq instanceof Equality) {
                final Equality equals = (Equality) meq;
                equiv.merge(equals.lhs(), equals.rhs());
            } else {
                optimized_equations_b.add(meq);
            }
        }

        final ImmutableList<Equation> optimized_equations = optimized_equations_b
                .build();
        logger.log(Level.INFO, "Reduced system to {0} non-equality equations.",
                optimized_equations.size());

        final Causalisation causalisation = new Causalisation(logger);
        final long match_start = System.currentTimeMillis();
        final ImmutableMap<Unknown, Unknown> representatives = equiv
                .getRepresentatives(variables, new VarComparator(
                        derivative_collector.asMap()));

        final long graph_start = System.currentTimeMillis();
        final Map<Unknown, Equation> matching = causalisation.matching(
                optimized_equations, derivative_collector.asMap(),
                representatives);

        logger.log(
                Level.INFO,
                "Matched {0} out of {1} equations in {2}ms graph took {3}ms",
                new Object[] { matching.size(), optimized_equations.size(),
                        System.currentTimeMillis() - graph_start,
                        System.currentTimeMillis() - match_start });

        final long causalise_start = System.currentTimeMillis();

        final CausalisationResult causality = causalisation.causalise(matching,
                representatives, derivative_collector);

        logger.log(Level.INFO, "Created a causality-relation in {0}ms",
                new Object[] { System.currentTimeMillis() - causalise_start });

        final SolvableDAE solvableDAE = new SolvableDAE(causality.states,
                representatives, causality.computations, derivative_collector,
                logger);

        return solvableDAE;
    }

    /*
     * (nicht-Javadoc)
     * 
     * @see
     * de.tuberlin.uebb.jdae.simulation.ISimulationRuntime#simulateFixedStep
     * (de.tuberlin.uebb.jdae.dae.SolvableDAE, java.util.Map, double, int)
     */
    @Override
    public void simulateFixedStep(SolvableDAE dae,
            Iterable<EventHandler> events, Map<String, Double> inits,
            double stop_time, final int steps) {
        final double stepSize = (stop_time / steps);

        final FirstOrderIntegrator i = new EulerIntegrator(stepSize);
        final SimulationOptions options = new SimulationOptions(0.0, stop_time,
                stepSize * 1e-3, stepSize, stepSize, i, inits);

        simulate(dae, events, options);

    }

    /*
     * (nicht-Javadoc)
     * 
     * @see
     * de.tuberlin.uebb.jdae.simulation.ISimulationRuntime#simulateVariableStep
     * (de.tuberlin.uebb.jdae.dae.SolvableDAE, java.util.Map, double, double,
     * double, double, double)
     */
    @Override
    public void simulateVariableStep(SolvableDAE dae,
            Map<String, Double> inits, double stop_time, double minStep,
            double maxStep, double absoluteTolerance, double relativeTolerance) {
        simulateVariableStep(dae, ImmutableList.<EventHandler> of(), inits,
                stop_time, minStep, maxStep, absoluteTolerance,
                relativeTolerance);
    }

    @Override
    public void simulateVariableStep(SolvableDAE dae,
            Iterable<EventHandler> events, Map<String, Double> inits,
            double stop_time, double minStep, double maxStep,
            double absoluteTolerance, double relativeTolerance) {

        final FirstOrderIntegrator i = new DormandPrince54Integrator(minStep,
                maxStep, absoluteTolerance, relativeTolerance);
        final SimulationOptions options = new SimulationOptions(0.0, stop_time,
                absoluteTolerance, minStep, maxStep, i, inits);

        simulate(dae, events, options);
    }

    @Override
    public DerivativeRelation der() {
        return derivative_collector;
    }

    @Override
    public ResultStorage lastResults() {
        return results;
    }

    @Override
    public void simulate(SolvableDAE dae, Iterable<EventHandler> events,
            SimulationOptions options) {
        SpecializedConstantLinearEquation.time_spent = 0;
        results = new ResultStorage(dae, 1000);

        System.out.println("About to simulate using options: " + options);

        options.integrator.clearEventHandlers();
        options.integrator.clearStepHandlers();

        options.integrator.addStepHandler(results);

        for (EventHandler e : events) {
            options.integrator.addEventHandler(e, options.minStepSize,
                    options.tolerance, 1000);
        }

        final long start = System.currentTimeMillis();

        dae.integrate(options);

        logger.log(
                Level.INFO,
                "Simulation finished after {1} accepted steps, {2} system-evaluations in {0}ms",
                new Object[] { System.currentTimeMillis() - start,
                        results.results.size(), dae.evaluations });
    }

    @Override
    public void simulateFixedStep(SolvableDAE dae,
            ImmutableMap<String, Double> inits, double stopTime, int fixedSteps) {
        simulateFixedStep(dae, ImmutableList.<EventHandler> of(), inits,
                stopTime, fixedSteps);
    }
}
