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

import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.apache.commons.math3.ode.events.EventHandler;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import de.tuberlin.uebb.jdae.examples.Pendulum;
import de.tuberlin.uebb.jdae.examples.Pendulum.LengthBlockEquation;
import de.tuberlin.uebb.jdae.examples.Pendulum.XAccelBlockEquation;
import de.tuberlin.uebb.jdae.examples.Pendulum.YAccelBlockEquation;
import de.tuberlin.uebb.jdae.llmsl.Block;
import de.tuberlin.uebb.jdae.llmsl.ContinuousEvent;
import de.tuberlin.uebb.jdae.llmsl.ExecutableDAE;
import de.tuberlin.uebb.jdae.llmsl.GlobalEquation;
import de.tuberlin.uebb.jdae.llmsl.specials.ConstantGlobalEquation;
import de.tuberlin.uebb.jdae.transformation.Reduction;

import static org.hamcrest.CoreMatchers.is;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class PendulumTest {

    final SimulationRuntime runtime = new DefaultSimulationRuntime();
    final Pendulum model = new Pendulum();
    final Reduction reduction = runtime.reduce(model.equations());

    final GlobalEquation initial_y = new ConstantGlobalEquation(
            reduction.ctxt.get(model.y), -0.9);

    final ExecutableDAE dae = runtime.causalise(reduction,
            ImmutableList.of(initial_y), model.initials(reduction.ctxt));
    final Iterable<EventHandler> events = Iterables.transform(
            model.events(reduction.ctxt), ContinuousEvent.instantiation(dae));

    @Test
    public void testCausalisation() {
        /* d2h and b */
        assertThat(reduction.reduced.size(), is(3));

        assertNotNull(dae);

        /* h and dh */
        assertThat(dae.getDimension(), is(2));

        /* h and b */
        assertThat(dae.layout.rows.length, is(3));
    }

    @Test
    public void testJacobian() {

        final ExecutableDAE dae = runtime.causalise(reduction,
                ImmutableList.<GlobalEquation> of(initial_y),
                model.initials(reduction.ctxt));

        final Block initBlock = (Block) dae.initials[2];

        final MultivariateMatrixFunction jacobian = initBlock.jacobian();

        int length_eq = 0;
        while (!(initBlock.equations[length_eq++].eq instanceof LengthBlockEquation))
            ;

        int xaccel_eq = 0;
        while (!(initBlock.equations[xaccel_eq++].eq instanceof XAccelBlockEquation))
            ;

        if (length_eq < xaccel_eq)
            xaccel_eq += 2;

        int yaccel_eq = 0;
        while (!(initBlock.equations[yaccel_eq++].eq instanceof YAccelBlockEquation))
            ;
        if (length_eq < yaccel_eq)
            yaccel_eq += 2;

        final double[][] testData = new double[][] { { 0, 0, 0, 0, 0, 0 },
                { 0, 1, 1, 1, 0, 0 }, { 0, 0, 0, 0, 1, 1 },
                { 0, 1, 2, 3, 4, 5 }, { 0, -5, 4, -3, 2, -1 },
                { 0, 1, 1, 1, 1, 1 } };

        for (double[] data : testData) {
            final double[][] M = jacobian.value(data);
            assertThat(M.length, is(6));
            assertThat(M[1].length, is(6));

            // x² + y² - 1
            assertEquals(2 * data[1], M[length_eq][1], 1e-6);
            assertEquals(0.0, M[length_eq][2], 1e-6);
            assertEquals(0.0, M[length_eq][3], 1e-6);
            assertEquals(0.0, M[length_eq][4], 1e-6);
            assertEquals(0.0, M[length_eq][5], 1e-6);

            // 2x * dx + 2y * dy
            assertEquals(2 * data[2], M[length_eq + 1][1], 1e-6);
            assertEquals(2 * data[1], M[length_eq + 1][2], 1e-6);
            assertEquals(0.0, M[length_eq + 1][3], 1e-6);
            assertEquals(0.0, M[length_eq + 1][4], 1e-6);
            assertEquals(0.0, M[length_eq + 1][5], 1e-6);

            // 2x * ddx + 2dx² + 2y * ddy + 2dy²
            assertEquals(2 * data[3], M[length_eq + 2][1], 1e-6);
            assertEquals(4 * data[2], M[length_eq + 2][2], 1e-6);
            assertEquals(2 * data[1], M[length_eq + 2][3], 1e-6);
            assertEquals(2 * dae.data[3][0], M[length_eq + 2][4], 1e-6);
            assertEquals(0.0, M[length_eq + 2][5], 1e-6);

            // ddy = F*y - g
            assertEquals(0.0, M[yaccel_eq][0], 1e-6);
            assertEquals(0.0, M[yaccel_eq][1], 1e-6);
            assertEquals(0.0, M[yaccel_eq][2], 1e-6);
            assertEquals(0.0, M[yaccel_eq][3], 1e-6);
            assertEquals(-dae.data[3][0], M[yaccel_eq][4], 1e-6);
            assertEquals(1.0, M[yaccel_eq][5], 1e-6);

            // ddx = F*x
            assertEquals(0.0, M[xaccel_eq][0], 1e-6);
            assertEquals(-data[4], M[xaccel_eq][1], 1e-6);
            assertEquals(0.0, M[xaccel_eq][2], 1e-6);
            assertEquals(1.0, M[xaccel_eq][3], 1e-6);
            assertEquals(-data[1], M[xaccel_eq][4], 1e-6);
            assertEquals(0.0, M[xaccel_eq][5], 1e-6);
        }
    }

    @Test
    public void testInitialization() {

        final ExecutableDAE dae = runtime.causalise(reduction,
                ImmutableList.<GlobalEquation> of(initial_y),
                model.initials(reduction.ctxt));

        dae.data[1][0] = 0.1;

        dae.initialize();

        assertEquals(-0.9, dae.load(reduction, model.y), 1e-6);
        assertEquals(Math.sqrt(1 - 0.9 * 0.9), dae.load(reduction, model.x),
                1e-6);
        final double force = (dae.load(reduction, model.x.der(2)))
                / dae.load(reduction, model.x);
        assertEquals(force, dae.load(reduction, model.F), 1e-6);

        final double x = dae.load(reduction, model.x);
        final double dx = dae.load(reduction, model.x.der());
        final double ddx = dae.load(reduction, model.x.der(2));

        final double y = dae.load(reduction, model.y);
        final double dy = dae.load(reduction, model.y.der());
        final double ddy = dae.load(reduction, model.y.der(2));

        assertEquals(0.0,
                2 * ddx * x + 2 * dx * dx + 2 * ddy * y + 2 * dy * dy, 1e-6);

    }

    @Test
    public void testSimulation() {

        dae.data[1][0] = 0.1;
        dae.initialize();

        runtime.simulateVariableStep(dae, events, 10, Double.MIN_VALUE,
                Double.MAX_VALUE, 1e-6, 1e-6);

        assertEquals(10, dae.data[0][0], 1e-8);

    }

}
