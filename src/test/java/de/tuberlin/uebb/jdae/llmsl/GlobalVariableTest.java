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

import de.tuberlin.uebb.jdae.llmsl.GlobalVariable;

import static org.hamcrest.CoreMatchers.is;

import static org.junit.Assert.assertThat;

public class GlobalVariableTest {

    final GlobalVariable x = new GlobalVariable("x", 1, 0);
    final GlobalVariable dx = new GlobalVariable("x", 1, 1);
    final GlobalVariable d2x = new GlobalVariable("x", 1, 2);
    final GlobalVariable d3x = new GlobalVariable("x", 1, 3);

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
