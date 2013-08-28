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

package de.tuberlin.uebb.jdae.diff.total.operations;

import de.tuberlin.uebb.jdae.diff.partial.PDNumber;
import de.tuberlin.uebb.jdae.diff.partial.PDOperations;

public class EmptyComposition implements Composition {

    @Override
    public void compInd(double[] f, PDNumber[] a, PDNumber[] target,
            PDOperations subOps) {
    }

    @Override
    public int countOrder() {
        return -1;
    }

    @Override
    public Composition get(int x) {
        return this;
    }

    @Override
    public CompositionProduct[] value() {
        return new CompositionProduct[0];
    }

    @Override
    public CompositionProduct[] partialDerivative() {
        return new CompositionProduct[0];
    }

}
