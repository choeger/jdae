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

import org.junit.Test;

import static org.hamcrest.Matchers.is;

import static org.junit.Assert.assertThat;

public class TDNumberIntegrationTest {

    public final TDNumber lengthConstraint(final TDNumber x, final TDNumber y) {
        return x.pow(2).add(y.pow(2)).add(-1);
    }

    /* just some completely abitrary points */
    final double[][][] example = { { { 0, 0, 0 }, { 0, 0, 0 } },
            { { 1, 0, 0 }, { 0, 0, 0 } }, { { 0, 0, 0 }, { 1, 0, 0 } },
            { { 0.5, 1, 0 }, { 1, 0, 0 } }, { { 0, 1, 0 }, { 1, 0, 0 } },
            { { 0, 0, 0 }, { 1, 1, 0 } }, { { 1, 0.5, 1 }, { 1, 0, 0.5 } },
            { { 0.5, 0, 1 }, { 1, 0, -1 } } };

    @Test
    public void testLengthConstraintPartialDerivative0() {
        final TDOperations ops = new TDOperations(0, 2);
        for (double[][] points : example) {
            final TDNumber x = ops.variable(0, points[0]);
            final TDNumber y = ops.variable(1, points[1]);

            final TDNumber l = lengthConstraint(x, y);

            assertThat(l.der(0, 0), is(2 * points[0][0]));
            assertThat(l.der(0, 1), is(2 * points[1][0]));
        }
    }

    @Test
    public void testLengthConstraintPartialDerivative1() {
        final TDOperations ops = new TDOperations(1, 4);
        for (double[][] points : example) {
            final TDNumber x = ops.variable(0, 1, points[0]);
            final TDNumber y = ops.variable(2, 1, points[1]);

            final TDNumber l = lengthConstraint(x, y);

            assertThat(l.der(0, 0), is(2 * points[0][0]));
            assertThat(l.der(0, 2), is(2 * points[1][0]));

            /* zero-derivative does not depend on derivatives */
            assertThat(l.der(0, 1), is(0.0));
            assertThat(l.der(0, 3), is(0.0));

            assertThat(l.der(1, 0), is(2 * points[0][1]));
            assertThat(l.der(1, 1), is(2 * points[0][0]));

            assertThat(l.der(1, 2), is(2 * points[1][1]));
            assertThat(l.der(1, 3), is(2 * points[1][0]));
        }
    }
}
