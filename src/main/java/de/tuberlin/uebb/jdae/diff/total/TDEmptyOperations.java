/**
 * 
 */
package de.tuberlin.uebb.jdae.diff.total;

import de.tuberlin.uebb.jdae.diff.partial.PDNumber;
import de.tuberlin.uebb.jdae.diff.partial.PDOperations;
import de.tuberlin.uebb.jdae.diff.total.TDOpsFactory.CompositionProduct;
import de.tuberlin.uebb.jdae.diff.total.TDOpsFactory.Product;

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

    /*
     * (nicht-Javadoc)
     * 
     * @see de.tuberlin.uebb.jdae.diff.total.TDOperations#subOps()
     */
    @Override
    public PDOperations subOps() {
        return null;
    }

    /*
     * (nicht-Javadoc)
     * 
     * @see
     * de.tuberlin.uebb.jdae.diff.total.TDOperations#add(de.tuberlin.uebb.jdae
     * .diff.partial.PDNumber[], de.tuberlin.uebb.jdae.diff.partial.PDNumber[],
     * de.tuberlin.uebb.jdae.diff.partial.PDNumber[])
     */
    @Override
    public void add(PDNumber[] a, PDNumber[] b, PDNumber[] target) {
    }

    /*
     * (nicht-Javadoc)
     * 
     * @see
     * de.tuberlin.uebb.jdae.diff.total.TDOperations#mult(de.tuberlin.uebb.jdae
     * .diff.partial.PDNumber[], de.tuberlin.uebb.jdae.diff.partial.PDNumber[],
     * de.tuberlin.uebb.jdae.diff.partial.PDNumber[])
     */
    @Override
    public void mult(PDNumber[] a, PDNumber[] b, PDNumber[] target) {
    }

    /*
     * (nicht-Javadoc)
     * 
     * @see de.tuberlin.uebb.jdae.diff.total.TDOperations#compose(double[],
     * de.tuberlin.uebb.jdae.diff.partial.PDNumber[],
     * de.tuberlin.uebb.jdae.diff.partial.PDNumber[])
     */
    @Override
    public void compose(double[] f, PDNumber[] a, PDNumber[] target) {
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
     * .diff.partial.PDNumber[], de.tuberlin.uebb.jdae.diff.partial.PDNumber[])
     */
    @Override
    public void sin(PDNumber[] a, PDNumber[] target) {
    }

    /*
     * (nicht-Javadoc)
     * 
     * @see
     * de.tuberlin.uebb.jdae.diff.total.TDOperations#cos(de.tuberlin.uebb.jdae
     * .diff.partial.PDNumber[], de.tuberlin.uebb.jdae.diff.partial.PDNumber[])
     */
    @Override
    public void cos(PDNumber[] a, PDNumber[] target) {
    }

    /*
     * (nicht-Javadoc)
     * 
     * @see de.tuberlin.uebb.jdae.diff.total.TDOperations#pow(int,
     * de.tuberlin.uebb.jdae.diff.partial.PDNumber[],
     * de.tuberlin.uebb.jdae.diff.partial.PDNumber[])
     */
    @Override
    public void pow(int n, PDNumber[] a, PDNumber[] target) {
    }

    /*
     * (nicht-Javadoc)
     * 
     * @see de.tuberlin.uebb.jdae.diff.total.TDOperations#pow(double,
     * de.tuberlin.uebb.jdae.diff.partial.PDNumber[],
     * de.tuberlin.uebb.jdae.diff.partial.PDNumber[])
     */
    @Override
    public void pow(double n, PDNumber[] a, PDNumber[] target) {
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
    public Product[][][] multOps() {
        return new Product[][][] { { {} } };
    }

    /*
     * (nicht-Javadoc)
     * 
     * @see de.tuberlin.uebb.jdae.diff.total.TDOperations#compOps()
     */
    @Override
    public CompositionProduct[][][] compOps() {
        return new CompositionProduct[][][] { { {} } };
    }

}
