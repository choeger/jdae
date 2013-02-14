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
package de.tuberlin.uebb.jdae.dae;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

/**
 * @author choeger
 * 
 */
public final class ConstantLinearEquation implements Equation {

    public static final Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private final Map<Unknown, Double> elements = Maps.newHashMap();
        private double constant;
        private double time;

        public Builder sub(final Unknown v, final double d) {
            return add(v, -d);
        }

        public Builder addTime(final double d) {
            time += d;
            return this;
        }

        public Builder subTime(final double d) {
            time -= d;
            return this;
        }

        public Builder add(final Unknown v, final double d) {
            if (v == null)
                throw new NullPointerException();
            if (elements.containsKey(v)) {
                elements.put(v, elements.get(v) + d);
            } else {
                elements.put(v, d);
            }
            return this;
        }

        public Builder sub(final double d) {
            constant -= d;
            return this;
        }

        public Builder add(final double d) {
            constant += d;
            return this;
        }

        public ConstantLinearEquation build() {
            final ImmutableList.Builder<Unknown> vars = ImmutableList.builder();
            final ImmutableList.Builder<Double> coeffs = ImmutableList
                    .builder();

            for (Entry<Unknown, Double> e : elements.entrySet()) {
                vars.add(e.getKey());
                coeffs.add(e.getValue());
            }

            return new ConstantLinearEquation(time, constant, coeffs.build(),
                    vars.build());
        }
    }

    @Override
    public Equation specialize(final SolvableDAE system) {
        return new SpecializedConstantLinearEquation(this, system);
    }

    public final double time;
    public final double constant;
    public final List<Double> coefficients;
    public final List<Unknown> variables;

    private ConstantLinearEquation(double time, double rhs,
            List<Double> coefficients, List<Unknown> variables) {
        this.time = time;
        this.constant = rhs;
        this.coefficients = coefficients;
        this.variables = variables;
    }

    @Override
    public Collection<Unknown> canSolveFor(Function<Unknown, Unknown> der) {
        return variables;
    }

    @Override
    public double solveFor(final int index, Unknown v,
            final SolvableDAE systemState) {
        double val = this.time * systemState.currentTime;
        int v_index = 0;
        for (int i = 0; i < variables.size(); i++) {
            final Unknown var = variables.get(i);
            if (var != v) {
                val += coefficients.get(i) * systemState.apply(var);
            } else {
                v_index = i;
            }
        }
        double d = (constant - val) / coefficients.get(v_index);
        systemState.logger.log(Level.INFO, "Returning {0}", d);
        return d;
    }

    @Override
    public double lhs(final SolvableDAE systemState) {
        double val = this.time * systemState.currentTime;
        for (int i = 0; i < variables.size(); i++) {
            val += coefficients.get(i) * systemState.apply(variables.get(i));
        }
        return val;
    }

    @Override
    public double rhs(final SolvableDAE systemState) {
        return constant;
    }

    public String toString() {
        final StringBuilder b = new StringBuilder();
        b.append(String.format("(%f * time) + ", time));
        for (int i = 0; i < variables.size(); i++) {
            b.append(String.format("(%f * %s) + ", coefficients.get(i),
                    variables.get(i)));
        }
        b.delete(b.length() - 3, b.length());
        b.append(String.format(" = %f", constant));
        return b.toString();
    }
}
