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
package de.tuberlin.uebb.jdae.tests.diff;

import org.junit.Test;

import de.tuberlin.uebb.jdae.diff.partial.PDNumber;
import de.tuberlin.uebb.jdae.diff.partial.PDOperations;

import static org.hamcrest.CoreMatchers.is;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * @author choeger
 * 
 */
public class PDTest {

    public static final PDOperations zeroParams = PDOperations.get(0);

    public static final PDOperations twoParams = PDOperations.get(2);

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

    @Test
    public void testAlgebraicIdentities() {
        final PDNumber[] examples = new PDNumber[] { zeroParams.constant(0.0),
                zeroParams.constant(1.0), zeroParams.constant(2.0),
                zeroParams.constant(4.0) };

        for (int i = 0; i < examples.length; i++) {

            final PDNumber x = examples[i];
            assertThat(x, is(x));
            assertThat(x.mult(x.one()), is(x));

            // assertThat(x.sub(x), is(x.zero()));
            // assertThat(x.div(x), is(x.one()));

            assertThat(x.add(x), is(x.mult(2)));
            assertThat(x.pow(2), is(x.mult(x)));
            assertThat(x.pow(1), is(x));
            assertThat(x.pow(0), is(x.one()));

            assertThat(x.pow(2), is(x.pow(2.0)));
            assertThat(x.pow(2).pow(0.5), is(x));
        }
    }
}
