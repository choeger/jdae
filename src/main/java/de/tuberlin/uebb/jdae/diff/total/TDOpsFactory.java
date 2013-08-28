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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.ObjectArrays;

import de.tuberlin.uebb.jbop.optimizer.annotations.Optimizable;
import de.tuberlin.uebb.jbop.optimizer.annotations.StrictLoops;
import de.tuberlin.uebb.jdae.diff.partial.PDNumber;
import de.tuberlin.uebb.jdae.diff.partial.PDOperations;
import de.tuberlin.uebb.jdae.utils.IntPair;
import de.tuberlin.uebb.jdae.utils.IntTriple;

public final class TDOpsFactory {
    public static final TDOperations NO_OPS = new TDEmptyOperations();

    private final static TIntObjectMap<TIntObjectMap<TDOperations>> instanceCache = new TIntObjectHashMap<TIntObjectMap<TDOperations>>();

    public static TDOperations getInstance(int order, int params) {
        if (!instanceCache.containsKey(order))
            instanceCache.put(order, new TIntObjectHashMap<TDOperations>());

        final TIntObjectMap<TDOperations> map = instanceCache.get(order);

        if (!map.containsKey(params)) {
            final PDOperations subOps = new PDOperations(params);

            final CompositionOperation compOps = compileCompIndirection(subOps,
                    order);
            final TDInterpreter ops = new TDInterpreter(order, subOps,
                    compileMultIndirection(order, subOps), compOps,
                    order > 0 ? getInstance(order - 1, params) : NO_OPS);
            map.put(params, ops);
        }

        return map.get(params);
    }

    public final static class Product {
        public final int factor;
        public final IntTriple key;

        public Product(int lhs, int rhs, boolean aDer, int factor) {
            super();
            this.key = new IntTriple(lhs, rhs, aDer ? 1 : 0);
            this.factor = factor;
        }

        protected Product(final IntTriple key, int factor) {
            this.key = key;
            this.factor = factor;
        }

        public Product plusOne() {
            return new Product(key, factor + 1);
        }

        public String toString() {
            if (key.z == 1) {
                return String.format("%d * a[%d][i] * b[%d][0]", factor, key.x,
                        key.y);
            } else
                return String.format("%d * a[%d][0] * b[%d][i]", factor, key.x,
                        key.y);
        }

        public double eval(final int col, final PDNumber[] a, final PDNumber[] b) {
            return factor * a[key.x].values[key.z * col]
                    * b[key.y].values[(1 - key.z) * col];
        }
    }

    private static final MultiplicationOperations compileMultIndirection(
            final int order, final PDOperations subOps) {

        if (order > 0) {
            final MultiplicationOperations smaller = getInstance(order - 1,
                    subOps.params).multOps();

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

            return new MultiplicationOperations(smaller,
                    value.toArray(new Product[value.size()]),
                    der.toArray(new Product[der.size()]));

        } else {
            // |[a*b]|(0,0) == a[0] * b[0]
            final Product[] value = new Product[] { new Product(0, 0, false, 1) };

            // (a*b)[i] == a[0] * der(b)[i] + der(a)[i] * b[0]
            final Product[] derivatives = new Product[] {
                    new Product(0, 0, true, 1), new Product(0, 0, false, 1) };

            return new MultiplicationOperations(null, value, derivatives);
        }
    }

    public static final class MultiplicationOperations {

        public final MultiplicationOperations smaller;
        public final Product[] value;
        public final Product[] partialDerivative;

        private final int order;

        public MultiplicationOperations(MultiplicationOperations smaller,
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

        @Optimizable
        @StrictLoops
        public final void multInd(final PDNumber[] a, final PDNumber[] b,
                final PDNumber[] target, PDOperations subOps) {

            if (smaller != null)
                smaller.multInd(a, b, target, subOps);

            if (target[order] == null)
                target[order] = new PDNumber(subOps,
                        new double[subOps.params + 1]);

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

            final Product[] array = map.values().toArray(
                    new Product[map.size()]);
            return array;
        }
    }

    public static final class CompositionKey {
        public final int f_order;
        public final IntPair[] keys;

        public CompositionKey(int f_order, IntPair[] keys) {
            super();
            this.f_order = f_order;
            this.keys = Arrays.copyOf(keys, keys.length);
            Arrays.sort(this.keys);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + f_order;
            result = prime * result + Arrays.hashCode(keys);
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            CompositionKey other = (CompositionKey) obj;
            if (f_order != other.f_order)
                return false;
            if (!Arrays.equals(keys, other.keys))
                return false;
            return true;
        }

        @Override
        public String toString() {
            final StringBuilder b = new StringBuilder();
            b.append("f^(" + f_order + ") * P[");
            for (IntPair k : keys) {
                b.append("a_(");
                b.append(k.x);
                b.append(", ");
                b.append(k.y == 0 ? "0" : "i");
                b.append(") ");
            }
            b.append("]");
            return b.toString();
        }
    }

    public final static class CompositionProduct {
        public final int f_factor;
        public final CompositionKey key;

        public CompositionProduct(int f_factor, CompositionKey key) {
            this.key = key;
            this.f_factor = f_factor;
        }

        public CompositionProduct(int f_order, int f_factor, IntPair[] keys) {
            super();
            this.key = new CompositionKey(f_order, keys);
            this.f_factor = f_factor;
        }

        public CompositionProduct plus(CompositionProduct o) {
            return new CompositionProduct(f_factor + o.f_factor, key);
        }

        public String toString() {
            return "( " + f_factor + " * " + key + ")";
        }

        public double apply(final int col, final PDNumber[] a, final double[] f) {
            double d = f_factor * f[key.f_order];
            for (IntPair k : key.keys)
                d *= a[k.x].values[col * k.y];
            return d;
        }
    }

    public final static class CompositionOperation {
        public final CompositionOperation smaller;

        private final CompositionProduct[] value;
        private final CompositionProduct[] partialDerivative;

        private final int order;

        public CompositionOperation(CompositionOperation smaller,
                CompositionProduct[] value,
                CompositionProduct[] partialDerivative) {
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
            final Map<CompositionKey, CompositionProduct> map = Maps
                    .newHashMap();

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

        @Optimizable
        @StrictLoops
        public final void compInd(final double[] f, final PDNumber[] a,
                final PDNumber[] target, final PDOperations subOps) {
            if (smaller != null)
                smaller.compInd(f, a, target, subOps);

            if (target[order] == null)
                target[order] = new PDNumber(subOps,
                        new double[subOps.params + 1]);

            target[order].values[0] = 0;
            for (int k = 0; k < value.length; ++k) {
                target[order].values[0] += value[k].apply(0, a, f);
            }

            for (int j = 1; j <= subOps.params; ++j) {
                target[order].values[j] = 0;
                for (int k = 0; k < partialDerivative.length; ++k) {
                    target[order].values[j] += partialDerivative[k].apply(j, a,
                            f);
                }
            }
        }

        public CompositionOperation get(int x) {
            if (x == order)
                return this;
            else
                return smaller.get(x);
        }
    }

    private static final CompositionOperation compileCompIndirection(
            final PDOperations subOps, int order) {

        if (order > 0) {
            final TDOperations smaller = getInstance(order - 1, subOps.params);

            /* D(|[f ° a]|) = |[f' ° I(a)]| * D(a) */

            final CompositionOperation subCompOps = smaller.compOps();
            final MultiplicationOperations multOps = smaller.multOps();

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
                        lhs = subCompOps.get(product.key.x).partialDerivative;
                        a_i = new IntPair(product.key.y + 1, 0);
                    } else {
                        /* |[f' ° I(a)]|(x, 0) * D(a)(y, col) */
                        lhs = subCompOps.get(product.key.x).value;
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

            return new CompositionOperation(subCompOps, p[0], p[1]);
        } else {
            /* |[f ° a]|(0,0) = f(a(0,0)) */
            final CompositionProduct[] values = new CompositionProduct[] { new CompositionProduct(
                    0, 1, new IntPair[0]) };

            /* |[f ° a]|(0,i) = f'(a(0,0)) * a(0,i) */
            final CompositionProduct[] partials = new CompositionProduct[] { new CompositionProduct(
                    1, 1, new IntPair[] { new IntPair(0, 1) }) };

            return new CompositionOperation(null, values, partials);
        }
    }
}
