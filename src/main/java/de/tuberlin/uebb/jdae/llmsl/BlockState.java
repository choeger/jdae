/**
 * 
 */
package de.tuberlin.uebb.jdae.llmsl;

import org.apache.commons.math3.analysis.differentiation.DerivativeStructure;

/**
 * @author choeger
 * 
 */
public class BlockState implements BlockVariable {

    public final GlobalVariable var;
    public final int firstDerivative;

    public BlockState(GlobalVariable var, int firstDerivative) {
        super();
        this.var = var;
        this.firstDerivative = firstDerivative;
    }

    @Override
    public double value(ExecutionContext ctxt) {
        return ctxt.loadD(var);
    }

    @Override
    public DerivativeStructure load(ExecutionContext ctxt) {
        final double[] number = ctxt.allocate();
        final int diff = ctxt.params[firstDerivative].der - var.der;

        for (int i = 0; i < ctxt.order; i++) {
            assert ctxt.data[var.index].length > var.der + i : String.format(
                    "%d-th derivative of %s is not allocated!", i, var);

            ctxt.setDt(i, ctxt.data[var.index][var.der + i], number);

            if (i >= diff) {
                final int relativeDerivative = firstDerivative + i - diff;
                assert ctxt.params[relativeDerivative].index == var.index : String
                        .format("%d-th derivative of %s is not iteratee of this block!",
                                i, var);
                ctxt.setDer(i, relativeDerivative, number);
            }
        }

        return ctxt.build(number);
    }

    @Override
    public BlockVariable der(ExecutionContext ctxt) {
        return this.der(ctxt, 1);
    }

    @Override
    public BlockVariable der(ExecutionContext ctxt, int n) {
        if (n == 0)
            return this;

        if (var.der + n >= ctxt.params[firstDerivative].der) {
            final int next = firstDerivative
                    + (n - ctxt.params[firstDerivative].der);
            assert ctxt.params[next].index == var.index : String
                    .format("The %d-th derivative of %s is not an iteratee of this block!",
                            n, var);
            return new BlockIteratee(ctxt.params[next], next);
        } else {
            return new BlockState(var.der(n), firstDerivative);
        }
    }

    @Override
    public GlobalVariable global() {
        return var;
    }
}
