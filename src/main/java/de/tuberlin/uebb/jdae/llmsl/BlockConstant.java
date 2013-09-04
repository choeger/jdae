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
package de.tuberlin.uebb.jdae.llmsl;

import java.util.Map;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

import de.tuberlin.uebb.jdae.diff.total.TDNumber;

/**
 * @author choeger
 * 
 */
public final class BlockConstant implements BlockVariable {

    public final static Function<GlobalVariable, BlockVariable> GLOB_CONSTANT = new Function<GlobalVariable, BlockVariable>() {
        public BlockVariable apply(final GlobalVariable v) {
            return new BlockConstant(v);
        }
    };

    public final static Map<GlobalVariable, BlockVariable> globalCtxt(
            Set<GlobalVariable> vars) {
        return Maps.asMap(vars, GLOB_CONSTANT);
    }

    public final GlobalVariable var;

    public String toString() {
        return "(block constant: " + var + ")";
    }

    @Override
    public double value(ExecutionContext ctxt) {
        return ctxt.loadD(var);
    }

    @Override
    public TDNumber load(ExecutionContext ctxt) {
        return ctxt.constant(var);
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
