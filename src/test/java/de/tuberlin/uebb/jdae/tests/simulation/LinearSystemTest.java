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

import de.tuberlin.uebb.jdae.hlmsl.Equation;
import de.tuberlin.uebb.jdae.hlmsl.Unknown;
import de.tuberlin.uebb.jdae.hlmsl.specials.ConstantLinear;
import de.tuberlin.uebb.jdae.llmsl.ExecutableDAE;
import de.tuberlin.uebb.jdae.llmsl.GlobalEquation;
import de.tuberlin.uebb.jdae.llmsl.GlobalVariable;
import de.tuberlin.uebb.jdae.simulation.DefaultSimulationRuntime;
import de.tuberlin.uebb.jdae.simulation.SimulationRuntime;
import de.tuberlin.uebb.jdae.transformation.Reduction;

import static org.hamcrest.CoreMatchers.is;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class LinearSystemTest {

    /*
     * x = time; x + y = 2*time;
     */

    private static final double PRECISION = 0.0001;
    private static final int FIXED_STEPS = 25000;

    final static de.tuberlin.uebb.jdae.hlmsl.Unknown x = new Unknown("x", 1, 0);
    final static Unknown y = new Unknown("y", 2, 0);
    final static Equation LINEAR1 = new ConstantLinear(-1.0, 0,
            new double[] { 1.0 }, ImmutableList.of(x));
    final static Equation LINEAR2 = new ConstantLinear(-2.0, 0, new double[] {
            1.0, 1.0 }, ImmutableList.of(x, y));

    final SimulationRuntime runtime = new DefaultSimulationRuntime();
    final Reduction reduction = runtime.reduce(ImmutableList.of(LINEAR1,
            LINEAR2));
    final ExecutableDAE dae = runtime.causalise(reduction,
            ImmutableList.<GlobalEquation> of(),
            ImmutableMap.<GlobalVariable, Double> of());

    @Test
    public void testCausalisation() {
        assertNotNull(dae);

        assertThat(dae.layout.rows.length, is(2));
        assertThat(dae.blocks.length, is(2));
        assertThat(dae.states.size(), is(0));

    }

    @Test
    public void testSimulation() {
        double stopTime = 1.0;
        runtime.simulateFixedStep(dae, stopTime, FIXED_STEPS);
        assertEquals(stopTime, dae.time(), PRECISION);
        assertEquals((Double) dae.time(), dae.load(reduction, x), PRECISION);
        assertEquals((Double) dae.time(), dae.load(reduction, y), PRECISION);
    }
}
