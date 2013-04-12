package de.tuberlin.uebb.jdae.tests.simulation;

import java.util.logging.Level;

import org.junit.Test;

import de.tuberlin.uebb.jdae.dae.SolvableDAE;
import de.tuberlin.uebb.jdae.examples.BouncingBallArray;
import de.tuberlin.uebb.jdae.simulation.DefaultSimulationRuntime;
import de.tuberlin.uebb.jdae.simulation.SimulationRuntime;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThan;

import static org.junit.Assert.assertThat;

public class BouncingArrayTest {

    @Test
    public void test() {
        final SimulationRuntime runtime = new DefaultSimulationRuntime();

        final BouncingBallArray model = new BouncingBallArray(runtime);

        final SolvableDAE dae = runtime.causalise(model.equations());

        runtime.simulateVariableStep(dae, model.events(dae), model.initials(),
                1.0, Double.MAX_VALUE, Double.MAX_VALUE, 1e-6, 1e-6);

        /*
         * runtime.simulateFixedStep(dae, model.events(dae), model.initials(),
         * 10, 10000);
         */

        dae.logger.log(
                Level.INFO,
                "Evaluations:  {0}, {1}, {2}",
                new Object[] { dae.value(model.balls[0].e, dae.time),
                        dae.value(model.balls[1].e, dae.time),
                        dae.value(model.balls[2].e, dae.time) });
        dae.logger.log(Level.INFO, "Accepted steps:  {0}",
                runtime.lastResults().results.size());
        assertThat(dae.value(model.balls[0].e, dae.time),
                is(greaterThan(dae.value(model.balls[1].e, dae.time))));
        assertThat(model.balls[0].events, is(1));
        assertThat(model.balls[1].events, is(0));
        assertThat(model.balls[2].events, is(0));

    }
}
