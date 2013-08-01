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

import org.apache.commons.math3.ode.events.EventHandler;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import de.tuberlin.uebb.jdae.examples.StiffHybrid;
import de.tuberlin.uebb.jdae.llmsl.ContinuousEvent;
import de.tuberlin.uebb.jdae.llmsl.ExecutableDAE;
import de.tuberlin.uebb.jdae.llmsl.GlobalEquation;
import de.tuberlin.uebb.jdae.llmsl.specials.ConstantGlobalEquation;
import de.tuberlin.uebb.jdae.transformation.Reduction;

import static org.hamcrest.CoreMatchers.is;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class StiffHybridTest {

    final SimulationRuntime runtime = new DefaultSimulationRuntime();
    final StiffHybrid model = new StiffHybrid(runtime);
    final Reduction reduction = runtime.reduce(model.equations());

    private GlobalEquation x1_init = new ConstantGlobalEquation(
            reduction.ctxt.get(model.x1), 0.0);

    final ExecutableDAE dae = runtime.causalise(reduction,
            ImmutableList.of(x1_init), model.initials(reduction.ctxt));
    final Iterable<EventHandler> events = Iterables.transform(
            model.events(reduction.ctxt), ContinuousEvent.instantiation(dae));

    @Test
    public void testCausalisation() {
        assertThat(reduction.reduced.size(), is(2));

        assertNotNull(dae);

        assertThat(dae.getDimension(), is(1));
        assertThat(dae.layout.rows.length, is(2));
    }

    @Test
    public void testInitialization() {

        final ExecutableDAE dae = runtime.causalise(reduction,
                ImmutableList.<GlobalEquation> of(x1_init),
                model.initials(reduction.ctxt));

        dae.initialize();

        assertEquals(0, dae.load(reduction, model.x1.der()), 1e-8);
        assertEquals(0.0, dae.load(reduction, model.x1), 1e-8);
        assertEquals(1.0, dae.load(reduction, model.x2), 1e-8);
    }

    @Test
    public void testSimulation() {
        dae.initialize();

        runtime.simulateVariableStep(dae, events, 10, Double.MIN_VALUE,
                Double.MAX_VALUE, 1e-9, 1e-9);

        assertEquals(10, dae.data[0][0], 1e-8);
        assertThat(model.events, is(16));

    }
}
