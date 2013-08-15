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

import org.junit.Ignore;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

import de.tuberlin.uebb.jdae.examples.BouncingBallArray;
import de.tuberlin.uebb.jdae.llmsl.ExecutableDAE;
import de.tuberlin.uebb.jdae.llmsl.GlobalEquation;
import de.tuberlin.uebb.jdae.llmsl.events.ContinuousEvent;
import de.tuberlin.uebb.jdae.llmsl.specials.ConstantGlobalEquation;
import de.tuberlin.uebb.jdae.transformation.Reduction;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThan;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class BouncingArrayTest {

    final SimulationRuntime runtime = new DefaultSimulationRuntime();
    final BouncingBallArray model = new BouncingBallArray(runtime);
    final Reduction reduction = runtime.reduce(model.equations());

    final ExecutableDAE dae = runtime.causalise(model);

    @Test
    public void testCausalisation() {
        assertThat(reduction.reduced.size(), is(9));

        assertNotNull(dae);

        assertThat(dae.getDimension(), is(6));
        assertThat(dae.layout.rows.length, is(9));
    }

    @Test
    public void testInitialization() {

        final ExecutableDAE dae = runtime.causalise(model);

        dae.initialize();

        assertEquals(5.0, dae.load(reduction, model.balls[0].h), 1e-6);
        assertEquals(10.0, dae.load(reduction, model.balls[1].h), 1e-6);
        assertEquals(20.0, dae.load(reduction, model.balls[2].h), 1e-6);

    }

    @Test
    public void testSimulation() {
        dae.initialize();

        runtime.simulateVariableStep(dae, 1, Double.MIN_VALUE,
                Double.MAX_VALUE, 1e-6, 1e-6);

        assertEquals(1, dae.data[0][0], 1e-8);

        assertThat(model.balls[0].events, is(1));
        assertThat(model.balls[1].events, is(0));
        assertThat(model.balls[2].events, is(0));

        assertThat(dae.load(reduction, model.balls[0].e),
                is(greaterThan(dae.load(reduction, model.balls[1].e))));

    }
}
