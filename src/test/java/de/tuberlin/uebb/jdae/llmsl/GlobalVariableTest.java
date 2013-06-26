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

package de.tuberlin.uebb.jdae.llmsl;

import org.junit.Test;

import com.google.common.testing.EqualsTester;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.lessThan;

import static org.junit.Assert.assertThat;

public class GlobalVariableTest {

    final GlobalVariable x = new GlobalVariable("x", 1, 0);
    final GlobalVariable dx = new GlobalVariable("x", 1, 1);
    final GlobalVariable d2x = new GlobalVariable("x", 1, 2);
    final GlobalVariable d3x = new GlobalVariable("x", 1, 3);

    final GlobalVariable y = new GlobalVariable("y", 2, 0);

    final GlobalVariable z = new GlobalVariable("z", 3, 0);

    @Test
    public void testDerivation() {
        assertThat(x.der(), is(dx));

        assertThat(x.der(2), is(d2x));

        assertThat(dx.der(), is(d2x));

        assertThat(dx.der(2), is(d3x));
    }

    @Test
    public void testEquals() {
        final GlobalVariable x_renamed = new GlobalVariable("X", 1, 0);

        new EqualsTester().addEqualityGroup(x, x_renamed, x.der().integrate())
                .addEqualityGroup(x.der(), x.der(1), x_renamed.der())
                .testEquals();
    }

    @Test
    public void testIsOneOf() {
        assertThat(x.isOneOf(x, y, z), is(true));
        assertThat(x.isOneOf(y, z), is(false));
        assertThat(x.isOneOf(x, y, x, z), is(false));
    }

    @Test
    public void testBase() {
        assertThat(dx.base(), is(x));
    }

    @Test
    public void testSorting() {
        assertThat(x, is(lessThan(dx)));
        assertThat(dx, is(lessThan(d2x)));
        assertThat(x, is(lessThan(d2x)));

        assertThat(d2x, is(lessThan(y)));
        assertThat(y, is(lessThan(z)));
    }
}
