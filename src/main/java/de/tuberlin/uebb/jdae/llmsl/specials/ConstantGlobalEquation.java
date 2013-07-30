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

import com.google.common.collect.ImmutableList;

import de.tuberlin.uebb.jdae.diff.total.TDNumber;
import de.tuberlin.uebb.jdae.llmsl.BlockEquation;
import de.tuberlin.uebb.jdae.llmsl.BlockVariable;
import de.tuberlin.uebb.jdae.llmsl.ExecutableDAE;
import de.tuberlin.uebb.jdae.llmsl.ExecutionContext;
import de.tuberlin.uebb.jdae.llmsl.GlobalEquation;
import de.tuberlin.uebb.jdae.llmsl.GlobalVariable;
import de.tuberlin.uebb.jdae.llmsl.IBlock;

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
            public TDNumber exec(ExecutionContext m) {
                final TDNumber load = bvar.load(m);
                return load.subtract(c);
            }
        };
    }

    public String toString() {
        return String.format("%s = %f", var, c);
    }

    public boolean canSpecializeFor(GlobalVariable v) {
        return var.equals(v);
    }

    public IBlock specializeFor(final GlobalVariable v, final ExecutableDAE dae) {

        return new IBlock() {

            @Override
            public Iterable<GlobalVariable> variables() {
                return ImmutableList.of(var);
            }

            @Override
            public void exec() {
                dae.set(var, c);
            }
        };
    }

}
