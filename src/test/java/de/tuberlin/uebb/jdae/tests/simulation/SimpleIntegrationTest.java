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

import de.tuberlin.uebb.jdae.builtins.ConstantLinearEquation;
import de.tuberlin.uebb.jdae.builtins.SimpleVar;
import de.tuberlin.uebb.jdae.dae.Equation;
import de.tuberlin.uebb.jdae.dae.SolvableDAE;
import de.tuberlin.uebb.jdae.dae.Unknown;
import de.tuberlin.uebb.jdae.simulation.DefaultSimulationRuntime;
import de.tuberlin.uebb.jdae.simulation.SimulationRuntime;
import static org.junit.Assert.assertEquals;

public class SimpleIntegrationTest {

    private static final double PRECISION = 0.0001;
    private static final int FIXED_STEPS = 250000;

    /*
     * der(x) = x;
     */
    final SimulationRuntime runtime = new DefaultSimulationRuntime();
    final Unknown x = new SimpleVar("x");
    final Unknown dx = runtime.der().apply(x);
    final Equation eq = ConstantLinearEquation.builder().add(dx, 1.0)
            .add(x, -1).build();

    @Test
    public void testCausalisation() {
        final SolvableDAE dae = runtime.causalise(ImmutableList.of(eq));
        assertEquals(dae.variables.get(dx),
                (Integer) dae.computationalOrder[0].unknown());
    }

    @Test
    public void testSimulation() {
        int stop_time = 1;
        SolvableDAE dae = runtime.causalise(ImmutableList.of(eq));
        runtime.simulateFixedStep(dae, ImmutableMap.of("x", 1.0), stop_time,
                FIXED_STEPS);
        assertEquals((Double) dae.value(x, stop_time), Math.exp(stop_time),
                PRECISION);
    }
}
