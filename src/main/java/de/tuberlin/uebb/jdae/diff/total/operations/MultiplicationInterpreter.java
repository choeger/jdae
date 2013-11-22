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

import de.tuberlin.uebb.jbop.optimizer.annotations.ImmutableArray;
import de.tuberlin.uebb.jbop.optimizer.annotations.Optimizable;
import de.tuberlin.uebb.jbop.optimizer.annotations.StrictLoops;
import de.tuberlin.uebb.jdae.utils.IntTriple;

public final class MultiplicationInterpreter implements Multiplication {

    public final Multiplication smaller;

    @ImmutableArray
    public final Product[] value;

    @ImmutableArray
    public final Product[] partialDerivative;

    private final int localOrder;

    public MultiplicationInterpreter(Multiplication smaller, Product[] value,
            Product[] partialDerivative) {
        super();
        this.smaller = smaller == null ? EMPTY_MULTIPLICATION : smaller;
        this.value = merge(value);
        this.partialDerivative = merge(partialDerivative);
        this.localOrder = countOrder();
    }

    @Override
    public int countOrder() {
        return smaller.countOrder() + 1;
    }

    /*
     * (nicht-Javadoc)
     * 
     * @see
     * de.tuberlin.uebb.jdae.diff.total.operations.Multiplication#multInd(de
     * .tuberlin.uebb.jdae.diff.partial.double[],
     * de.tuberlin.uebb.jdae.diff.partial.double[],
     * de.tuberlin.uebb.jdae.diff.partial.double[],
     * de.tuberlin.uebb.jdae.diff.partial.PDOperations)
     */
    @Override
    @Optimizable
    @StrictLoops
    public final void multInd(final double[] a, final double[] b,
            final double[] target, final int width) {

        final int params = width - 1;
        
        if (smaller != EMPTY_MULTIPLICATION)
            smaller.multInd(a, b, target, width);

        evalValue(a, b, target, width);

        for (int j = 1; j <= params; ++j) {
            evalPartialDerivative(a, b, target, width, j);
        }
    }

    @Optimizable
    @StrictLoops
    public final void evalPartialDerivative(final double[] a,
            final double[] b, final double[] target, final int width, final int j) {

        double d = 0;
        for (int p = 0; p < partialDerivative.length; ++p) {  
            final int factor = partialDerivative[p].factor;
            
            if (factor != 0) {
                final int x = partialDerivative[p].key.x;
                final int y = partialDerivative[p].key.y;
                final int z = partialDerivative[p].key.z;            

                d += factor * a[x * width + z * j]
                        * b[y * width + (1 - z) * j];
            }
        }
        target[localOrder*width + j] = d;
    }

    @Optimizable
    @StrictLoops
    public final void evalValue(final double[] a, final double[] b,
            final double[] target, final int width) {

        double d = 0;
        for (int p = 0; p < value.length; ++p) {
            final int factor = value[p].factor;
            if (factor != 0) {
                final int x = value[p].key.x;
                final int y = value[p].key.y;

                d += factor * a[x*width] * b[y*width];
            }       
        }
        target[localOrder*width] = d;
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