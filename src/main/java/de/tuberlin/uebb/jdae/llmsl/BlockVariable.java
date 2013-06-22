package de.tuberlin.uebb.jdae.llmsl;

import org.apache.commons.math3.analysis.differentiation.DerivativeStructure;

public interface BlockVariable {

    public double value(ExecutionContext ctxt);

    public DerivativeStructure load(ExecutionContext ctxt);

    public BlockVariable der(ExecutionContext ctxt);

    public BlockVariable der(ExecutionContext ctxt, int n);

    public GlobalVariable global();
}
