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

import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import de.tuberlin.uebb.jdae.hlmsl.Equation;
import de.tuberlin.uebb.jdae.hlmsl.Unknown;
import de.tuberlin.uebb.jdae.hlmsl.specials.ConstantEquation;
import de.tuberlin.uebb.jdae.hlmsl.specials.ConstantLinear;
import de.tuberlin.uebb.jdae.llmsl.ExecutableDAE;
import de.tuberlin.uebb.jdae.llmsl.GlobalVariable;
import de.tuberlin.uebb.jdae.transformation.Reduction;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class SimpleIntegrationTest {

    private static final double PRECISION = 0.0001;
    private static final int FIXED_STEPS = 250000;

    /*
     * der(x) = x;
     */
    final SimulationRuntime runtime = new DefaultSimulationRuntime();
    final Unknown x = new Unknown("x", 1, 0);
    final Unknown dx = x.der();
    final Equation eq = new ConstantLinear(0, 0, new double[] { 1, -1 },
            ImmutableList.of(x, dx));

    final Reduction reduction = runtime.reduce(ImmutableList.of(eq));
    final ExecutableDAE dae = runtime
            .causalise(reduction, ImmutableList.of(new ConstantEquation(x, 1.0)
                    .bind(reduction.ctxt)), ImmutableMap
                    .<GlobalVariable, Double> of());

    @Test
    public void testCausalisation() {
        assertThat(dae, is(notNullValue()));
        assertThat(dae.layout.rows.length, is(1));
        assertThat(dae.states.size(), is(1));
    }

    @Test
    public void testSimulation() {
        dae.initialize();
        assertEquals(1.0, dae.load(reduction, x), PRECISION);
        assertEquals(1.0, dae.load(reduction, dx), PRECISION);

        int stop_time = 1;
        runtime.simulateFixedStep(dae, stop_time, FIXED_STEPS);
        assertEquals(Math.exp(stop_time), (Double) dae.load(reduction, x),
                PRECISION);
    }
}
