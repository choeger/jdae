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
package de.tuberlin.uebb.jdae.llmsl.specials;

import java.util.List;
import java.util.Map;

import org.apache.commons.math3.analysis.differentiation.DerivativeStructure;

import com.google.common.base.Functions;
import com.google.common.collect.Lists;

import de.tuberlin.uebb.jdae.llmsl.BlockEquation;
import de.tuberlin.uebb.jdae.llmsl.BlockVariable;
import de.tuberlin.uebb.jdae.llmsl.ExecutableDAE;
import de.tuberlin.uebb.jdae.llmsl.ExecutionContext;
import de.tuberlin.uebb.jdae.llmsl.GlobalEquation;
import de.tuberlin.uebb.jdae.llmsl.GlobalVariable;
import de.tuberlin.uebb.jdae.llmsl.IBlock;

/**
 * @author choeger
 * 
 */
public final class LinearGlobalEquation extends GlobalEquation {

    public final double time;
    public final double constant;
    public final double[] coefficients;
    public final List<GlobalVariable> variables;

    public LinearGlobalEquation(double time, double rhs, double[] coefficients,
            List<GlobalVariable> variables) {
        this.time = time;
        this.constant = rhs;
        this.coefficients = coefficients.clone();
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
    public List<GlobalVariable> need() {
        return variables;
    }

    public boolean canSpecializeFor(GlobalVariable v) {
        return variables.contains(v);
    }

    public IBlock specializeFor(final GlobalVariable v, final ExecutableDAE dae) {

        return new IBlock() {
            final int j = need().indexOf(v);

            @Override
            public Iterable<GlobalVariable> variables() {
                return need();
            }

            @Override
            public void exec() {
                double ret = dae.time() * time;
                for (int i = 0; i < coefficients.length; i++)
                    if (i != j)
                        ret += dae.load(need().get(i)) * coefficients[i];

                dae.set(v, (ret - constant) / -coefficients[j]);
            }
        };
    }

    @Override
    public BlockEquation bind(final Map<GlobalVariable, BlockVariable> blockCtxt) {
        final List<BlockVariable> bvars = Lists.transform(variables,
                Functions.forMap(blockCtxt));

        return new BlockEquation() {

            @Override
            public DerivativeStructure exec(ExecutionContext m) {
                DerivativeStructure ret = m.time().multiply(time);
                for (int i = 0; i < bvars.size(); i++)
                    ret = ret.add(bvars.get(i).load(m)
                            .multiply(coefficients[i]));

                return ret.subtract(constant);
            }

        };
    }
}
