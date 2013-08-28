package de.tuberlin.uebb.jdae.diff.total.operations;

import de.tuberlin.uebb.jdae.diff.partial.PDNumber;
import de.tuberlin.uebb.jdae.diff.partial.PDOperations;

public class EmptyComposition implements Composition {

    @Override
    public void compInd(double[] f, PDNumber[] a, PDNumber[] target,
            PDOperations subOps) {
    }

    @Override
    public int countOrder() {
        return -1;
    }

    @Override
    public Composition get(int x) {
        return this;
    }

    @Override
    public CompositionProduct[] value() {
        return new CompositionProduct[0];
    }

    @Override
    public CompositionProduct[] partialDerivative() {
        return new CompositionProduct[0];
    }

}
