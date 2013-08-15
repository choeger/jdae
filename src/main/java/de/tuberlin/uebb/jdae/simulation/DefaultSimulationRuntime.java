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
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.math3.ode.FirstOrderIntegrator;
import org.apache.commons.math3.ode.nonstiff.DormandPrince54Integrator;
import org.apache.commons.math3.ode.nonstiff.EulerIntegrator;

import de.tuberlin.uebb.jdae.hlmsl.Equation;
import de.tuberlin.uebb.jdae.hlmsl.Unknown;
import de.tuberlin.uebb.jdae.llmsl.DataLayout;
import de.tuberlin.uebb.jdae.llmsl.ExecutableDAE;
import de.tuberlin.uebb.jdae.llmsl.GlobalEquation;
import de.tuberlin.uebb.jdae.llmsl.GlobalVariable;
import de.tuberlin.uebb.jdae.dae.LoadableModel;
import de.tuberlin.uebb.jdae.llmsl.events.ContinuousEvent;
import de.tuberlin.uebb.jdae.transformation.Causalisation;
import de.tuberlin.uebb.jdae.transformation.InitializationCausalisation;
import de.tuberlin.uebb.jdae.transformation.InitializationMatching;
import de.tuberlin.uebb.jdae.transformation.Matching;
import de.tuberlin.uebb.jdae.transformation.Reduction;

import com.google.common.collect.Lists;
import com.google.common.collect.ImmutableList;


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
    private int unknowns = 0;
    private ResultStorage results;

    public DefaultSimulationRuntime(Logger l) {
        super();
        this.logger = l;
        logger.setLevel(Level.INFO);
    }

    public DefaultSimulationRuntime() {
        this(Logger.getLogger("simulation"));
    }

    /*
     * (nicht-Javadoc)
     * 
     * @see
     * de.tuberlin.uebb.jdae.simulation.ISimulationRuntime#simulateFixedStep
     * (de.tuberlin.uebb.jdae.dae.SolvableDAE, java.util.Map, double, int)
     */
    @Override
    public void simulateFixedStep(ExecutableDAE dae, double stop_time,
            final int steps) {
        final double stepSize = (stop_time / steps);

        final FirstOrderIntegrator i = new EulerIntegrator(stepSize);
        final SimulationOptions options = new SimulationOptions(0.0, stop_time,
                stepSize * 1e-3, stepSize, stepSize, i);

        simulate(dae, options);
    }

    @Override
    public void simulateInlineFixedStep(ExecutableDAE dae, double stop_time,
            final int steps) {
        final double stepSize = (stop_time / steps);

        final SimulationOptions options = new SimulationOptions(0.0, stop_time,
                stepSize * 1e-3, stepSize, stepSize,
                InlineIntegratorSelection.INLINE_FORWARD_EULER);

        simulate(dae, options);
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
    public void simulateVariableStep(ExecutableDAE dae, double stop_time,
            double minStep, double maxStep, double absoluteTolerance,
            double relativeTolerance) {

        final FirstOrderIntegrator i = new DormandPrince54Integrator(minStep,
                maxStep, absoluteTolerance, relativeTolerance);
        final SimulationOptions options = new SimulationOptions(0.0, stop_time,
                absoluteTolerance, minStep, maxStep, i);

        simulate(dae, options);
    }

    @Override
    public ResultStorage lastResults() {
        return results;
    }

    @Override
    public void simulate(ExecutableDAE dae, SimulationOptions options) {
        results = new ResultStorage(dae);

        final long iStart = System.currentTimeMillis();

        dae.initialize();

        logger.log(Level.INFO, "Initialization done after: {0}ms",
                (System.currentTimeMillis() - iStart));

        logger.log(Level.INFO, "About to simulate using options: {0}", options);

        if (options.integrator != null) {
            options.integrator.clearEventHandlers();
            options.integrator.clearStepHandlers();

            options.integrator.addStepHandler(results);
            // TODO: add event Handler
        }

        final long start = System.currentTimeMillis();

        dae.integrate(results, options);

        logger.log(
                Level.INFO,
                "Simulation finished after {1} accepted steps, {2} system-evaluations in {0}ms",
                new Object[] { System.currentTimeMillis() - start,
                        results.results.size(), dae.getEvaluations() });
    }

    @Override
    public Reduction reduce(final Collection<Equation> equations) {
        final Reduction reduction = new Reduction(equations);
        logger.log(Level.INFO, "Reduced system to {0} non-equality equations.",
                reduction.reduced.size());
        return reduction;
    }

    @Override
    public ExecutableDAE causalise(LoadableModel model) {
	final Reduction reduction = reduce(model.equations());
	final Collection<ContinuousEvent> events = model.events(reduction.ctxt);
	return causalise(reduction, 
			 Lists.transform(ImmutableList.copyOf(model.initialEquations()), 
					 GlobalEquation.bindFrom(reduction.ctxt)), 
			 model.initials(reduction.ctxt), 
			 events.toArray(new ContinuousEvent[events.size()]));
    }

    @Override
    public ExecutableDAE causalise(Reduction reduction,
            List<GlobalEquation> initialEquations,
            Map<GlobalVariable, Double> startValues, ContinuousEvent[] c_events) {
        final long match_start = System.currentTimeMillis();

        final Matching matching = new Matching(reduction, logger);

        logger.log(Level.INFO, "Matched {0} equations in {1}ms", new Object[] {
                matching.assignment.length,
                System.currentTimeMillis() - match_start });

        final long causalise_start = System.currentTimeMillis();

        final Causalisation causality = new Causalisation(reduction, matching);

        logger.log(Level.INFO, "Created a causality-relation in {0}ms",
                new Object[] { System.currentTimeMillis() - causalise_start });

        final InitializationMatching iMatching = new InitializationMatching(
                reduction, causality, matching, initialEquations, startValues,
                logger);

        final InitializationCausalisation iCausalisation = new InitializationCausalisation(
                iMatching, logger);

        final ExecutableDAE dae = new ExecutableDAE(new DataLayout(
                causality.layout), causality, iCausalisation, c_events);

        return dae;

    }

    @Override
    public Unknown newUnknown(String name) {
        return new Unknown(name, ++unknowns, 0);
    }
}
