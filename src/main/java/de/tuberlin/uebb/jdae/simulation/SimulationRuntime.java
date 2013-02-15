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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.math3.ode.FirstOrderIntegrator;
import org.apache.commons.math3.ode.nonstiff.DormandPrince54Integrator;
import org.apache.commons.math3.ode.nonstiff.EulerIntegrator;

import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import de.tuberlin.uebb.jdae.dae.DerivativeCollector;
import de.tuberlin.uebb.jdae.dae.EqualityEquation;
import de.tuberlin.uebb.jdae.dae.Equation;
import de.tuberlin.uebb.jdae.dae.SolvableDAE;
import de.tuberlin.uebb.jdae.dae.SpecializedConstantLinearEquation;
import de.tuberlin.uebb.jdae.dae.Unknown;
import de.tuberlin.uebb.jdae.transformation.Causalisation;
import de.tuberlin.uebb.jdae.transformation.Causalisation.CausalisationResult;
import de.tuberlin.uebb.jdae.transformation.Causalisation.Computation;
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
public final class SimulationRuntime {

    public final Logger logger = Logger.getLogger("simulation");
    public final LoadingCache<Unknown, Unknown> derivative_collector = DerivativeCollector
            .derivatives();
    private long last_eq_instantiation;
    private long last_obj_instantiation;

    public SimulationRuntime() {
        logger.setLevel(Level.INFO);
    }

    /**
     * Create a solvable dae instance from a list of equations.
     * 
     * @param equations
     *            the equations that form the model. Note: Any derivatives need
     *            to be collected with {@code derivative_collector}!
     * @return a solvable dae
     */
    public SolvableDAE causalise(List<Equation> equations) {
        final UnionFindEquivalence<Unknown> equiv = UnionFindEquivalence
                .create();
        final ImmutableSet.Builder<Unknown> variable_b = ImmutableSet.builder();

        /*
         * start with the der collector so we do not accidentally leave out some
         * states
         */
        variable_b.addAll(derivative_collector.asMap().keySet());

        for (Equation eq : equations)
            variable_b.addAll(eq.canSolveFor(derivative_collector));
        final Set<Unknown> variables = variable_b.build();

        final ImmutableList.Builder<Equation> optimized_equations_b = ImmutableList
                .builder();

        for (Equation meq : equations) {
            if (meq instanceof EqualityEquation) {
                final EqualityEquation equals = (EqualityEquation) meq;
                equiv.merge(equals.lhs_obj, equals.rhs_obj);
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

        final Map<Unknown, Equation> matching = causalisation.prepareEquations(
                optimized_equations, derivative_collector.asMap(),
                representatives);

        logger.log(Level.INFO, "Matched {0} out of {1} equations in {2}ms",
                new Object[] { matching.size(), optimized_equations.size(),
                        System.currentTimeMillis() - match_start });

        final long causalise_start = System.currentTimeMillis();

        final CausalisationResult causality = causalisation.causalise(matching,
                representatives, derivative_collector);

        logger.log(Level.INFO, "Created a causality-relation in {0}ms",
                new Object[] { System.currentTimeMillis() - causalise_start });

        for (Computation comp : causality.computations) {
            logger.log(Level.INFO, comp.toString());
        }

        final SolvableDAE solvableDAE = new SolvableDAE(causality.states,
                representatives, causality.computations, derivative_collector,
                logger);

        solvableDAE.equation_instantiation_time = last_eq_instantiation;
        solvableDAE.object_instantiation_time = last_obj_instantiation;

        return solvableDAE;
    }

    public void simulateFixedStep(SolvableDAE dae, Map<String, Double> inits,
            double stop_time, final int steps) {

        SpecializedConstantLinearEquation.time_spent = 0;

        final ResultStorage storage = new ResultStorage(dae, steps);

        final FirstOrderIntegrator i = new EulerIntegrator(stop_time / steps);
        i.addStepHandler(storage);

        final long start = System.currentTimeMillis();
        dae.integrate(new SimulationOptions(0.0, stop_time, i, inits));
        logger.log(Level.INFO,
                "Simulation finished after {1} evaluation-steps in {0}ms",
                new Object[] { System.currentTimeMillis() - start,
                        dae.evaluations });
    }

    public void simulateVariableStep(SolvableDAE dae,
            Map<String, Double> inits, double stop_time, double minStep,
            double maxStep, double absoluteTolerance, double relativeTolerance) {
        SpecializedConstantLinearEquation.time_spent = 0;

        final ResultStorage storage = new ResultStorage(dae,
                (int) Math.round(stop_time / maxStep));
        final FirstOrderIntegrator i = new DormandPrince54Integrator(minStep,
                maxStep, absoluteTolerance, relativeTolerance);
        i.addStepHandler(storage);
        final long start = System.currentTimeMillis();
        dae.integrate(new SimulationOptions(0.0, stop_time, i, inits));
        logger.log(Level.INFO,
                "Simulation finished after {1} evaluation-steps in {0}ms",
                new Object[] { System.currentTimeMillis() - start,
                        dae.evaluations });
    }

}
