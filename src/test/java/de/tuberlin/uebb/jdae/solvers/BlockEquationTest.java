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

package de.tuberlin.uebb.jdae.solvers;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import de.tuberlin.uebb.jdae.diff.total.TDNumber;
import de.tuberlin.uebb.jdae.llmsl.Block;
import de.tuberlin.uebb.jdae.llmsl.BlockEquation;
import de.tuberlin.uebb.jdae.llmsl.BlockVariable;
import de.tuberlin.uebb.jdae.llmsl.DataLayout;
import de.tuberlin.uebb.jdae.llmsl.ExecutionContext;
import de.tuberlin.uebb.jdae.llmsl.GlobalEquation;
import de.tuberlin.uebb.jdae.llmsl.GlobalVariable;
import de.tuberlin.uebb.jdae.llmsl.specials.ConstantGlobalEquation;
import de.tuberlin.uebb.jdae.simulation.SimulationOptions;
import static org.junit.Assert.assertEquals;

public final class BlockEquationTest {

    final GlobalVariable x = new GlobalVariable("x", 1, 0);
    final GlobalVariable y = new GlobalVariable("y", 2, 0);
    final DataLayout layout = new DataLayout(2, ImmutableList.of(x, y));

    final GlobalEquation eq1 = new GlobalEquation() {

        @Override
        public List<GlobalVariable> need() {
            return ImmutableList.of(x, y);
        }

        @Override
        public BlockEquation bind(
                final Map<GlobalVariable, BlockVariable> blockCtxt) {
            return new BlockEquation() {
                @Override
                public TDNumber exec(ExecutionContext m) {
                    return blockCtxt.get(x).load(m).pow(2)
                            .add(blockCtxt.get(y).load(m).pow(2)).subtract(1.0);
                }
            };
        }

    };

    final GlobalEquation eq2 = new ConstantGlobalEquation(y, 1.0);

    final double data[][] = layout.alloc();
    final Block b = new Block(data, layout, ImmutableSet.of(x, y),
            ImmutableSet.of(eq2.der(0), eq1.der(0)), SimulationOptions.DEFAULT);

    public void testSingularBlock() {
        data[1][0] = 0.0;
        data[2][0] = 1.0;

        b.exec();

        this.assertSolution();
    }

    @Test
    public void testFarStart() {
        testFromStartValue(0.001, 0.001);
    }

    @Test
    public void testCloseStart() {
        testFromStartValue(0.001, 0.999);
    }

    private void testFromStartValue(final double x, final double y) {
        data[1][0] = x;
        data[2][0] = y;

        b.exec();

        assertSolution();
    }

    private void assertSolution() {
        assertEquals(data[0][0], 0.0, Double.MIN_VALUE);
        assertEquals(0.0, data[1][0], 1e-3);
        assertEquals(1.0, data[2][0], 1e-3);
    }
}
