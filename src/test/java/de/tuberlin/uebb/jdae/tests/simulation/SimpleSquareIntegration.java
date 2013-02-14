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
package de.tuberlin.uebb.jdae.tests.simulation;

import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import de.tuberlin.uebb.jdae.dae.ConstantLinearEquation;
import de.tuberlin.uebb.jdae.dae.Equation;
import de.tuberlin.uebb.jdae.dae.SimpleVar;
import de.tuberlin.uebb.jdae.dae.SolvableDAE;
import de.tuberlin.uebb.jdae.dae.Unknown;
import de.tuberlin.uebb.jdae.simulation.SimulationRuntime;
import static org.junit.Assert.assertEquals;

public class SimpleSquareIntegration {

    private static final double PRECISION = 0.0001;
    private static final int FIXED_STEPS = 250000;

    private static final double MIN_STEPSIZE = PRECISION;
    private static final double MAX_STEPSIZE = 1.0;
    private static final double RTOL = 0.0001;

    public static SolvableDAE model(SimulationRuntime runtime) {

        /*
         * der(x) = 2*time + 1;
         */
        final Unknown x = new SimpleVar("x");
        final Unknown dx = runtime.derivative_collector.apply(x);
        final Equation eq = ConstantLinearEquation.builder().add(dx, 1.0)
                .addTime(-2).add(1.0).build();

        return runtime.causalise(ImmutableList.of(eq));
    }

    @Test
    public void testCausalisation() {
        final SimulationRuntime runtime = new SimulationRuntime();
        final SolvableDAE dae = model(runtime);
        assertEquals(1, dae.specialComputations[0].target);
    }

    @Test
    public void testSimulation() {
        final SimulationRuntime runtime = new SimulationRuntime();
        int stop_time = 2;
        final SolvableDAE dae = model(runtime);
        runtime.simulateFixedStep(dae, ImmutableMap.of("x", 0.0), stop_time,
                FIXED_STEPS);

        final double t = dae.currentTime;
        assertEquals(stop_time, dae.currentTime, PRECISION);
        assertEquals(2 * t + 1, (Double) dae.get(1), PRECISION);
        assertEquals(t * t + t, (Double) dae.get(0), PRECISION);
    }

    @Test
    public void testVariableStepSimulation() {
        final SimulationRuntime runtime = new SimulationRuntime();
        int stop_time = 2;
        final SolvableDAE dae = model(runtime);
        runtime.simulateVariableStep(dae, ImmutableMap.of("x", 0.0), stop_time,
                MIN_STEPSIZE, MAX_STEPSIZE, PRECISION, RTOL);

        final double t = dae.currentTime;
        assertEquals(stop_time, dae.currentTime, PRECISION);
        assertEquals(2 * t + 1, (Double) dae.get(1), PRECISION);
        assertEquals(t * t + t, (Double) dae.get(0), PRECISION);
    }
}
