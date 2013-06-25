package de.tuberlin.uebb.jdae.tests.diff;

import org.junit.Test;

import de.tuberlin.uebb.jdae.diff.total.TDNumber;
import de.tuberlin.uebb.jdae.diff.total.TDOperations;

import static org.hamcrest.CoreMatchers.is;

import static org.junit.Assert.assertThat;

public class TDTest {

    final TDOperations zeroParams0 = new TDOperations(0, 0);

    final TDNumber[] zeroZeroExamples = new TDNumber[] {
            zeroParams0.constant(0.0), zeroParams0.constant(1.0),
            zeroParams0.constant(2.0), zeroParams0.constant(4.0) };

    final TDOperations twoParams0 = new TDOperations(0, 2);

    final TDNumber[] zeroTwoExamples = new TDNumber[] {
            twoParams0.constant(0.0), twoParams0.constant(1.0),
            twoParams0.constant(2.0), twoParams0.constant(4.0),
            twoParams0.variable(0, 5.0), twoParams0.variable(1, 3.0) };

    private void testAdditionProperties(final TDNumber[] examples) {
        for (int i = 0; i < examples.length; i++) {
            final TDNumber x = examples[i];
            assertThat(x, is(x));
            assertThat(x.add(x.zero()), is(x));
            assertThat(x.add(x), is(x.mult(2)));
            assertThat(x.add(x).add(x), is(x.mult(3)));

            final TDNumber y = examples[(i + 1) % examples.length];
            assertThat(x.add(y), is(y.add(x)));
            assertThat(x.add(y).add(x), is(y.add(x).add(x)));
        }
    }

    @Test
    public void testAddAlgebraicIdentitiesZeroParamsZeroOrder() {
        testAdditionProperties(zeroZeroExamples);
    }

    @Test
    public void testAddAlgebraicIdentitiesTwoParamsZeroOrder() {
        testAdditionProperties(zeroTwoExamples);
    }

    @Test
    public void testMultAlgebraicIdentitiesZeroParamsZeroOrder() {
        testMultiplicationProperties(zeroZeroExamples);
    }

    @Test
    public void testMultAlgebraicIdentitiesTwoParamsZeroOrder() {
        testMultiplicationProperties(zeroTwoExamples);
    }

    private void testMultiplicationProperties(TDNumber[] examples) {
        for (int i = 0; i < examples.length; i++) {
            final TDNumber x = examples[i];

            assertThat(x.mult(x.one()), is(x));
            assertThat(x.mult(x.zero()), is(x.zero()));
            assertThat(x.mult(x), is(x.pow(2)));
            assertThat(x.mult(x), is(x.pow(2)));
            assertThat(x.mult(x).mult(x), is(x.pow(3)));
            final TDNumber y = examples[(i + 1) % examples.length];

            assertThat(x.mult(y), is(y.mult(x)));
            assertThat(x.mult(x).mult(y), is(y.mult(x).mult(x)));
        }
    }
}
