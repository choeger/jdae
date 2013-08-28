package de.tuberlin.uebb.jdae.diff.total.operations;

import de.tuberlin.uebb.jdae.diff.partial.PDNumber;
import de.tuberlin.uebb.jdae.diff.partial.PDOperations;

public class EmptyMultiplication implements Multiplication {

    @Override
    public void multInd(PDNumber[] a, PDNumber[] b, PDNumber[] target,
            PDOperations subOps) {
    }

    @Override
    public int countOrder() {
        return -1;
    }

}
