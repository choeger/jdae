/**
 * 
 */
package de.tuberlin.uebb.jdae.tests.transformation;

import org.junit.Test;

import de.tuberlin.uebb.jdae.dae.LoadableModel;
import de.tuberlin.uebb.jdae.dae.SolvableDAE;
import de.tuberlin.uebb.jdae.examples.SimpleHigherIndexExample;
import de.tuberlin.uebb.jdae.simulation.DefaultSimulationRuntime;
import de.tuberlin.uebb.jdae.simulation.SimulationRuntime;

import static org.hamcrest.Matchers.is;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * @author choeger
 * 
 */
public class SimpleIndexReductionTest {

    @Test
    public void testCausalisation() {
        final SimulationRuntime runtime = new DefaultSimulationRuntime();
        final LoadableModel m = new SimpleHigherIndexExample(runtime);
        final SolvableDAE dae = runtime.causalise(m.equations());

        assertNotNull(dae);

        /* Index reduction eliminates one state */
        assertThat(dae.dimension, is(1));

        /* We have two algebraics, one former state and its derivative */
        assertThat(dae.algebraics.length, is(2));
    }

    @Test
    public void testSimulation() {
        final SimulationRuntime runtime = new DefaultSimulationRuntime();
        final SimpleHigherIndexExample m = new SimpleHigherIndexExample(runtime);
        final SolvableDAE dae = runtime.causalise(m.equations());

        runtime.simulateVariableStep(dae, m.events(dae), m.initials(), 1.0,
                Double.MAX_VALUE, Double.MAX_VALUE, 1e-6, 1e-6);

        assertEquals(0.5, dae.value(m.dx, dae.time), 1e-6);
        assertEquals(0.5 * dae.time, dae.value(m.x, dae.time), 1e-6);

        assertEquals(dae.value(m.dx, dae.time), dae.value(m.dy, dae.time), 1e-6);
        assertEquals(dae.value(m.x, dae.time), dae.value(m.y, dae.time), 1e-6);
    }
}
