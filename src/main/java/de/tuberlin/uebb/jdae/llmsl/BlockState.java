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

import de.tuberlin.uebb.jdae.diff.total.TDNumber;

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
    public TDNumber load(ExecutionContext ctxt) {
        final TDNumber number = ctxt.constant(var);
        final int diff = ctxt.params[firstDerivative].der - var.der;

        for (int i = diff; i <= ctxt.order; i++) {
            final int relativeDerivative = firstDerivative + i - diff;
            assert ctxt.params[relativeDerivative].index == var.index : String
                    .format("%d-th derivative of %s is not iteratee of this block!",
                            i, var);
            number.values[i * number.width + relativeDerivative + 1] = 1.0;
        }

        return number;
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
