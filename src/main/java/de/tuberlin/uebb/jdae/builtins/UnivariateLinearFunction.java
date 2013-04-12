package de.tuberlin.uebb.jdae.builtins;

import org.apache.commons.math3.analysis.UnivariateFunction;

import de.tuberlin.uebb.jdae.dae.FunctionalEquation;

public class UnivariateLinearFunction implements UnivariateFunction {

    public final double timeCoefficient;
    public final FunctionalEquation[] variables;
    public final double[] coefficients;
    public final double constant;

    @Override
    public double value(double time) {
        double val = time * timeCoefficient;
        for (int i = 0; i < variables.length; i++) {
            val += variables[i].value(time) * coefficients[i];
        }
        return val -= constant;
    }

    public UnivariateLinearFunction(double timeCoefficient,
            FunctionalEquation[] variables, double[] coefficients,
            double constant) {
        super();
        this.timeCoefficient = timeCoefficient;
        this.variables = variables;
        this.coefficients = coefficients;
        this.constant = constant;
    }

}
