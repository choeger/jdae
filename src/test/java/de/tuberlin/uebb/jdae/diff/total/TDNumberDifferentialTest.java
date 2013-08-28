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

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.tuberlin.uebb.jdae.diff.partial.PDNumber;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class TDNumberDifferentialTest {

    /* just some completely arbitrary points */

    /*
     * zsh: for ((n = 0; n <50; n++)) {float x; float y; float dx; float dy;
     * float ddx; float ddy; ((x = ($RANDOM % 100 -50) / 10.0, y = ($RANDOM %
     * 100 - 50) / 10.0, dx = ($RANDOM % 100 - 50) / 10.0, dy = ($RANDOM % 10 -
     * 5) / 10.0, ddx = ($RANDOM % 100 - 50) / 10.0, ddy = ($RANDOM % 100 - 50)
     * / 10.0)) ; echo ",{ $x, $y, $dx, $dy, $ddx, $ddy }"}
     */
    final static Object[][] example = {
            { 0, 0, 0, 0, 0, 0 },

            { 1, 1, 1, 1, 1, 1 },
            { 1, 0, 0, 0, 0, 0 },
            { 0, 1, 0, 0, 0, 0 },
            { 0, 0, 1, 0, 0, 0 },
            { 0, 0, 0, 1, 0, 0 },
            { 0, 0, 0, 0, 1, 0 },
            { 0, 0, 0, 0, 0, 1 },
            { -3.500000000e+00, 0.000000000e+00, -1.500000000e+00,
                    0.000000000e+00, -2.500000000e+00, 3.900000000e+00 },
            { 4.200000000e+00, 1.600000000e+00, 9.000000000e-01,
                    -1.000000000e-01, 1.000000000e-01, 1.400000000e+00 },
            { -3.800000000e+00, 4.800000000e+00, -4.900000000e+00,
                    -1.000000000e-01, -2.700000000e+00, 1.500000000e+00 },
            { 4.600000000e+00, 2.000000000e+00, -3.500000000e+00,
                    0.000000000e+00, 8.000000000e-01, 2.700000000e+00 },
            { -1.000000000e+00, -4.500000000e+00, -2.300000000e+00,
                    4.000000000e-01, -3.800000000e+00, 4.900000000e+00 },
            { -1.300000000e+00, 9.000000000e-01, -1.000000000e-01,
                    -3.000000000e-01, -3.600000000e+00, 2.500000000e+00 },
            { 4.400000000e+00, -4.300000000e+00, 2.300000000e+00,
                    -2.000000000e-01, 4.100000000e+00, 6.000000000e-01 },
            { -3.300000000e+00, -4.700000000e+00, 4.000000000e-01,
                    -4.000000000e-01, 3.000000000e+00, -4.100000000e+00 },
            { -3.400000000e+00, -4.200000000e+00, 2.900000000e+00,
                    -2.000000000e-01, -3.600000000e+00, 1.900000000e+00 },
            { -9.000000000e-01, 3.600000000e+00, -4.300000000e+00,
                    -5.000000000e-01, 5.000000000e-01, -3.100000000e+00 },
            { -5.000000000e+00, -2.600000000e+00, -4.000000000e+00,
                    -4.000000000e-01, -2.100000000e+00, -2.500000000e+00 },
            { 3.800000000e+00, -2.700000000e+00, 1.400000000e+00,
                    -4.000000000e-01, -4.200000000e+00, 3.700000000e+00 },
            { -3.300000000e+00, 8.000000000e-01, -2.700000000e+00,
                    -2.000000000e-01, -4.100000000e+00, -4.700000000e+00 },
            { -3.700000000e+00, 7.000000000e-01, -3.900000000e+00,
                    -1.000000000e-01, 3.000000000e-01, 7.000000000e-01 },
            { -2.400000000e+00, -2.400000000e+00, -7.000000000e-01,
                    -2.000000000e-01, 8.000000000e-01, -2.000000000e+00 },
            { 3.400000000e+00, 8.000000000e-01, 5.000000000e-01,
                    -1.000000000e-01, 2.200000000e+00, 3.400000000e+00 },
            { -3.100000000e+00, 1.000000000e+00, -1.100000000e+00,
                    0.000000000e+00, -2.800000000e+00, -3.000000000e-01 },
            { -4.700000000e+00, 2.100000000e+00, -4.500000000e+00,
                    3.000000000e-01, -4.300000000e+00, -4.000000000e-01 },
            { 1.100000000e+00, 2.000000000e-01, -1.400000000e+00,
                    -1.000000000e-01, -4.200000000e+00, 3.900000000e+00 },
            { 4.400000000e+00, -1.600000000e+00, -3.000000000e-01,
                    4.000000000e-01, 1.700000000e+00, -4.500000000e+00 },
            { -1.800000000e+00, 1.000000000e-01, 1.400000000e+00,
                    2.000000000e-01, -4.000000000e-01, 1.800000000e+00 },
            { -4.700000000e+00, 4.700000000e+00, -2.200000000e+00,
                    -1.000000000e-01, -3.700000000e+00, 0.000000000e+00 },
            { -2.900000000e+00, -2.000000000e-01, 4.000000000e-01,
                    4.000000000e-01, -4.400000000e+00, 1.100000000e+00 },
            { -4.500000000e+00, 4.900000000e+00, -5.000000000e-01,
                    -4.000000000e-01, -1.500000000e+00, 3.000000000e-01 },
            { 1.200000000e+00, 1.100000000e+00, 3.800000000e+00,
                    4.000000000e-01, 1.300000000e+00, 5.000000000e-01 },
            { -3.500000000e+00, 4.500000000e+00, -4.300000000e+00,
                    -4.000000000e-01, -3.600000000e+00, 3.500000000e+00 },
            { 2.900000000e+00, -3.300000000e+00, 3.200000000e+00,
                    2.000000000e-01, -2.700000000e+00, 4.500000000e+00 },
            { 4.000000000e+00, -6.000000000e-01, 2.500000000e+00,
                    1.000000000e-01, -4.700000000e+00, 3.100000000e+00 },
            { 1.900000000e+00, -9.000000000e-01, -3.800000000e+00,
                    -1.000000000e-01, 3.200000000e+00, -2.000000000e-01 },
            { 1.700000000e+00, 2.700000000e+00, -9.000000000e-01,
                    2.000000000e-01, 3.600000000e+00, -4.600000000e+00 },
            { -7.000000000e-01, -4.900000000e+00, 4.900000000e+00,
                    -3.000000000e-01, -6.000000000e-01, -3.700000000e+00 },
            { 1.700000000e+00, -2.700000000e+00, 1.200000000e+00,
                    -4.000000000e-01, 1.300000000e+00, 3.500000000e+00 },
            { 2.700000000e+00, 3.000000000e-01, 1.200000000e+00,
                    -3.000000000e-01, -2.100000000e+00, 1.500000000e+00 },
            { 1.600000000e+00, 4.800000000e+00, -1.200000000e+00,
                    -5.000000000e-01, -6.000000000e-01, 3.000000000e-01 },
            { 8.000000000e-01, -3.900000000e+00, -2.000000000e+00,
                    -5.000000000e-01, -1.900000000e+00, -3.400000000e+00 },
            { -4.600000000e+00, 2.400000000e+00, 0.000000000e+00,
                    1.000000000e-01, 3.800000000e+00, 4.400000000e+00 },
            { -1.000000000e-01, 3.700000000e+00, 0.000000000e+00,
                    -1.000000000e-01, 1.800000000e+00, -3.700000000e+00 },
            { -2.100000000e+00, -5.000000000e-01, 1.600000000e+00,
                    -4.000000000e-01, -2.000000000e+00, -2.300000000e+00 },
            { 3.900000000e+00, -2.200000000e+00, 7.000000000e-01,
                    2.000000000e-01, -1.200000000e+00, -4.900000000e+00 },
            { 3.000000000e+00, -2.100000000e+00, -6.000000000e-01,
                    -3.000000000e-01, 1.100000000e+00, 2.500000000e+00 },
            { 9.000000000e-01, 4.700000000e+00, 3.100000000e+00,
                    -4.000000000e-01, -1.700000000e+00, 1.900000000e+00 },
            { 1.700000000e+00, -3.500000000e+00, 6.000000000e-01,
                    2.000000000e-01, 9.000000000e-01, -2.500000000e+00 },
            { -2.000000000e+00, -3.000000000e+00, -4.800000000e+00,
                    3.000000000e-01, -3.800000000e+00, 1.400000000e+00 },
            { 5.000000000e-01, -1.700000000e+00, -2.600000000e+00,
                    -1.000000000e-01, 1.000000000e+00, 4.500000000e+00 },
            { -5.000000000e-01, 2.300000000e+00, -2.600000000e+00,
                    -3.000000000e-01, -3.500000000e+00, 3.500000000e+00 },
            { 4.700000000e+00, -4.400000000e+00, -3.600000000e+00,
                    -4.000000000e-01, 2.900000000e+00, 3.000000000e+00 },
            { 3.000000000e+00, -3.000000000e-01, 4.500000000e+00,
                    4.000000000e-01, 4.600000000e+00, 3.600000000e+00 },
            { -2.400000000e+00, -2.300000000e+00, -4.400000000e+00,
                    3.000000000e-01, 3.700000000e+00, 0.000000000e+00 },
            { -2.500000000e+00, -7.000000000e-01, 3.300000000e+00,
                    4.000000000e-01, -3.100000000e+00, 2.600000000e+00 },
            { -6.000000000e-01, 4.700000000e+00, 3.100000000e+00,
                    -5.000000000e-01, -3.100000000e+00, -2.200000000e+00 }

    };

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(example);
    }

    final private TDNumber f(final TDNumber x, final TDNumber y) {
        // f(x,y) = sin(2x + y^2)
        return inner(x, y).sin();
    }

    public TDNumber inner(final TDNumber x, final TDNumber y) {
        final TDNumber t = y.mult(y);
        return x.mult(2).add(t);
    }

    final private double df2() {
        return 2 * Math.cos(2 * x + Math.pow(y, 2))
                * (ddx + y * ddy + Math.pow(dy, 2)) - 4
                * Math.pow((dx + y * dy), 2) * Math.sin(2 * x + Math.pow(y, 2));
    }

    final private double df() {
        return 2 * Math.cos(2 * x + Math.pow(y, 2)) * (dx + y * dy);
    }

    final private double dfdx() {
        return -4 * Math.sin(2 * x + Math.pow(y, 2)) * (dx + y * dy);
    }

    final private double dfddx() {
        return 2 * Math.cos(2 * x + Math.pow(y, 2));
    }

    final double x;
    final double y;
    final double dx;
    final double dy;
    final double ddx;
    final double ddy;

    final TDNumber tx, ty;

    public TDNumberDifferentialTest(final double x, final double y,
            final double dx, final double dy, final double ddx, final double ddy) {
        super();

        this.x = x;
        this.y = y;
        this.dx = dx;
        this.dy = dy;
        this.ddx = ddx;
        this.ddy = ddy;

        final PDNumber _x = getPD(x, 0);
        final PDNumber _y = getPD(y, 1);
        final PDNumber _dx = getPD(dx, 2);
        final PDNumber _dy = getPD(dy, 3);
        final PDNumber _ddx = getPD(ddx, 4);
        final PDNumber _ddy = getPD(ddy, 5);

        this.tx = new TDNumber(new PDNumber[] { _x, _dx, _ddx });
        this.ty = new TDNumber(new PDNumber[] { _y, _dy, _ddy });

    }

    @Test
    public void test1stTotalDerivative() {
        final TDNumber result = f(tx, ty);
        assertEquals(df(), result.values[1].values[0], 10e-6);
    }

    @Test
    public void test1stPartialDerivativeX() {
        final TDNumber result = f(tx, ty);
        assertEquals(dfdx(), result.values[1].values[1], 10e-6);
    }

    @Test
    public void test1stPartialDerivativeDx() {
        final TDNumber result = f(tx, ty);
        assertEquals(dfddx(), result.values[1].values[3], 10e-6);
    }

    @Test
    public void test2ndTotalDerivative() {
        final TDNumber result = f(tx, ty);
        assertEquals(df2(), result.values[2].values[0], 10e-6);
    }

    @Test
    public void testInner2ndTotalDerivative() {
        final TDNumber result = inner(tx, ty);
        assertEquals(2 * ddx + 2 * dy * dy + 2 * y * ddy,
                result.values[2].values[0], 10e-6);
    }

    private final PDNumber getPD(double val, int idx) {
        final PDNumber n = new PDNumber(6);
        n.values[0] = val;
        n.values[idx + 1] = 1.0;
        return n;
    }
}
