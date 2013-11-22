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
import org.junit.Test;

import com.google.common.collect.ImmutableList;

import de.tuberlin.uebb.jdae.examples.Pendulum;
import de.tuberlin.uebb.jdae.examples.Pendulum.LengthBlockEquation;
import de.tuberlin.uebb.jdae.examples.Pendulum.XAccelBlockEquation;
import de.tuberlin.uebb.jdae.examples.Pendulum.YAccelBlockEquation;
import de.tuberlin.uebb.jdae.llmsl.Block;
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

public class PendulumTest {

    private static final int SIM_TEST_STOP_TIME = 100;

    final SimulationRuntime runtime = new DefaultSimulationRuntime();
    final Pendulum model = new Pendulum();
    final Reduction reduction = runtime.reduce(model.equations());

    final GlobalEquation initial_y = new ConstantGlobalEquation(
            reduction.ctxt.get(model.y), -0.9);
    final ContinuousEvent[] events = model.events(reduction.ctxt).toArray(
            new ContinuousEvent[0]);
    final ExecutableDAE dae = runtime
            .causalise(reduction, ImmutableList.of(initial_y),
                    model.initials(reduction.ctxt), events);
    
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
                model.initials(reduction.ctxt), events);

        final Block initBlock = (Block) dae.initials[2];

        final MultivariateMatrixFunction jacobian = initBlock.jacobian();

        int length_eq = -1;
        while (!(initBlock.equations[++length_eq].eq instanceof LengthBlockEquation))
            ;

        int xaccel_eq = -1;
        while (!(initBlock.equations[++xaccel_eq].eq instanceof XAccelBlockEquation))
            ;

        if (length_eq < xaccel_eq)
            xaccel_eq += 2;

        int yaccel_eq = -1;
        while (!(initBlock.equations[++yaccel_eq].eq instanceof YAccelBlockEquation))
            ;
        if (length_eq < yaccel_eq)
            yaccel_eq += 2;

        final double[][] testData = new double[][] { { 0, 0, 0, 0, 0 },
                { 1, 1, 1, 0, 0 }, { 0, 0, 0, 1, 1 }, { 1, 2, 3, 4, 5 },
                { -5, 4, -3, 2, -1 }, { 1, 1, 1, 1, 1 } };

        for (double[] data : testData) {
            final double[][] M = jacobian.value(data);
            assertThat(M.length, is(5));
            assertThat(M[0].length, is(5));
            assertThat(M[1].length, is(5));
            assertThat(M[2].length, is(5));
            assertThat(M[3].length, is(5));
            assertThat(M[4].length, is(5));

            // x² + y² - 1
            assertEquals(2 * data[0], M[length_eq][0], 1e-6);
            assertEquals(0.0, M[length_eq][1], 1e-6);
            assertEquals(0.0, M[length_eq][2], 1e-6);
            assertEquals(0.0, M[length_eq][3], 1e-6);
            assertEquals(0.0, M[length_eq][4], 1e-6);

            // 2x * dx + 2y * dy
            assertEquals(2 * data[1], M[length_eq + 1][0], 1e-6);
            assertEquals(2 * data[0], M[length_eq + 1][1], 1e-6);
            assertEquals(0.0, M[length_eq + 1][2], 1e-6);
            assertEquals(0.0, M[length_eq + 1][3], 1e-6);
            assertEquals(0.0, M[length_eq + 1][4], 1e-6);

            // 2x * ddx + 2dx² + 2y * ddy + 2dy²
            assertEquals(2 * data[2], M[length_eq + 2][0], 1e-6);
            assertEquals(4 * data[1], M[length_eq + 2][1], 1e-6);
            assertEquals(2 * data[0], M[length_eq + 2][2], 1e-6);
            assertEquals(0.0, M[length_eq + 2][3], 1e-6);
            assertEquals(2 * dae.data[3][0], M[length_eq + 2][4], 1e-6);

            // ddy = F*y - g
            assertEquals(0.0, M[yaccel_eq][0], 1e-6);
            assertEquals(0.0, M[yaccel_eq][1], 1e-6);
            assertEquals(0.0, M[yaccel_eq][2], 1e-6);
            assertEquals(-dae.data[3][0], M[yaccel_eq][3], 1e-6);
            assertEquals(1.0, M[yaccel_eq][4], 1e-6);

            // ddx = F*x
            assertEquals(-data[3], M[xaccel_eq][0], 1e-6);
            assertEquals(0.0, M[xaccel_eq][1], 1e-6);
            assertEquals(1.0, M[xaccel_eq][2], 1e-6);
            assertEquals(-data[0], M[xaccel_eq][3], 1e-6);
            assertEquals(0.0, M[xaccel_eq][4], 1e-6);
        }
    }

    @Test
    public void testInitialization() {

        final ExecutableDAE dae = runtime.causalise(reduction,
                ImmutableList.<GlobalEquation> of(initial_y),
                model.initials(reduction.ctxt), events);

        dae.data[1][0] = 0.1;

        dae.initialize();

        final double delta = 1e-4;
        assertEquals(-0.9, dae.load(reduction, model.y), delta);
        assertEquals(Math.sqrt(1 - 0.9 * 0.9), dae.load(reduction, model.x),
                delta);
        final double force = (dae.load(reduction, model.x.der(2)))
                / dae.load(reduction, model.x);
        assertEquals(force, dae.load(reduction, model.F), delta);

        final double x = dae.load(reduction, model.x);
        final double dx = dae.load(reduction, model.x.der());
        final double ddx = dae.load(reduction, model.x.der(2));

        final double y = dae.load(reduction, model.y);
        final double dy = dae.load(reduction, model.y.der());
        final double ddy = dae.load(reduction, model.y.der(2));

        assertEquals(0.0,
                2 * ddx * x + 2 * dx * dx + 2 * ddy * y + 2 * dy * dy, delta);

    }

    @Test
    public void testLongSimulationVariableStep() {

        dae.data[1][0] = 0.1;
        dae.initialize();

        runtime.simulateVariableStep(dae, SIM_TEST_STOP_TIME, Double.MIN_VALUE,
                Double.MAX_VALUE, 1e-6, 1e-6);

        assertEquals(SIM_TEST_STOP_TIME, dae.data[0][0], 1e-8);
        assertThat(runtime.lastResults().results.size(), is(greaterThan(1)));
    }

    @Test
    public void testShortSimulationFixedStep() {

        dae.data[1][0] = 0.1;

        runtime.simulateFixedStep(dae, 1, 100000);

        assertEquals(1, dae.data[0][0], 1e-8);
        assertEquals(-0.43542183, dae.data[1][0], 1e-6);
        assertThat(runtime.lastResults().results.size(), is(100001));
    }

    @Test
    public void testShortSimulationVariableStep() {

        dae.data[1][0] = 0.1;
        dae.initialize();

        runtime.simulateVariableStep(dae, 1, Double.MIN_VALUE,
                Double.MAX_VALUE, 1e-6, 1e-6);

        assertEquals(1, dae.data[0][0], 1e-8);
        assertEquals(-0.4354014183074412, dae.data[1][0], 1e-6);
    }

    @Test
    public void testLongSimulationFixedStep() {

        dae.data[1][0] = 0.1;
        dae.initialize();

        Block.evals = 0;
        dae.time = 0;
        long start = System.currentTimeMillis();
        runtime.simulateFixedStep(dae, SIM_TEST_STOP_TIME,
                SIM_TEST_STOP_TIME * 1000);
        System.out.println("Overall simulation time: "
                + (System.currentTimeMillis() - start));
        System.out.println("Solver time: " + dae.time);
        System.out.println("Block eval time: " + Block.evals);

        assertEquals(SIM_TEST_STOP_TIME, dae.data[0][0], 1e-8);
        assertThat(runtime.lastResults().results.size(),
                is(SIM_TEST_STOP_TIME * 1000));
    }

    @Test
    public void testLongSimulationInlineFixedStep() {

        dae.data[1][0] = 0.1;
        dae.initialize();

        Block.evals = 0;
        dae.time = 0;
        long start = System.currentTimeMillis();
        runtime.simulateInlineFixedStep(dae, SIM_TEST_STOP_TIME,
                SIM_TEST_STOP_TIME * 1000);
        System.out.println("Overall simulation time: "
                + (System.currentTimeMillis() - start));
        System.out.println("Solver time: " + dae.time);
        System.out.println("Block eval time: " + Block.evals);

        assertEquals(SIM_TEST_STOP_TIME, dae.data[0][0], 1e-8);
        assertThat(runtime.lastResults().results.size(),
                is(SIM_TEST_STOP_TIME * 1000));
    }

    @Test
    public void testVeryLongSimulationInlineFixedStep() {

        dae.data[1][0] = 0.1;
        dae.initialize();

        Block.evals = 0;
        dae.time = 0;
        long start = System.currentTimeMillis();
        runtime.simulateInlineFixedStep(dae, SIM_TEST_STOP_TIME,
                SIM_TEST_STOP_TIME * 10000);
        System.out.println("Overall simulation time: "
                + (System.currentTimeMillis() - start));
        System.out.println("Solver time: " + dae.time);
        System.out.println("Block eval time: " + Block.evals);

        assertEquals(SIM_TEST_STOP_TIME, dae.data[0][0], 1e-8);
        assertThat(runtime.lastResults().results.size(),
                is(SIM_TEST_STOP_TIME * 10000));
    }

    @Test
    public void testVeryLongSimulationFixedStep() {

        dae.data[1][0] = 0.1;
        dae.initialize();

        Block.evals = 0;
        dae.time = 0;
        long start = System.currentTimeMillis();
        runtime.simulateFixedStep(dae, SIM_TEST_STOP_TIME,
                SIM_TEST_STOP_TIME * 10000);
        System.out.println("Overall simulation time: "
                + (System.currentTimeMillis() - start));
        System.out.println("Solver time: " + dae.time);
        System.out.println("Block eval time: " + Block.evals);

        assertEquals(SIM_TEST_STOP_TIME, dae.data[0][0], 1e-8);

    }

}
