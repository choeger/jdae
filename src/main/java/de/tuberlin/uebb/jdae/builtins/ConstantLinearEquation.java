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
package de.tuberlin.uebb.jdae.builtins;

import java.util.Collection;
import java.util.List;

import org.apache.commons.math3.analysis.UnivariateFunction;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import de.tuberlin.uebb.jdae.dae.ConstantLinear;
import de.tuberlin.uebb.jdae.dae.FunctionalEquation;
import de.tuberlin.uebb.jdae.dae.SolvableDAE;
import de.tuberlin.uebb.jdae.dae.Unknown;

/**
 * @author choeger
 * 
 */
public final class ConstantLinearEquation implements ConstantLinear {

    public static final Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private final List<Unknown> variables = Lists.newArrayList();
        private final List<Double> coefficients = Lists.newArrayList();
        private double constant;
        private double time;

        public Builder addTime(final double d) {
            time += d;
            return this;
        }

        public Builder addConstant(final double d) {
            constant += d;
            return this;
        }

        public Builder add(final Unknown v, final double d) {
            if (v == null)
                throw new NullPointerException();
            variables.add(v);
            coefficients.add(d);
            return this;
        }

        public ConstantLinearEquation build() {
            return new ConstantLinearEquation(time, constant, coefficients,
                    variables);
        }
    }

    public final double time;
    public final double constant;
    public final double[] coefficients;
    public final List<Unknown> variables;

    public ConstantLinearEquation(double time, double rhs,
            List<Double> coefficients, List<Unknown> variables) {
        this.time = time;
        this.constant = rhs;
        this.coefficients = new double[coefficients.size()];
        for (int i = 0; i < coefficients.size(); i++) {
            this.coefficients[i] = coefficients.get(i);
        }
        this.variables = variables;
    }

    public String toString() {
        final StringBuilder b = new StringBuilder();
        b.append(String.format("(%f * time) + ", time));
        for (int i = 0; i < variables.size(); i++) {
            b.append(String.format("(%f * %s) + ", coefficients[i],
                    variables.get(i)));
        }
        b.delete(b.length() - 3, b.length());
        b.append(String.format(" = %f", constant));
        return b.toString();
    }

    @Override
    public List<Unknown> unknowns() {
        return variables;
    }

    @Override
    public double[] coefficients() {
        return coefficients;
    }

    @Override
    public double timeCoefficient() {
        return time;
    }

    @Override
    public double constant() {
        return constant;
    }

    @Override
    public FunctionalEquation specializeFor(Unknown unknown, SolvableDAE system) {
        final FunctionalEquation unknown2Function[] = new FunctionalEquation[variables
                .size()];
        for (int i = 0; i < variables.size(); i++) {
            unknown2Function[i] = system.get(variables.get(i));
        }
        return new LinearFunctionalEquation(system.variables.get(unknown),
                unknown2Function, coefficients, time, constant);
    }

    @Override
    public UnivariateFunction residual(SolvableDAE system) {
        final FunctionalEquation unknown2Function[] = new FunctionalEquation[variables
                .size()];
        for (int i = 0; i < variables.size(); i++) {
            unknown2Function[i] = system.get(variables.get(i));
        }
        return new UnivariateLinearFunction(time, unknown2Function,
                coefficients, constant);
    }

    @Override
    public Collection<Unknown> canSolveFor(Function<Unknown, Unknown> der) {
        return variables;
    }
}
