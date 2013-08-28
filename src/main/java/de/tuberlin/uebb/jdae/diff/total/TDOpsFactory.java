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
package de.tuberlin.uebb.jdae.diff.total;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.ObjectArrays;

import de.tuberlin.uebb.jdae.diff.partial.PDOperations;
import de.tuberlin.uebb.jdae.diff.total.operations.Composition;
import de.tuberlin.uebb.jdae.diff.total.operations.CompositionInterpreter;
import de.tuberlin.uebb.jdae.diff.total.operations.CompositionProduct;
import de.tuberlin.uebb.jdae.diff.total.operations.Multiplication;
import de.tuberlin.uebb.jdae.diff.total.operations.MultiplicationInterpreter;
import de.tuberlin.uebb.jdae.diff.total.operations.Product;
import de.tuberlin.uebb.jdae.utils.IntPair;

public final class TDOpsFactory {
    public static final TDOperations NO_OPS = new TDEmptyOperations();

    private final static TIntObjectMap<TIntObjectMap<TDOperations>> instanceCache = new TIntObjectHashMap<TIntObjectMap<TDOperations>>();

    public static TDOperations getInstance(int order, int params) {
        if (!instanceCache.containsKey(order))
            instanceCache.put(order, new TIntObjectHashMap<TDOperations>());

        final TIntObjectMap<TDOperations> map = instanceCache.get(order);

        if (!map.containsKey(params)) {
            final PDOperations subOps = new PDOperations(params);

            final Composition compOps = getComposition(order);
            final Multiplication multiplication = getMultiplication(order);

            final TDInterpreter ops = new TDInterpreter(order, subOps,
                    multiplication, compOps, order > 0 ? getInstance(order - 1,
                            params) : NO_OPS);

            map.put(params, ops);
        }

        return map.get(params);
    }

    private final static TIntObjectMap<MultiplicationInterpreter> multCache = new TIntObjectHashMap<MultiplicationInterpreter>();

    public static MultiplicationInterpreter getMultiplication(final int order) {
        if (!multCache.containsKey(order)) {
            multCache.put(order, compileMultIndirection(order));
        }
        return multCache.get(order);
    }

    private static final MultiplicationInterpreter compileMultIndirection(
            final int order) {

        if (order > 0) {
            final MultiplicationInterpreter smaller = getMultiplication(order - 1);

            /* D|[a*b]| = D(a) * I(b) + I(a) * D(b) */

            final List<Product> value = Lists.newArrayList();
            for (Product p : smaller.value) {
                /* D(a) * I(b) */
                value.add(new Product(p.key.x + 1, p.key.y, false, p.factor));

                /* I(a) * D(b) */
                value.add(new Product(p.key.x, p.key.y + 1, false, p.factor));
            }

            final List<Product> der = Lists.newArrayList();
            for (Product p : smaller.partialDerivative) {
                /* D(a) * I(b) */
                der.add(new Product(p.key.x + 1, p.key.y, p.key.z == 1,
                        p.factor));

                /* I(a) * D(b) */
                der.add(new Product(p.key.x, p.key.y + 1, p.key.z == 1,
                        p.factor));
            }

            return new MultiplicationInterpreter(smaller,
                    value.toArray(new Product[value.size()]),
                    der.toArray(new Product[der.size()]));

        } else {
            // |[a*b]|(0,0) == a[0] * b[0]
            final Product[] value = new Product[] { new Product(0, 0, false, 1) };

            // (a*b)[i] == a[0] * der(b)[i] + der(a)[i] * b[0]
            final Product[] derivatives = new Product[] {
                    new Product(0, 0, true, 1), new Product(0, 0, false, 1) };

            return new MultiplicationInterpreter(null, value, derivatives);
        }
    }

    private final static TIntObjectMap<CompositionInterpreter> compCache = new TIntObjectHashMap<CompositionInterpreter>();

    public static Composition getComposition(final int order) {
        if (!compCache.containsKey(order)) {
            compCache.put(order, compileCompIndirection(order));
        }
        return compCache.get(order);
    }

    private static final CompositionInterpreter compileCompIndirection(int order) {

        if (order > 0) {

            /* D(|[f ° a]|) = |[f' ° I(a)]| * D(a) */

            final Composition subCompOps = getComposition(order - 1);
            final MultiplicationInterpreter multOps = getMultiplication(order - 1);

            final CompositionProduct[][] p = new CompositionProduct[2][];

            for (int j = 0; j < 2; ++j) {
                final Product[] sum = j == 0 ? multOps.value
                        : multOps.partialDerivative;

                final List<CompositionProduct> newComp = Lists.newArrayList();

                for (Product product : sum) {

                    final CompositionProduct[] lhs;
                    final IntPair a_i;

                    if (product.key.z == 1) {
                        /* |[f' ° I(a)]|(x, col) * D(a)(y, 0) */
                        lhs = subCompOps.get(product.key.x).partialDerivative();
                        a_i = new IntPair(product.key.y + 1, 0);
                    } else {
                        /* |[f' ° I(a)]|(x, 0) * D(a)(y, col) */
                        lhs = subCompOps.get(product.key.x).value();
                        a_i = new IntPair(product.key.y + 1, 1);
                    }

                    for (CompositionProduct l : lhs) {
                        final IntPair[] newProduct = ObjectArrays.concat(
                                l.key.keys, new IntPair[] { a_i },
                                IntPair.class);

                        newComp.add(new CompositionProduct(l.key.f_order + 1,
                                l.f_factor * product.factor, newProduct));
                    }

                }

                p[j] = newComp.toArray(new CompositionProduct[newComp.size()]);
            }

            return new CompositionInterpreter(subCompOps, p[0], p[1]);
        } else {
            /* |[f ° a]|(0,0) = f(a(0,0)) */
            final CompositionProduct[] values = new CompositionProduct[] { new CompositionProduct(
                    0, 1, new IntPair[0]) };

            /* |[f ° a]|(0,i) = f'(a(0,0)) * a(0,i) */
            final CompositionProduct[] partials = new CompositionProduct[] { new CompositionProduct(
                    1, 1, new IntPair[] { new IntPair(0, 1) }) };

            return new CompositionInterpreter(null, values, partials);
        }
    }
}
