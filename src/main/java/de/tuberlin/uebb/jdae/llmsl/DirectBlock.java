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

import com.google.common.collect.ImmutableList;

/**
 * This is an @link{IBlock} that solves for exactly one variable by direct
 * computation.
 */
public abstract class DirectBlock implements IBlock {

    public final GlobalVariable v;
    private final IBlock alternative;
    private final ExecutableDAE dae;

    public DirectBlock(GlobalVariable v, IBlock alt, ExecutableDAE dae) {
        this.v = v;
        this.alternative = alt;
        this.dae = dae;
    }

    public void exec() {
        final double val = computeValue();

        if (Double.isNaN(val)) {
            alternative.exec();
            System.out.println("Corrected to: " + dae.load(v));
        } else {
            dae.set(v, val);
        }
    }

    public abstract double computeValue();

    public final Iterable<GlobalVariable> variables() {
        return ImmutableList.of(v);
    }

}
