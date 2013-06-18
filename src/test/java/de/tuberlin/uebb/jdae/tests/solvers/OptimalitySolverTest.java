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

package de.tuberlin.uebb.jdae.tests.solvers;

import org.junit.Test;

import de.tuberlin.uebb.jdae.examples.FactorialEquation;
import de.tuberlin.uebb.jdae.examples.SquareRootEquation;
import de.tuberlin.uebb.jdae.solvers.OptimalitySolver;
import static org.junit.Assert.assertEquals;

public class OptimalitySolverTest {

    final OptimalitySolver solver = new OptimalitySolver();

    @Test
    public void test6() {
        final FactorialEquation f = new FactorialEquation(6.0);
        assertEquals(3.0,
                solver.solve(Integer.MAX_VALUE, f, new double[] { 2.0 })[0],
                1e-6);
    }

    @Test
    public void test24() {
        final FactorialEquation f = new FactorialEquation(24.0);
        assertEquals(4.0,
                solver.solve(Integer.MAX_VALUE, f, new double[] { 2.0 })[0],
                1e-6);
    }

    @Test
    public void test120() {
        final FactorialEquation f = new FactorialEquation(120.0);
        assertEquals(5.0,
                solver.solve(Integer.MAX_VALUE, f, new double[] { 3.0 })[0],
                1e-6);
    }

    @Test
    public void test720() {
        final FactorialEquation f = new FactorialEquation(720.0);
        assertEquals(6.0,
                solver.solve(Integer.MAX_VALUE, f, new double[] { 4.0 })[0],
                1e-6);
    }

    @Test
    public void test5040() {
        final FactorialEquation f = new FactorialEquation(5040.0);
        assertEquals(7.0,
                solver.solve(Integer.MAX_VALUE, f, new double[] { 5.0 })[0],
                1e-6);
    }

    @Test
    public void test40320() {
        final FactorialEquation f = new FactorialEquation(40320.0);
        assertEquals(8.0,
                solver.solve(Integer.MAX_VALUE, f, new double[] { 6.0 })[0],
                1e-6);
    }

    @Test
    public void testSqrt1() {
        final SquareRootEquation f = new SquareRootEquation(1.0);
        assertEquals(0.0,
                solver.solve(Integer.MAX_VALUE, f, new double[] { 1.0 })[0],
                1e-3);
    }

    @Test
    public void testSqrtNeg1() {
        final SquareRootEquation f = new SquareRootEquation(-1.0);
        assertEquals(0.0,
                solver.solve(Integer.MAX_VALUE, f, new double[] { 1.0 })[0],
                1e-3);
    }

    @Test
    public void testSqrt0() {
        final SquareRootEquation f = new SquareRootEquation(0);
        assertEquals(
                1.0,
                solver.solve(Integer.MAX_VALUE, f, new double[] { 0.00001 })[0],
                1e-3);
        assertEquals(
                -1.0,
                solver.solve(Integer.MAX_VALUE, f, new double[] { -0.00001 })[0],
                1e-3);
    }
}
