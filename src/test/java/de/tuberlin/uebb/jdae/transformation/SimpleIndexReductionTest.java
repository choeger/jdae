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

package de.tuberlin.uebb.jdae.transformation;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

import de.tuberlin.uebb.jdae.examples.SimpleHigherIndexExample;
import de.tuberlin.uebb.jdae.llmsl.ExecutableDAE;
import de.tuberlin.uebb.jdae.llmsl.GlobalEquation;
import de.tuberlin.uebb.jdae.llmsl.events.ContinuousEvent;
import de.tuberlin.uebb.jdae.simulation.DefaultSimulationRuntime;
import de.tuberlin.uebb.jdae.simulation.SimulationOptions;
import de.tuberlin.uebb.jdae.simulation.SimulationRuntime;

import static org.hamcrest.Matchers.is;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * @author choeger
 * 
 */
public final class SimpleIndexReductionTest {

    final SimulationRuntime runtime = new DefaultSimulationRuntime();
    final SimpleHigherIndexExample m = new SimpleHigherIndexExample(runtime);
    final Reduction reduction = runtime.reduce(m.equations());

    @Test
    public void testCausalisation() {
        assertThat(reduction.reduced.size(), is(2));

        final ExecutableDAE dae = runtime.causalise(reduction,
                ImmutableList.<GlobalEquation> of(),
                m.initials(reduction.ctxt), new ContinuousEvent[0],
                SimulationOptions.DEFAULT);

        assertNotNull(dae);

        /* Index reduction eliminates one state */
        assertThat(dae.getDimension(), is(1));

        /* We have two algebraics, one former state and its derivative */
        assertThat(dae.layout.rows.length, is(2));
    }

    @Test
    public void testInitialization() {
        final ExecutableDAE dae = runtime.causalise(reduction,
                ImmutableList.<GlobalEquation> of(),
                m.initials(reduction.ctxt), new ContinuousEvent[0],
                SimulationOptions.DEFAULT);

        dae.initialize();

        assertEquals(0.5, dae.data[1][1], 1e-6);
        assertEquals(0.5, dae.data[2][1], 1e-6);
    }

    @Test
    public void testSimulation() {

        final ContinuousEvent[] events = m.events(reduction.ctxt).toArray(
                new ContinuousEvent[0]);

        final ExecutableDAE dae = runtime.causalise(reduction,
                ImmutableList.<GlobalEquation> of(),
                m.initials(reduction.ctxt), events, SimulationOptions.DEFAULT);

        runtime.simulateVariableStep(dae, 1.0, Double.MAX_VALUE,
                Double.MAX_VALUE, 1e-6, 1e-6);

        assertEquals(0.5, dae.load(reduction, m.dx), 1e-6);
        assertEquals(0.5 * dae.time(), dae.load(reduction, m.x), 1e-6);

        assertEquals(dae.load(reduction, m.dx), dae.load(reduction, m.dy), 1e-6);
        assertEquals(dae.load(reduction, m.x), dae.load(reduction, m.y), 1e-6);
    }

}
