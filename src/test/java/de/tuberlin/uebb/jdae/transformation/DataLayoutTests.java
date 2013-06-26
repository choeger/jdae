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

package de.tuberlin.uebb.jdae.transformation;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

import de.tuberlin.uebb.jdae.llmsl.DataLayout;
import de.tuberlin.uebb.jdae.llmsl.GlobalVariable;

import static org.hamcrest.Matchers.is;

import static org.junit.Assert.assertThat;

public class DataLayoutTests {

    final GlobalVariable x = new GlobalVariable("x", 1, 0);
    final GlobalVariable y = new GlobalVariable("y", 2, 0);
    final DataLayout layout = new DataLayout(2, ImmutableList.of(x, y));

    final GlobalVariable ddx = x.der(2);

    final DataLayout layout2 = new DataLayout(2, ImmutableList.of(x, y, ddx));

    @Test
    public void testRowSize() {
        assertThat(layout.rows.length, is(2));
    }

    @Test
    public void testRowContent() {
        assertThat(layout.rows[0].derOrder, is(0));
        assertThat(layout.rows[1].derOrder, is(0));
    }

    @Test
    public void testDataAllocation() {
        final double[][] data = layout.alloc();
        assertThat(data.length, is(3));
        assertThat(data[0].length, is(1));
        assertThat(data[1].length, is(1));
        assertThat(data[2].length, is(1));
    }

    @Test
    public void testRowSizeWithDerivative() {
        assertThat(layout2.rows.length, is(2));
    }

    @Test
    public void testRowContentWithDerivative() {
        assertThat(layout2.rows[0].derOrder, is(2));
        assertThat(layout2.rows[1].derOrder, is(0));
    }

    @Test
    public void testDataAllocationWithDerivative() {
        final double[][] data = layout2.alloc();
        assertThat(data.length, is(3));
        assertThat(data[0].length, is(1));
        assertThat(data[1].length, is(3));
        assertThat(data[2].length, is(1));
    }

}
