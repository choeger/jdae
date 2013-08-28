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

import java.util.Map;

import com.google.common.collect.Maps;

import de.tuberlin.uebb.jbop.optimizer.annotations.Optimizable;
import de.tuberlin.uebb.jbop.optimizer.annotations.StrictLoops;
import de.tuberlin.uebb.jdae.diff.partial.PDNumber;
import de.tuberlin.uebb.jdae.diff.partial.PDOperations;
import de.tuberlin.uebb.jdae.utils.IntTriple;

public final class MultiplicationInterpreter implements Multiplication {

    public final MultiplicationInterpreter smaller;
    public final Product[] value;
    public final Product[] partialDerivative;

    private final int order;

    public MultiplicationInterpreter(MultiplicationInterpreter smaller,
            Product[] value, Product[] partialDerivative) {
        super();
        this.smaller = smaller;
        this.value = merge(value);
        this.partialDerivative = merge(partialDerivative);
        this.order = countOrder();
    }

    private int countOrder() {
        if (smaller == null)
            return 0;
        else
            return smaller.countOrder() + 1;
    }

    /*
     * (nicht-Javadoc)
     * 
     * @see
     * de.tuberlin.uebb.jdae.diff.total.operations.Multiplication#multInd(de
     * .tuberlin.uebb.jdae.diff.partial.PDNumber[],
     * de.tuberlin.uebb.jdae.diff.partial.PDNumber[],
     * de.tuberlin.uebb.jdae.diff.partial.PDNumber[],
     * de.tuberlin.uebb.jdae.diff.partial.PDOperations)
     */
    @Override
    @Optimizable
    @StrictLoops
    public final void multInd(final PDNumber[] a, final PDNumber[] b,
            final PDNumber[] target, PDOperations subOps) {

        if (smaller != null)
            smaller.multInd(a, b, target, subOps);

        if (target[order] == null)
            target[order] = new PDNumber(subOps, new double[subOps.params + 1]);

        target[order].values[0] = 0;

        for (Product product : value) {
            target[order].values[0] += product.eval(0, a, b);
        }

        for (int j = 1; j <= subOps.params; ++j) {
            target[order].values[j] = 0;
            for (Product product : partialDerivative) {
                target[order].values[j] += product.eval(j, a, b);
            }
        }
    }

    private static Product[] merge(Product[] products) {
        final Map<IntTriple, Product> map = Maps.newHashMap();

        for (Product product : products) {
            if (map.containsKey(product.key))
                map.put(product.key, map.get(product.key).plusOne());
            else
                map.put(product.key, product);
        }

        final Product[] array = map.values().toArray(new Product[map.size()]);
        return array;
    }
}