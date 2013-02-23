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

import org.apache.commons.math3.ode.events.EventHandler;

import com.google.common.base.Function;

import de.tuberlin.uebb.jdae.dae.Equation;
import de.tuberlin.uebb.jdae.dae.SolvableDAE;
import de.tuberlin.uebb.jdae.dae.Unknown;

public interface SimulationRuntime {

    /**
     * Create a solvable dae instance from a list of equations.
     * 
     * @param equations
     *            the equations that form the model. Note: Any derivatives need
     *            to be collected with {@code derivative_collector}!
     * @return a solvable dae
     */
    public abstract SolvableDAE causalise(
            Collection<? extends Equation> equations);

    public Function<Unknown, Unknown> der();

    public abstract void simulateFixedStep(SolvableDAE dae,
            Map<String, Double> inits, double stop_time, int steps);

    public abstract void simulateVariableStep(SolvableDAE dae,
            Map<String, Double> inits, double stop_time, double minStep,
            double maxStep, double absoluteTolerance, double relativeTolerance);

    void simulateVariableStep(SolvableDAE dae, Iterable<EventHandler> events,
            Map<String, Double> inits, double stop_time, double minStep,
            double maxStep, double absoluteTolerance, double relativeTolerance);

    ResultStorage lastResults();

}