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
import de.tuberlin.uebb.jdae.simulation.DefaultSimulationRuntime;
import de.tuberlin.uebb.jdae.simulation.SimulationRuntime;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class LinearSystemTest {

    /*
     * x = time; x + y = 2*time;
     */

    private static final double PRECISION = 0.0001;
    private static final int FIXED_STEPS = 250000;

    final static Unknown x = new SimpleVar("x");
    final static Unknown y = new SimpleVar("y");
    final static Equation LINEAR1 = ConstantLinearEquation.builder()
            .add(x, 1.0).subTime(1.0).build();
    final static Equation LINEAR2 = ConstantLinearEquation.builder()
            .add(x, 1.0).add(y, 1.0).subTime(2.0).build();

    @Test
    public void testCausalisation() {
        final SimulationRuntime runtime = new DefaultSimulationRuntime();
        final SolvableDAE dae = runtime.causalise(ImmutableList.of(LINEAR1,
                LINEAR2));
        assertNotNull(dae);
    }

    @Test
    public void testSimulation() {
        final SimulationRuntime runtime = new DefaultSimulationRuntime();
        final SolvableDAE dae = runtime.causalise(ImmutableList.of(LINEAR1,
                LINEAR2));

        double stopTime = 1.0;
        runtime.simulateFixedStep(dae, ImmutableMap.of("x", 0.0, "y", 0.0),
                stopTime, FIXED_STEPS);
        assertEquals(stopTime, dae.currentTime, PRECISION);
        assertEquals((Double) dae.currentTime, dae.apply(x), PRECISION);
        assertEquals((Double) dae.currentTime, dae.apply(y), PRECISION);

    }
}
