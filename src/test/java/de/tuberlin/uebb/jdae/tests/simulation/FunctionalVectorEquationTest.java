package de.tuberlin.uebb.jdae.tests.simulation;

import java.util.List;

import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import de.tuberlin.uebb.jdae.builtins.ConstantLinearEquation;
import de.tuberlin.uebb.jdae.builtins.LinearFunctionalVectorEquation;
import de.tuberlin.uebb.jdae.builtins.SimpleVar;
import de.tuberlin.uebb.jdae.dae.FunctionalEquation;
import de.tuberlin.uebb.jdae.dae.FunctionalVectorEquation;
import de.tuberlin.uebb.jdae.dae.Unknown;

import static org.hamcrest.Matchers.is;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class FunctionalVectorEquationTest {

    @Test
    public void testIdentityMatrix() {

        final int[] targets = null;

        final List<Unknown> unknowns = Lists.newArrayList();
        for (int i = 0; i < 10; i++) {
            unknowns.add(new SimpleVar("a" + i));
        }

        final ConstantLinearEquation eq[] = new ConstantLinearEquation[10];
        for (int i = 0; i < 10; i++) {
            final List<Double> coefficients = Lists.newArrayList();
            for (int j = 0; j < 10; j++)
                coefficients.add(0.0);

            coefficients.set(i, 1.0);

            eq[i] = new ConstantLinearEquation(0.0, i, coefficients, unknowns);

        }

        final FunctionalVectorEquation veq = new LinearFunctionalVectorEquation(
                targets, new FunctionalEquation[10][10], eq);

        final double sol[] = veq.compute(0.0);

        assertThat(sol.length, is(10));
        for (int i = 0; i < 10; i++) {
            assertEquals(sol[i], i, 0.0001);
        }
    }

    @Test
    public void testSimpleSystem() {

        final int[] targets = null;

        final List<Unknown> unknowns = ImmutableList.of(
                (Unknown) new SimpleVar("x"), new SimpleVar("y"));

        final ConstantLinearEquation eq[] = new ConstantLinearEquation[] {
                new ConstantLinearEquation(-3.0, 0.0,
                        ImmutableList.of(2.0, 1.0), unknowns),
                new ConstantLinearEquation(-2.0, 0.0,
                        ImmutableList.of(1.0, 1.0), unknowns) };

        final FunctionalVectorEquation veq = new LinearFunctionalVectorEquation(
                targets, new FunctionalEquation[2][2], eq);

        for (double t : new double[] { 0.0, 1.0, 10.0, 1000 }) {

            final double sol[] = veq.compute(t);

            assertThat(sol.length, is(2));
            assertEquals(t, sol[0], 0.0001);
            assertEquals(t, sol[1], 0.0001);
        }
    }

    @Test
    public void testIdentityTimeMatrix() {

        final int[] targets = null;

        final List<Unknown> unknowns = Lists.newArrayList();
        for (int i = 0; i < 10; i++) {
            unknowns.add(new SimpleVar("a" + i));
        }

        final ConstantLinearEquation eq[] = new ConstantLinearEquation[10];
        for (int i = 0; i < 10; i++) {
            final List<Double> coefficients = Lists.newArrayList();
            for (int j = 0; j < 10; j++)
                coefficients.add(0.0);

            coefficients.set(i, 1.0);

            eq[i] = new ConstantLinearEquation(-i, 0.0, coefficients, unknowns);

        }

        final FunctionalVectorEquation veq = new LinearFunctionalVectorEquation(
                targets, new FunctionalEquation[10][10], eq);

        for (double t : new double[] { 0.0, 1.0, 10.0, 1000 }) {

            final double sol[] = veq.compute(t);

            assertThat(sol.length, is(10));
            for (int i = 0; i < 10; i++) {
                assertEquals(i * t, sol[i], 0.0001);
            }
        }
    }
}
