/**
 * 
 */
package de.tuberlin.uebb.jdae.llmsl;

import org.apache.commons.math3.analysis.differentiation.DerivativeStructure;

/**
 * @author choeger
 * 
 */
public final class BlockConstant implements BlockVariable {

    public final GlobalVariable var;

    public String toString() {
        return "(constant: " + var + ")";
    }

    @Override
    public double value(ExecutionContext ctxt) {
        return ctxt.loadD(var);
    }

    @Override
    public DerivativeStructure load(ExecutionContext ctxt) {
        final double[] number = ctxt.allocate();
        for (int i = 0; i <= ctxt.order; i++)
            ctxt.setDt(i, ctxt.data[var.index][var.der + i], number);
        return ctxt.build(number);
    }

    @Override
    public BlockVariable der(ExecutionContext ctxt) {
        return der(ctxt, 1);
    }

    public BlockConstant(GlobalVariable var) {
        super();
        this.var = var;
    }

    @Override
    public BlockVariable der(ExecutionContext ctxt, int n) {
        if (n == 0)
            return this;
        else
            return new BlockConstant(var.der(n));
    }

    @Override
    public GlobalVariable global() {
        return var;
    }

}
