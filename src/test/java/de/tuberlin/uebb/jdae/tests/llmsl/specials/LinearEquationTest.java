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

package de.tuberlin.uebb.jdae.tests.llmsl.specials;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

import de.tuberlin.uebb.jdae.llmsl.DataLayout;
import de.tuberlin.uebb.jdae.llmsl.ExecutableDAE;
import de.tuberlin.uebb.jdae.llmsl.GlobalVariable;
import de.tuberlin.uebb.jdae.llmsl.IBlock;
import de.tuberlin.uebb.jdae.llmsl.specials.LinearGlobalEquation;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LinearEquationTest {

    final GlobalVariable b = new GlobalVariable("b", 1, 0);
    final GlobalVariable h = new GlobalVariable("h", 2, 0);

    final LinearGlobalEquation eq = new LinearGlobalEquation(0.0, -0.5,
            new double[] { 1, -1 }, ImmutableList.of(b, h));

    final DataLayout layout = new DataLayout(2, ImmutableList.of(b, h));

    @Test
    public void testSpecializationAbility() {
        assertTrue(eq + " could not be specialized for b!",
                eq.canSpecializeFor(b));
        assertTrue(eq + " could not be specialized for h!",
                eq.canSpecializeFor(h));
    }

    @Test
    public void testSpecialization() {
        final ExecutableDAE dae = new ExecutableDAE(layout,
                new IBlock[] { null }, new IBlock[] {}, ImmutableList.of(h));

        /* don't try this at home! */
        dae.blocks[0] = eq.specializeFor(b, dae);

        dae.set(h, 1);
        dae.blocks[0].exec();

        assertEquals(0.5, dae.load(b), 1e-8);
    }

}
