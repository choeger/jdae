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

import org.apache.commons.math3.ode.events.EventHandler;

import de.tuberlin.uebb.jdae.hlmsl.Equation;
import de.tuberlin.uebb.jdae.hlmsl.Unknown;
import de.tuberlin.uebb.jdae.llmsl.ExecutableDAE;
import de.tuberlin.uebb.jdae.llmsl.GlobalEquation;
import de.tuberlin.uebb.jdae.llmsl.GlobalVariable;
import de.tuberlin.uebb.jdae.transformation.Reduction;

public interface SimulationRuntime {

    public Unknown newUnknown(String name);

    public Reduction reduce(Collection<Equation> equations);

    public ExecutableDAE causalise(Reduction reduction,
            List<GlobalEquation> initialEquations,
            Map<GlobalVariable, Double> startValues);

    public void simulate(ExecutableDAE dae, Iterable<EventHandler> events,
            SimulationOptions options);

    public void simulateFixedStep(ExecutableDAE dae,
            Iterable<EventHandler> events, double stop_time, int steps);

    public void simulateVariableStep(ExecutableDAE dae, double stop_time,
            double minStep, double maxStep, double absoluteTolerance,
            double relativeTolerance);

    public void simulateVariableStep(ExecutableDAE dae,
            Iterable<EventHandler> events, double stop_time, double minStep,
            double maxStep, double absoluteTolerance, double relativeTolerance);

    public ResultStorage lastResults();

    public void simulateFixedStep(ExecutableDAE dae, double stopTime,
            int fixedSteps);

    public void simulateInlineFixedStep(ExecutableDAE dae, double stopTime,
            int fixedSteps);

    public void simulateInlineFixedStep(ExecutableDAE dae,
            Iterable<EventHandler> events, double stop_time, int steps);

}