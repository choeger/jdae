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
package de.tuberlin.uebb.jdae.diff.total;

import org.apache.commons.math3.util.FastMath;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;

import com.google.common.testing.EqualsTester;

import de.tuberlin.uebb.jdae.diff.partial.PDNumberTest;

import static org.hamcrest.CoreMatchers.is;

import static org.junit.Assert.assertThat;

public class TDNumberTest {

    public static Matcher<TDNumber> closeTo(final TDNumber number) {
        return new BaseMatcher<TDNumber>() {

            @Override
            public boolean matches(Object item) {
                final TDNumber testee = (TDNumber) item;

                return approximate(number, testee);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("value should be close to: " + number);
            }

        };
    }

    public static boolean approximate(final TDNumber number,
            final TDNumber testee) {
        if (testee.values.length != number.values.length)
            return false;

        for (int i = 0; i < testee.values.length; i++)
            if (!PDNumberTest.approximate(testee.values[i], number.values[i]))
                return false;

        return true;
    }

    final TDOperations zeroParams0 = new TDOperations(0, 0);

    final TDNumber[] zeroZeroExamples = new TDNumber[] {
            zeroParams0.constant(0.0), zeroParams0.constant(1.0),
            zeroParams0.constant(2.0), zeroParams0.constant(4.0) };

    final TDOperations twoParams0 = new TDOperations(0, 2);

    final TDOperations twoParams2 = new TDOperations(2, 2);

    final TDNumber[] zeroTwoExamples = new TDNumber[] {
            twoParams0.constant(0.0), twoParams0.constant(1.0),
            twoParams0.constant(2.0), twoParams0.constant(4.0),
            twoParams0.variable(0, 5.0), twoParams0.variable(1, 3.0) };

    @Test
    public void testEqualsContract() {
        new EqualsTester()
                .addEqualityGroup(zeroParams0.constant(1.0),
                        zeroParams0.constant(1.0))
                .addEqualityGroup(zeroParams0.constant(0.0),
                        zeroParams0.constant(0.0))
                .addEqualityGroup(twoParams0.constant(1.0),
                        twoParams0.constant(1.0)).testEquals();
    }

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
    public void testManyMultiplications() {
        TDNumber result = twoParams2.constant(1.0);

        for (int i = 0; i < 1000000; ++i)
            result = result.mult(result);

        assertThat(result, is(twoParams2.constant(1.0)));
    }

    @Test
    public void testMultAlgebraicIdentitiesZeroParamsZeroOrder() {
        testMultiplicationProperties(zeroZeroExamples);
    }

    @Test
    public void testMultAlgebraicIdentitiesTwoParamsZeroOrder() {
        testMultiplicationProperties(zeroTwoExamples);
    }

    @Test
    public void testTrigonometicBasics() {
        assertThat(zeroParams0.constant(0.0).sin(),
                is(zeroParams0.constant(0.0)));

        assertThat(zeroParams0.constant(0.0).sin(), is(closeTo(zeroParams0
                .constant(pihalf).cos())));
        assertThat(twoParams0.constant(0.0).sin(), is(closeTo(twoParams0
                .constant(pihalf).cos())));

        assertThat(zeroParams0.constant(0.0).cos(), is(closeTo(zeroParams0
                .constant(pihalf).sin())));
        assertThat(twoParams0.constant(0.0).cos(), is(closeTo(twoParams0
                .constant(pihalf).sin())));

    }

    @Test
    public void testZeroParamTrigonometricIdentities() {
        testTrigonometicIdentities(zeroZeroExamples);
    }

    @Test
    public void testTwoParamTrigonometricIdentities() {
        testTrigonometicIdentities(zeroTwoExamples);
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

    final static double pihalf = FastMath.PI / 2;
    final static double twopi = FastMath.PI * 2;

    private void testTrigonometicIdentities(final TDNumber[] examples) {
        for (int i = 0; i < examples.length; i++) {
            final TDNumber x = examples[i];
            final TDNumber y = examples[(i + 1) % examples.length];

            assertThat(x.sin().mult(-1), is(x.mult(-1).sin()));
            assertThat(x.mult(-1).add(FastMath.PI).sin(), is(closeTo(x.sin())));
            assertThat(x.mult(-1).add(pihalf).sin(), is(closeTo(x.cos())));
            assertThat(x.add(twopi).sin(), is(closeTo(x.sin())));

            assertThat(x.mult(-1).cos(), is(x.cos()));
            assertThat(x.mult(-1).add(FastMath.PI).cos(), is(closeTo(x.cos()
                    .mult(-1))));
            assertThat(x.mult(-1).add(pihalf).cos(), is(closeTo(x.sin())));
            assertThat(x.add(twopi).cos(), is(closeTo(x.cos())));

            assertThat(x.sin().mult(y.cos()).add(y.sin().mult(x.cos())),
                    is(closeTo(x.add(y).sin())));

        }
    }
}
