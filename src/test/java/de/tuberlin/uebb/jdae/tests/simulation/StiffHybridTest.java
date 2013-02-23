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

import de.tuberlin.uebb.jdae.dae.SolvableDAE;
import de.tuberlin.uebb.jdae.examples.StiffHybrid;
import de.tuberlin.uebb.jdae.simulation.DefaultSimulationRuntime;
import de.tuberlin.uebb.jdae.simulation.SimulationRuntime;

import static org.hamcrest.CoreMatchers.is;

import static org.junit.Assert.assertThat;

public class StiffHybridTest {

    @Test
    public void test() {
        final SimulationRuntime runtime = new DefaultSimulationRuntime();

        final StiffHybrid model = new StiffHybrid(runtime);

        final SolvableDAE dae = runtime.causalise(model.equations());

        runtime.simulateVariableStep(dae, model.events(dae), model.initials(),
                10000, 0.002, 0.01, 0.000001, 0.01);

        assertThat(model.events, is(15916));
    }

}
