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

import de.tuberlin.uebb.jdae.diff.total.operations.Composition;
import de.tuberlin.uebb.jdae.diff.total.operations.Multiplication;

/**
 * @author choeger
 * 
 */
public class TDEmptyOperations implements TDOperations {

    /*
     * (nicht-Javadoc)
     * 
     * @see de.tuberlin.uebb.jdae.diff.total.TDOperations#order()
     */
    @Override
    public int order() {
        return 0;
    }
    
    @Override
    public int params() {
        return 0;
    }
    /*
     * (nicht-Javadoc)
     * 
     * @see
     * de.tuberlin.uebb.jdae.diff.total.TDOperations#add(de.tuberlin.uebb.jdae
     * .diff.partial.double[], de.tuberlin.uebb.jdae.diff.partial.double[],
     * de.tuberlin.uebb.jdae.diff.partial.double[])
     */
    @Override
    public void add(double[] a, double[] b, double[] target) {
    }

    /*
     * (nicht-Javadoc)
     * 
     * @see
     * de.tuberlin.uebb.jdae.diff.total.TDOperations#mult(de.tuberlin.uebb.jdae
     * .diff.partial.double[], de.tuberlin.uebb.jdae.diff.partial.double[],
     * de.tuberlin.uebb.jdae.diff.partial.double[])
     */
    @Override
    public void mult(double[] a, double[] b, double[] target) {
    }

    /*
     * (nicht-Javadoc)
     * 
     * @see de.tuberlin.uebb.jdae.diff.total.TDOperations#compose(double[],
     * de.tuberlin.uebb.jdae.diff.partial.double[],
     * de.tuberlin.uebb.jdae.diff.partial.double[])
     */
    @Override
    public void compose(double[] f, double[] a, double[] target) {
    }

    /*
     * (nicht-Javadoc)
     * 
     * @see de.tuberlin.uebb.jdae.diff.total.TDOperations#smaller()
     */
    @Override
    public TDOperations smaller() {
        return this;
    }

    /*
     * (nicht-Javadoc)
     * 
     * @see
     * de.tuberlin.uebb.jdae.diff.total.TDOperations#sin(de.tuberlin.uebb.jdae
     * .diff.partial.double[], de.tuberlin.uebb.jdae.diff.partial.double[])
     */
    @Override
    public void sin(double[] a, double[] target) {
    }

    /*
     * (nicht-Javadoc)
     * 
     * @see
     * de.tuberlin.uebb.jdae.diff.total.TDOperations#cos(de.tuberlin.uebb.jdae
     * .diff.partial.double[], de.tuberlin.uebb.jdae.diff.partial.double[])
     */
    @Override
    public void cos(double[] a, double[] target) {
    }

    /*
     * (nicht-Javadoc)
     * 
     * @see de.tuberlin.uebb.jdae.diff.total.TDOperations#pow(int,
     * de.tuberlin.uebb.jdae.diff.partial.double[],
     * de.tuberlin.uebb.jdae.diff.partial.double[])
     */
    @Override
    public void pow(int n, double[] a, double[] target) {
    }

    /*
     * (nicht-Javadoc)
     * 
     * @see de.tuberlin.uebb.jdae.diff.total.TDOperations#pow(double,
     * de.tuberlin.uebb.jdae.diff.partial.double[],
     * de.tuberlin.uebb.jdae.diff.partial.double[])
     */
    @Override
    public void pow(double n, double[] a, double[] target) {
    }

    /*
     * (nicht-Javadoc)
     * 
     * @see de.tuberlin.uebb.jdae.diff.total.TDOperations#constantVar(int,
     * double[])
     */
    @Override
    public TDNumber constantVar(int offset, double... dt) {
        return null;
    }

    /*
     * (nicht-Javadoc)
     * 
     * @see de.tuberlin.uebb.jdae.diff.total.TDOperations#constant(double,
     * double[])
     */
    @Override
    public TDNumber constant(double d, double... dt) {
        return null;
    }

    /*
     * (nicht-Javadoc)
     * 
     * @see de.tuberlin.uebb.jdae.diff.total.TDOperations#variable(int,
     * double[])
     */
    @Override
    public TDNumber variable(int idx, double... der) {
        return null;
    }

    /*
     * (nicht-Javadoc)
     * 
     * @see de.tuberlin.uebb.jdae.diff.total.TDOperations#variable(int, int,
     * double[])
     */
    @Override
    public TDNumber variable(int idx, int n, double... der) {
        return null;
    }

    /*
     * (nicht-Javadoc)
     * 
     * @see de.tuberlin.uebb.jdae.diff.total.TDOperations#multOps()
     */
    @Override
    public Multiplication multOps() {
        return null;
    }

    /*
     * (nicht-Javadoc)
     * 
     * @see de.tuberlin.uebb.jdae.diff.total.TDOperations#compOps()
     */
    @Override
    public Composition compOps() {
        return null;
    }

}
