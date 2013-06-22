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

import org.apache.commons.math3.analysis.differentiation.DerivativeStructure;

public final class BlockIteratee implements BlockVariable {

    public final int blockIndex;
    public final GlobalVariable var;

    @Override
    public double value(ExecutionContext ctxt) {
        return ctxt.loadD(var);
    }

    public BlockIteratee(GlobalVariable var, int blockIndex) {
        super();
        this.blockIndex = blockIndex;
        this.var = var;
    }

    @Override
    public DerivativeStructure load(ExecutionContext ctxt) {
        final double[] number = ctxt.allocate();
        for (int i = 0; i <= ctxt.order; i++) {
            assert ctxt.data[var.index].length > var.der + i : String.format(
                    "%d-th derivative of %s is not allocated!", i, var);

            ctxt.setDt(i, ctxt.data[var.index][var.der + i], number);

            assert ctxt.params[blockIndex + i].index == var.index : String
                    .format("%d-th derivative of %s is not iteratee of this block!",
                            i, var);

            ctxt.setDer(i, blockIndex + i, number);
        }

        return ctxt.build(number);
    }

    @Override
    public BlockVariable der(ExecutionContext ctxt) {
        return der(ctxt, 1);
    }

    @Override
    public BlockVariable der(ExecutionContext ctxt, int n) {
        if (n == 0)
            return this;

        final int idx = blockIndex + n;

        assert ctxt.params[idx].index == var.index : String.format(
                "%d-th derivative of %s is not part of this block!", n, var);
        return new BlockIteratee(ctxt.params[idx], idx);
    }

    @Override
    public GlobalVariable global() {
        return var;
    }
}
