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

import java.util.Collection;
import java.util.List;

import org.apache.commons.math3.util.FastMath;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.collect.Lists;
import com.google.common.testing.EqualsTester;

import de.tuberlin.uebb.jdae.diff.partial.PDNumberTest;

import static org.hamcrest.CoreMatchers.is;

import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
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

    final static TDNumber[] examples(TDOperations ops) {
        return new TDNumber[] { ops.constant(0.0), ops.constant(1.0),
                ops.constant(2.0), ops.constant(4.0) };
    }

    final static TDOperations[] exampleOps = new TDOperations[] {
            TDOperations.getInstance(0, 0), TDOperations.getInstance(2, 0),
            TDOperations.getInstance(2, 2), TDOperations.getInstance(5, 10) };

    @Parameters(name = "TD Test on: {0}")
    public static Collection<Object[]> data() {
        final List<Object[]> p = Lists.newArrayList();
        for (TDOperations op : exampleOps)
            p.add(new Object[] { op });

        return p;
    }

    public final TDOperations ops;
    public final TDNumber[] examples;

    public TDNumberTest(TDOperations testOps) {
        this.ops = testOps;
        this.examples = examples(ops);
    }

    @Test
    public void testEqualsContract() {
        if (ops.subOps.params > 0) {
            final double[] der = new double[ops.order + 1];
            for (int i = 0; i < der.length; ++i)
                der[i] = 1.0 + i;

            new EqualsTester()
                    .addEqualityGroup(ops.constant(1.0), ops.constant(1.0))
                    .addEqualityGroup(ops.constant(0.0), ops.constant(0.0))
                    .addEqualityGroup(ops.variable(1, 0, der),
                            ops.variable(1, 0, der)).testEquals();
        } else {
            new EqualsTester()
                    .addEqualityGroup(ops.constant(1.0), ops.constant(1.0))
                    .addEqualityGroup(ops.constant(0.0), ops.constant(0.0))
                    .testEquals();
        }
    }

    @Test
    public void testAdditionProperties() {
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
    public void testManyMultiplications() {
        TDNumber result = ops.constant(1.0);

        for (int i = 0; i < 1000000; ++i)
            result = result.mult(result);

        assertThat(result, is(ops.constant(1.0)));
    }

    @Test
    public void testTrigonometicBasics() {
        assertThat(ops.constant(0.0).sin(), is(ops.constant(0.0)));

        assertThat(ops.constant(0.0).sin(), is(closeTo(ops.constant(pihalf)
                .cos())));

        assertThat(ops.constant(0.0).cos(), is(closeTo(ops.constant(pihalf)
                .sin())));
    }

    @Test
    public void testMultiplicationProperties() {
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

    @Test
    public void testTrigonometicIdentities() {
        for (int i = 0; i < examples.length; i++) {
            final TDNumber x = examples[i];
            final TDNumber y = examples[(i + 1) % examples.length];

            assertThat(x.sin().mult(-1), is(closeTo(x.mult(-1).sin())));
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
