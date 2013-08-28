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

public final class CompositionInterpreter implements Composition {
    public final CompositionInterpreter smaller;

    public final CompositionProduct[] value;
    public final CompositionProduct[] partialDerivative;

    private final int order;

    public CompositionInterpreter(CompositionInterpreter smaller,
            CompositionProduct[] value, CompositionProduct[] partialDerivative) {
        super();
        this.smaller = smaller;
        this.order = countOrder();
        this.value = merge(value);
        this.partialDerivative = merge(partialDerivative);
    }

    private int countOrder() {
        if (smaller == null)
            return 0;
        else
            return smaller.order + 1;
    }

    private static CompositionProduct[] merge(CompositionProduct[] products) {
        final Map<CompositionKey, CompositionProduct> map = Maps.newHashMap();

        for (CompositionProduct product : products) {
            if (map.containsKey(product.key)) {
                map.put(product.key, map.get(product.key).plus(product));
            } else
                map.put(product.key, product);
        }

        final CompositionProduct[] array = map.values().toArray(
                new CompositionProduct[map.size()]);
        System.out.println("Compressed  " + products.length
                + " products down to " + array.length);
        return array;
    }

    /* (nicht-Javadoc)
     * @see de.tuberlin.uebb.jdae.diff.total.operations.Composition#compInd(double[], de.tuberlin.uebb.jdae.diff.partial.PDNumber[], de.tuberlin.uebb.jdae.diff.partial.PDNumber[], de.tuberlin.uebb.jdae.diff.partial.PDOperations)
     */
    @Override
    @Optimizable
    @StrictLoops
    public final void compInd(final double[] f, final PDNumber[] a,
            final PDNumber[] target, final PDOperations subOps) {
        if (smaller != null)
            smaller.compInd(f, a, target, subOps);

        if (target[order] == null)
            target[order] = new PDNumber(subOps, new double[subOps.params + 1]);

        target[order].values[0] = 0;
        for (int k = 0; k < value.length; ++k) {
            target[order].values[0] += value[k].apply(0, a, f);
        }

        for (int j = 1; j <= subOps.params; ++j) {
            target[order].values[j] = 0;
            for (int k = 0; k < partialDerivative.length; ++k) {
                target[order].values[j] += partialDerivative[k].apply(j, a, f);
            }
        }
    }

    public CompositionInterpreter get(int x) {
        if (x == order)
            return this;
        else
            return smaller.get(x);
    }
}