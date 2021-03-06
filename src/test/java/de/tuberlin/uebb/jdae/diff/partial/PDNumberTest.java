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
package de.tuberlin.uebb.jdae.diff.partial;

import org.apache.commons.math3.util.FastMath;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;

import com.google.common.testing.EqualsTester;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.Matchers.not;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * @author choeger
 * 
 */
public class PDNumberTest {

    public static final double TOLERANCE = 10e-12;

    public static final PDOperations zeroParams = PDOperations.get(0);
    public static final PDOperations twoParams = PDOperations.get(2);

    public static Matcher<PDNumber> closeTo(final PDNumber number) {
        return new BaseMatcher<PDNumber>() {

            @Override
            public boolean matches(Object item) {
                final PDNumber testee = (PDNumber) item;

                return approximate(number, testee);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("value should be close to: " + number);
            }

        };
    }

    public static boolean approximate(final PDNumber number,
            final PDNumber testee) {
        if (testee.values.length != number.values.length)
            return false;

        for (int i = 0; i < testee.values.length; i++)
            if (Math.abs(testee.values[i] - number.values[i]) > TOLERANCE)
                return false;

        return true;
    }

    @Test
    public void testEqualsContract() {
        new EqualsTester()
                .addEqualityGroup(zeroParams.constant(1.0),
                        zeroParams.constant(1.0))
                .addEqualityGroup(zeroParams.constant(0.0),
                        zeroParams.constant(0.0))
                .addEqualityGroup(twoParams.constant(1.0),
                        twoParams.constant(1.0)).testEquals();
    }

    @Test
    public void testCreation() {
        assertThat(new PDNumber(0).values.length, is(1));
        assertThat(new PDNumber(1).values.length, is(2));
        assertThat(new PDNumber(10).values.length, is(11));

        assertThat(new PDNumber(new double[] { 0.0 }), is(new PDNumber(0)));
    }

    @Test
    public void testConstantLoading() {
        assertArrayEquals(new double[] { 1.0 },
                zeroParams.constant(1.0).values, 1e-8);

        assertArrayEquals(new double[] { 1.0, 0.0, 0.0 },
                twoParams.constant(1.0).values, 1e-8);

    }

    @Test
    public void testConstantAddition() {
        assertArrayEquals(new double[] { 2.0 },
                zeroParams.constant(1.0).add(zeroParams.constant(1.0)).values,
                1e-8);
    }

    @Test
    public void testConstantMultiplication() {
        assertArrayEquals(new double[] { 42.0 }, zeroParams.constant(21.0)
                .mult(zeroParams.constant(2.0)).values, 1e-8);
    }

    @Test
    public void testVariableLoading() {
        assertArrayEquals(new double[] { 2.0, 1.0, 0.0 },
                twoParams.variable(0, 2.0).values, 1e-8);

        assertArrayEquals(new double[] { 21.0, 0.0, 1.0 },
                twoParams.variable(1, 21.0).values, 1e-8);

    }

    @Test
    public void testVariableAdd() {
        assertArrayEquals(
                new double[] { 42.0, 1.0, 1.0 },
                twoParams.variable(0, 2.0).add(twoParams.variable(1, 40)).values,
                1e-8);

        assertArrayEquals(
                new double[] { 42.0, 2.0, 0.0 },
                twoParams.variable(0, 2.0).add(twoParams.variable(0, 40)).values,
                1e-8);

        assertArrayEquals(
                new double[] { 42.0, 0, 2.0 },
                twoParams.variable(1, 2.0).add(twoParams.variable(1, 40)).values,
                1e-8);

    }

    @Test
    public void testVariableMult() {
        assertArrayEquals(new double[] { 42.0, 2.0, 0.0 },
                twoParams.variable(0, 21.0).mult(twoParams.constant(2)).values,
                1e-8);

        assertArrayEquals(
                new double[] { 42.0, 21.0, 2.0 },
                twoParams.variable(0, 2.0).mult(twoParams.variable(1, 21)).values,
                1e-8);
    }

    private PDNumber f(PDNumber x) {
        return x.mult(x).add(1);
    }

    @Test
    public void testSquarePartialDerivatives() {
        assertEquals(0, f(twoParams.variable(0, 0)).der(0), 1e-8);
        assertEquals(2, f(twoParams.variable(0, 1)).der(0), 1e-8);
        assertEquals(4, f(twoParams.variable(0, 2)).der(0), 1e-8);
        assertEquals(6, f(twoParams.variable(0, 3)).der(0), 1e-8);
    }

    private PDNumber f_pow(PDNumber x) {
        return x.pow(2).add(1);
    }

    @Test
    public void testPowPartialDerivatives() {
        assertEquals(0, f_pow(twoParams.variable(0, 0)).der(0), 1e-8);
        assertEquals(2, f_pow(twoParams.variable(0, 1)).der(0), 1e-8);
        assertEquals(4, f_pow(twoParams.variable(0, 2)).der(0), 1e-8);
        assertEquals(6, f_pow(twoParams.variable(0, 3)).der(0), 1e-8);
    }

    private PDNumber radius(PDNumber x, PDNumber y) {
        return x.pow(2).add(y.pow(2)).add(-1);
    }

    @Test
    public void testRadiusPartialDerivatives() {
        assertEquals(
                0,
                radius(twoParams.variable(0, 0), twoParams.variable(1, 0)).der(
                        0), 1e-8);
        assertEquals(
                0,
                radius(twoParams.variable(0, 0), twoParams.variable(1, 0)).der(
                        1), 1e-8);

        assertEquals(
                2,
                radius(twoParams.variable(0, 1), twoParams.variable(1, 1)).der(
                        0), 1e-8);
        assertEquals(
                2,
                radius(twoParams.variable(0, 1), twoParams.variable(1, 1)).der(
                        1), 1e-8);

        assertEquals(
                4,
                radius(twoParams.variable(0, 1), twoParams.variable(1, 2)).der(
                        1), 1e-8);
        assertEquals(
                6,
                radius(twoParams.variable(0, 1), twoParams.variable(1, 3)).der(
                        1), 1e-8);

    }

    final PDNumber[] zeroParamsExamples = new PDNumber[] {
            zeroParams.constant(0.0), zeroParams.constant(1.0),
            zeroParams.constant(2.0), zeroParams.constant(4.0) };

    final PDNumber[] twoParamsExamples = new PDNumber[] {
            twoParams.constant(0.0), twoParams.constant(1.0),
            twoParams.constant(2.0), twoParams.constant(4.0),
            twoParams.variable(0, 5), twoParams.variable(1, 7) };

    @Test
    public void testZeroParamsAdditionAlgebraicIdentities() {
        testAdditionProperties(zeroParamsExamples);
    }

    @Test
    public void testTwoParamsAdditionAlgebraicIdentities() {
        testAdditionProperties(twoParamsExamples);
    }

    @Test
    public void testZeroParamsMultiplicationAlgebraicIdentities() {
        testMultiplicationProperties(zeroParamsExamples);
    }

    @Test
    public void testTwoParamsMultiplicationAlgebraicIdentities() {
        testMultiplicationProperties(twoParamsExamples);
    }

    @Test
    public void testTrigonometicBasics() {
        assertThat(zeroParams.constant(0.0).sin(), is(zeroParams.constant(0.0)));

        assertThat(zeroParams.constant(0.0).sin(), is(closeTo(zeroParams
                .constant(pihalf).cos())));
        assertThat(twoParams.constant(0.0).sin(), is(closeTo(twoParams
                .constant(pihalf).cos())));

        assertThat(zeroParams.constant(0.0).cos(), is(closeTo(zeroParams
                .constant(pihalf).sin())));
        assertThat(twoParams.constant(0.0).cos(), is(closeTo(twoParams
                .constant(pihalf).sin())));

    }

    @Test
    public void testZeroParamTrigonometricIdentities() {
        testTrigonometicIdentities(zeroParamsExamples);
    }

    @Test
    public void testTwoParamTrigonometricIdentities() {
        testTrigonometicIdentities(twoParamsExamples);
    }

    @Test
    public void testZeroParamsMutableAddition() {
        testMutableAdditionOperations(zeroParamsExamples);
    }

    @Test
    public void testTwoParamsMutableAddition() {
        testMutableAdditionOperations(twoParamsExamples);
    }

    @Test
    public void testZeroParamsMutableMultiplication() {
        testMutableMultOperations(zeroParamsExamples);
    }

    @Test
    public void testTwoParamsMutableMultiplication() {
        testMutableMultOperations(twoParamsExamples);
    }

    @Test
    public void testZeroParamsMutableTrigonometric() {
        testMutableTrigOperations(zeroParamsExamples);
    }

    @Test
    public void testTwoParamsMutableTrigonometric() {
        testMutableTrigOperations(twoParamsExamples);
    }

    private void testMutableAdditionOperations(PDNumber[] examples) {
        for (int i = 0; i < examples.length; i++) {
            final PDNumber x = examples[i];
            final PDNumber y = examples[(i + 1) % examples.length];

            final PDNumber m = new PDNumber(x.values).copy();
            assertThat(m.values, is(not(sameInstance(x.values))));

            m.m_add(y.values);
            assertThat(m, is(x.add(y)));
            m.m_add(y.values);
            assertThat(m, is(x.add(y.mult(2))));

            final PDNumber m2 = new PDNumber(x.values).copy();
            m2.m_add(0);
            assertThat(m2, is(x));
            m2.m_add(1.0);
            assertThat(m2, is(x.add(1.0)));
            m2.m_add(1.0);
            assertThat(m2, is(x.add(2.0)));

            final PDNumber m3 = new PDNumber(x.values).copy();
            m3.m_add(1);
            assertThat(m3, is(x.add(1)));
            m3.m_add(1);
            assertThat(m3, is(x.add(2)));
        }
    }

    private void testMutableMultOperations(PDNumber[] examples) {
        for (int i = 0; i < examples.length; i++) {
            final PDNumber x = examples[i];
            final PDNumber y = examples[(i + 1) % examples.length];

            final PDNumber m = new PDNumber(x.values).copy();
            assertThat(m.values, is(not(sameInstance(x.values))));

            m.m_mult(y.values);
            assertThat(m, is(x.mult(y)));
            m.m_mult(y.values);
            assertThat(m, is(x.mult(y.pow(2))));

            final PDNumber m2 = new PDNumber(x.values).copy();
            m2.m_mult(1);
            assertThat(m2, is(x));
            m2.m_mult(x.values);
            assertThat(m2, is(x.pow(2)));
            m2.m_mult(0);
            assertThat(m2, is(x.zero()));

            final PDNumber m3 = new PDNumber(x.values).copy();
            m3.m_add(1);
            assertThat(m3, is(x.add(1)));
            m3.m_add(1);
            assertThat(m3, is(x.add(2)));
        }
    }

    private void testMutableTrigOperations(PDNumber[] examples) {
        for (int i = 0; i < examples.length; i++) {
            final PDNumber x = examples[i];

            final PDNumber m = new PDNumber(x.values).copy();
            assertThat(m.values, is(not(sameInstance(x.values))));
            m.m_cos();
            assertThat(m, is(x.cos()));
            m.m_sin();
            assertThat(m, is(x.cos().sin()));

            final PDNumber m2 = new PDNumber(x.values).copy();
            m2.m_sin();
            assertThat(m2, is(x.sin()));
            m2.m_cos();
            assertThat(m2, is(x.sin().cos()));

        }
    }

    private void testMultiplicationProperties(PDNumber[] examples) {
        for (int i = 0; i < examples.length; i++) {
            final PDNumber x = examples[i];

            assertThat(x.mult(x.one()), is(x));
            assertThat(x.mult(x.zero()), is(x.zero()));
            assertThat(x.mult(x), is(x.pow(2)));
            assertThat(x.mult(x), is(x.pow(2)));
            assertThat(x.mult(x).mult(x), is(x.pow(3)));
            final PDNumber y = examples[(i + 1) % examples.length];

            assertThat(x.mult(y), is(y.mult(x)));
            assertThat(x.mult(x).mult(y), is(y.mult(x).mult(x)));
        }
    }

    private void testAdditionProperties(final PDNumber[] examples) {
        for (int i = 0; i < examples.length; i++) {
            final PDNumber x = examples[i];
            final PDNumber y = examples[(i + 1) % examples.length];

            assertThat(x, is(x));
            assertThat(x.add(x.zero()), is(x));
            assertThat(x.add(x), is(x.mult(2)));
            assertThat(x.add(x).add(x), is(x.mult(3)));

            assertThat(x.add(y), is(y.add(x)));
            assertThat(x.add(y).add(x), is(y.add(x).add(x)));
        }
    }

    final static double pihalf = FastMath.PI / 2;
    final static double twopi = FastMath.PI * 2;

    private void testTrigonometicIdentities(final PDNumber[] examples) {
        for (int i = 0; i < examples.length; i++) {
            final PDNumber x = examples[i];
            final PDNumber y = examples[(i + 1) % examples.length];

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
