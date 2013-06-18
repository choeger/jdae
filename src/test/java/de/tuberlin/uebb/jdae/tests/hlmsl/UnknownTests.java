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

package de.tuberlin.uebb.jdae.tests.hlmsl;

import org.junit.Test;

import com.google.common.collect.Ordering;

import de.tuberlin.uebb.jdae.hlmsl.Unknown;

import static org.hamcrest.CoreMatchers.is;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class UnknownTests {

    final Unknown x = new Unknown("x", 1, 0);
    final Unknown y = new Unknown("y", 2, 0);
    final Unknown dx = new Unknown("x", 1, 1);
    final Unknown d2x = new Unknown("x", 1, 2);
    final Unknown d3x = new Unknown("x", 1, 3);

    @Test
    public void testDer() {
        assertThat(x.der(), is(new Unknown("x", 1, 1)));
    }

    @Test
    public void testEquality() {
        assertTrue("equality should be based on indices and derivatives only",
                x.equals(new Unknown("not x", 1, 0)));
        assertFalse("in-equality should work on indices only",
                x.equals(new Unknown("x", 2, 0)));
        assertFalse("in-equality should work on derivatives only",
                x.equals(x.der()));

    }

    @Test
    public void testOrderingByDerivative() {
        /* Unknowns are ordered by lowest derivative-first */
        final Unknown min = Ordering.natural().min(x, dx);

        assertThat(min, is(x));

        final Unknown min2 = Ordering.natural().min(dx, y);
        assertThat(min2, is(y));

        final Unknown min3 = Ordering.natural().min(dx, y.der());
        assertThat(min3, is(dx));

    }

    @Test
    public void testDerivation() {
        assertThat(x.der(), is(dx));

        assertThat(x.der(2), is(d2x));

        assertThat(dx.der(), is(d2x));

        assertThat(dx.der(2), is(d3x));
    }

    @Test
    public void testBase() {
        assertThat(dx.base(), is(x));
    }

}
