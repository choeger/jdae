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

import org.apache.commons.math3.util.FastMath;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.ObjectArrays;
import com.google.common.collect.Ordering;

import de.tuberlin.uebb.jdae.diff.partial.PDNumber;
import de.tuberlin.uebb.jdae.diff.partial.PDOperations;
import de.tuberlin.uebb.jdae.utils.IntPair;

public final class TDOpsFactory {
    private final static TIntObjectMap<TIntObjectMap<TDInterpreter>> instanceCache = 
	new TIntObjectHashMap<TIntObjectMap<TDInterpreter>>();

    public static TDInterpreter getInstance(int order, int params) {
        if (!instanceCache.containsKey(order))
            instanceCache.put(order, new TIntObjectHashMap<TDInterpreter>());

        final TIntObjectMap<TDInterpreter> map = instanceCache.get(order);

        if (!map.containsKey(params)) {
	    final PDOperations subOps = new PDOperations(params);
            final TDInterpreter ops = new TDInterpreter(order, params, compileMultIndirection(order, subOps), compileCompIndirection(order, subOps));
            map.put(params, ops);
            return ops;
        } else
            return map.get(params);
    }

   static final class ProductElements {
        public final int lhs_row;
        public final int lhs_column;
        public final int rhs_row;
        public final int rhs_column;

        public ProductElements(int lhs_row, int lhs_column, int rhs_row,
                int rhs_column) {
            super();
            this.lhs_row = lhs_row;
            this.lhs_column = lhs_column;
            this.rhs_row = rhs_row;
            this.rhs_column = rhs_column;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + lhs_column;
            result = prime * result + lhs_row;
            result = prime * result + rhs_column;
            result = prime * result + rhs_row;
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
            ProductElements other = (ProductElements) obj;
            if (lhs_column != other.lhs_column)
                return false;
            if (lhs_row != other.lhs_row)
                return false;
            if (rhs_column != other.rhs_column)
                return false;
            if (rhs_row != other.rhs_row)
                return false;
            return true;
        }

    }

    final static class Product {
        public final ProductElements elements;
        public final int factor;

        public Product(int lhs_row, int lhs_column, int rhs_row,
                int rhs_column, int factor) {
            super();
            this.elements = new ProductElements(lhs_row, lhs_column, rhs_row,
                    rhs_column);
            this.factor = factor;
        }

        public Product(int factor, ProductElements elements) {
            this.factor = factor;
            this.elements = elements;
        }

        public Product plusOne() {
            return new Product(factor + 1, elements);
        }
    }

    private static final Product[][][] compileMultIndirection(final int order,
							      final PDOperations subOps
							      ) {
        final Product[][][] multOps = new Product[order + 1][][];
        addMultOps(order, subOps, 0, 0, 0, multOps);

        for (int i = 0; i < multOps.length; ++i) {
            for (int j = 0; j < multOps[i].length; ++j) {
                multOps[i][j] = merge(multOps[i][j]);
            }
        }

        return multOps;
    }

    private static final void addMultOps(int order, final PDOperations subOps,
					 int row, int a, int b, Product[][][] ops) {
        if (ops[row] == null) {
            ops[row] = new Product[subOps.params + 1][];
        }

        // (a*b)[0] == a[0] * b[0]
        final Product[] sum = new Product[] { new Product(a, 0, b, 0, 1) };
        if (ops[row][0] == null) {
            ops[row][0] = sum;
        } else {
            ops[row][0] = ObjectArrays.concat(sum, ops[row][0], Product.class);
        }

        // (a*b)[i] == a[0] * der(b)[i] + der(a)[i] * b[0]
        for (int i = 1; i <= subOps.params; ++i) {
            final Product[] sum2 = new Product[] { new Product(a, 0, b, i, 1),
                    new Product(a, i, b, 0, 1) };
            if (ops[row][i] == null) {
                ops[row][i] = sum2;
            } else {
                ops[row][i] = ObjectArrays.concat(sum2, ops[row][i],
                        Product.class);
            }
        }

        if (row < order) {
            addMultOps(order, subOps, row + 1, a + 1, b, ops);
            addMultOps(order, subOps, row + 1, a, b + 1, ops);
        }
    }

    private static Product[] merge(Product[] products) {
        final Map<ProductElements, Product> map = Maps.newHashMap();

        for (Product product : products) {
            if (map.containsKey(product.elements))
                map.put(product.elements, map.get(product.elements).plusOne());
            else
                map.put(product.elements, product);
        }

        final Product[] array = map.values().toArray(new Product[map.size()]);
        return array;
    }

    static final class CompositionKey {
        public final int f_order;
        public final IntPair[] keys;

        public CompositionKey(int f_order, IntPair[] keys) {
            super();
            this.f_order = f_order;
            this.keys = Arrays.copyOf(keys, keys.length);
            Arrays.sort(this.keys, Ordering.natural());
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
                b.append("a_");
                b.append(k);
                b.append(" ");
            }
            b.append("]");
            return b.toString();
        }
    }

    final static class CompositionProduct {
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
    }

    private static final CompositionProduct[][][] compileCompIndirection(int order, PDOperations subOps) {
        final CompositionProduct[][][] ops = new CompositionProduct[order + 1][][];
        addCompOps(order, subOps, ops);

        for (int i = 0; i < ops.length; ++i) {
            for (int j = 0; j < ops[i].length; ++j) {
                ops[i][j] = merge(ops[i][j]);
            }
        }

        return ops;
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
        return array;
    }

    private static final void addCompOps(final int order, 
					 final PDOperations subOps, 
					 CompositionProduct[][][] ops) {
        ops[0] = new CompositionProduct[subOps.params + 1][];
        ops[0][0] = new CompositionProduct[] { new CompositionProduct(0, 1,
                new IntPair[0]) };

        for (int i = 1; i <= subOps.params; ++i) {
            ops[0][i] = new CompositionProduct[] { new CompositionProduct(1, 1,
                    new IntPair[] { new IntPair(0, i) }) };
        }

        if (order > 0) {
            final TDInterpreter smaller = getInstance(order - 1, subOps.params);

            final CompositionProduct[][][] subCompOps = smaller.compOps;
            final Product[][][] multOps = smaller.multOps;

            for (int i = 0; i < subCompOps.length; ++i) {
                ops[i + 1] = new CompositionProduct[subOps.params + 1][];

                for (int j = 0; j < subCompOps[i].length; ++j) {
                    final Product[] sum = multOps[i][j];
                    final List<CompositionProduct> newComp = Lists
                            .newArrayList();
                    for (Product product : sum) {
                        final CompositionProduct[] lhs = subCompOps[product.elements.lhs_row][product.elements.lhs_column];
                        final int a_i = product.elements.rhs_row + 1;
                        final int a_j = product.elements.rhs_column;

                        for (CompositionProduct p : lhs) {
                            newComp.add(new CompositionProduct(
                                    p.key.f_order + 1, p.f_factor
                                            * product.factor, ObjectArrays
                                            .concat(new IntPair(a_i, a_j),
                                                    p.key.keys)));
                        }
                    }

                    ops[i + 1][j] = newComp
                            .toArray(new CompositionProduct[newComp.size()]);
                }
            }
        }
    }


}
