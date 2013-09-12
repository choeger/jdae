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

import de.tuberlin.uebb.jdae.examples.BouncingBall;
import de.tuberlin.uebb.jdae.llmsl.ExecutableDAE;
import de.tuberlin.uebb.jdae.transformation.Reduction;

import static org.hamcrest.CoreMatchers.is;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class BouncingBallTest {

    final SimulationRuntime runtime = new DefaultSimulationRuntime();
    final BouncingBall model = new BouncingBall(runtime);
    final Reduction reduction = runtime.reduce(model.equations());

    final ExecutableDAE dae = runtime.causalise(model,
            SimulationOptions.DEFAULT);

    @Test
    public void testCausalisation() {
        /* d2h and b */
        assertThat(reduction.reduced.size(), is(2));

        assertNotNull(dae);

        /* h and dh */
        assertThat(dae.getDimension(), is(2));

        /* h and b */
        assertThat(dae.layout.rows.length, is(2));
    }

    @Test
    public void testInitialization() {

        final ExecutableDAE dae = runtime.causalise(model,
                SimulationOptions.DEFAULT);

        dae.initialize();

        assertEquals(10.0, dae.load(reduction, model.h), 1e-6);
    }

    @Test
    public void testSimulation() {
        dae.initialize();

        runtime.simulateVariableStep(dae, 10, Double.MIN_VALUE,
                Double.MAX_VALUE, 1e-6, 1e-6);

        assertEquals(10, dae.data[0][0], 1e-8);
        assertThat(model.events, is(7));

    }

    @Test
    public void testInlineSimulation() {
        dae.initialize();
        final int steps = 10000;
        runtime.simulateInlineFixedStep(dae, 10, steps);

        assertEquals(10, dae.data[0][0], 1.0 / steps);
        assertThat(model.events, is(7));

    }

}
