package de.tuberlin.uebb.jdae.tests.solvers;

import org.junit.Test;

import de.tuberlin.uebb.jdae.examples.FactorialEquation;
import de.tuberlin.uebb.jdae.examples.SquareRootEquation;
import de.tuberlin.uebb.jdae.solvers.OptimalitySolver;
import static org.junit.Assert.assertEquals;

public class OptimalitySolverTest {

    final OptimalitySolver solver = new OptimalitySolver();

    @Test
    public void test1() {
        final FactorialEquation f = new FactorialEquation(1.0);
        assertEquals(1.0,
                solver.solve(Integer.MAX_VALUE, f, new double[] { 1.1 })[0],
                1e-6);
    }

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
                solver.solve(Integer.MAX_VALUE, f, new double[] { 2.0 })[0],
                1e-6);
    }

    @Test
    public void test720() {
        final FactorialEquation f = new FactorialEquation(720.0);
        assertEquals(6.0,
                solver.solve(Integer.MAX_VALUE, f, new double[] { 2.0 })[0],
                1e-6);
    }

    @Test
    public void test5040() {
        final FactorialEquation f = new FactorialEquation(5040.0);
        assertEquals(7.0,
                solver.solve(Integer.MAX_VALUE, f, new double[] { 2.0 })[0],
                1e-6);
    }

    @Test
    public void test40320() {
        final FactorialEquation f = new FactorialEquation(40320.0);
        assertEquals(8.0,
                solver.solve(Integer.MAX_VALUE, f, new double[] { 2.0 })[0],
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
