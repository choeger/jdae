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

import com.google.common.collect.ImmutableList;

import de.tuberlin.uebb.jdae.llmsl.BlockEquation;
import de.tuberlin.uebb.jdae.llmsl.BlockVariable;
import de.tuberlin.uebb.jdae.llmsl.ExecutionContext;
import de.tuberlin.uebb.jdae.llmsl.GlobalEquation;
import de.tuberlin.uebb.jdae.llmsl.GlobalVariable;

public final class ConstantGlobalEquation extends GlobalEquation {

    private final double c;
    private final GlobalVariable var;

    public ConstantGlobalEquation(GlobalVariable var, double c) {
        super();
        this.c = c;
        this.var = var;
    }

    @Override
    public List<GlobalVariable> need() {
        return ImmutableList.of(var);
    }

    @Override
    public BlockEquation bind(final Map<GlobalVariable, BlockVariable> blockCtxt) {
        return new BlockEquation() {

            final BlockVariable bvar = blockCtxt.get(var);

            @Override
            public DerivativeStructure exec(ExecutionContext m) {
                return m.load(bvar).subtract(c);
            }
        };
    }

    public String toString() {
        return String.format("%s = %f", var, c);
    }

}
