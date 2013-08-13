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

import de.tuberlin.uebb.jdae.diff.partial.PDNumber;
import de.tuberlin.uebb.jdae.diff.partial.PDOperations;
import de.tuberlin.uebb.jdae.diff.total.TDOpsFactory.*;

/**
 * @author choeger
 * 
 */
public interface TDOperations {

    /**
     * @return the total derivation order of that implementation.
     */
    int order();

    /**
     * @return the underyling PDOperations
     */
    PDOperations subOps();

    /**
     * Invoke addition a + b and store result in target
     */
    void add(final PDNumber[] a, final PDNumber[] b,
		    final PDNumber[] target);

    
    /**
     * Invoke multiplaction a * b and store result in target
     */    
    void mult(final PDNumber[] a, final PDNumber[] b,
	      final PDNumber[] target);
    /**
     * Invoke the composition f ° a and store result in target.
     * Note: f must contain the total derivatives computed at a[0][0]
     */
    void compose(double f[], final PDNumber[] a, final PDNumber[] target);

    /**
     * @return the operations for the TDNumber of one less degree.
     */
    TDOperations smaller();


    /**
     * Invoke the sinus of a and store result in target
     */
    void sin(final PDNumber[] a, final PDNumber[] target);

    /**
     * Invoke the sinus of a and store result in target
     */
    void cos(final PDNumber[] a, final PDNumber[] target);

    /**
     * Calculate the nt-h power of a and store result in target
     */
    void pow(int n, final PDNumber[] a, final PDNumber[] target);

    /**
     * Calculate the nt-h power of a and store result in target
     */
    void pow(double n, final PDNumber[] a, final PDNumber[] target);

    /**
     * Create a matching constant variable (that is, a variable
     * with given total derivatives and no partial derivatives).
     * The values start at index offset in dt.
     */
    TDNumber constantVar(int offset, double... dt);

    /**
     * Create a matching constant variable (that is, a variable
     * with given total derivatives and no partial derivatives).
     * The derivatives are defined by the array dt.
     */
    TDNumber constant(double d, double... dt);

    /**
     * Load a variable with the given derivatives and
     * index idx.
     */
    TDNumber variable(int idx, double... der);

    /**
     * Load a mixed variable (that is a variable where the first
     * n derivatives are constant in the local function context).
     * The values in der starting at n will be interpreteted as 
     * local function variables.
     */
    TDNumber variable(int idx, int n, double... der);

    Product[][][] multOps();

    CompositionProduct[][][] compOps();

}
