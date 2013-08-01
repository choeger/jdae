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
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.ObjectArrays;
import com.google.common.collect.Ordering;

import de.tuberlin.uebb.jdae.diff.partial.PDNumber;
import de.tuberlin.uebb.jdae.diff.partial.PDOperations;

/**
 * @author choeger
 * 
 */
public final class TDOperations {

    public final PDOperations subOps;
    public final int order;
    private final Product[][][] multOps;
    public final CompositionProduct[][][] compOps;

    private TDOperations(int order, int params) {
        super();
        this.order = order;
        this.subOps = new PDOperations(params);
        multOps = compileMultIndirection();
        compOps = compileCompIndirection();
        System.out.println(stats(compOps));
    }

    private String stats(CompositionProduct[][][] compositionProducts) {

        int keys = 0;
        int terms = 0;
        for (CompositionProduct[][] row : compositionProducts)
            for (CompositionProduct[] column : row)
                for (CompositionProduct prod : column) {
                    terms++;
                    keys += prod.key.keys.length;
                }
        return String.format("%d terms with %d keys in total.", terms, keys);

    }

    public static final class IntPair implements Comparable<IntPair> {
        public final int x;
        public final int y;

        public IntPair(int x, int y) {
            super();
            this.x = x;
            this.y = y;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + x;
            result = prime * result + y;
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
            IntPair other = (IntPair) obj;
            if (x != other.x)
                return false;
            if (y != other.y)
                return false;
            return true;
        }

        @Override
        public int compareTo(IntPair o) {
            return ComparisonChain.start().compare(x, o.x).compare(y, o.y)
                    .result();
        }

        public String toString() {
            return "(" + x + ", " + y + ")";
        }

    }

    private final static TIntObjectMap<TIntObjectMap<TDOperations>> instanceCache = new TIntObjectHashMap<TIntObjectMap<TDOperations>>();

    public static TDOperations getInstance(int order, int params) {
        if (!instanceCache.containsKey(order))
            instanceCache.put(order, new TIntObjectHashMap<TDOperations>());

        final TIntObjectMap<TDOperations> map = instanceCache.get(order);

        if (!map.containsKey(params)) {
            final TDOperations ops = new TDOperations(order, params);
            map.put(params, ops);
            return ops;
        } else
            return map.get(params);
    }

    public final void add(final PDNumber[] a, final PDNumber[] b,
            final PDNumber[] target) {
        for (int i = 0; i <= order; i++) {
            if (target[i] == null)
                target[i] = a[i].add(b[i]);
            else {
                for (int j = 0; j < a[i].values.length; ++j)
                    target[i].values[j] = a[i].values[j] + b[i].values[j];
            }

        }
    }

    private static final class ProductElements {
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

    private final static class Product {
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

    private final Product[][][] compileMultIndirection() {
        final Product[][][] multOps = new Product[order + 1][][];
        addMultOps(0, 0, 0, multOps);

        for (int i = 0; i < multOps.length; ++i) {
            for (int j = 0; j < multOps[i].length; ++j) {
                multOps[i][j] = merge(multOps[i][j]);
            }
        }

        return multOps;
    }

    private final void addMultOps(int row, int a, int b, Product[][][] ops) {
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
            addMultOps(row + 1, a + 1, b, ops);
            addMultOps(row + 1, a, b + 1, ops);
        }
    }

    private Product[] merge(Product[] products) {
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

    private static final class CompositionKey {
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

    private final static class CompositionProduct {
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

    private final CompositionProduct[][][] compileCompIndirection() {
        final CompositionProduct[][][] ops = new CompositionProduct[order + 1][][];
        addCompOps(ops);

        for (int i = 0; i < ops.length; ++i) {
            for (int j = 0; j < ops[i].length; ++j) {
                ops[i][j] = merge(ops[i][j]);
            }
        }

        return ops;
    }

    private CompositionProduct[] merge(CompositionProduct[] products) {
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

    private final void addCompOps(CompositionProduct[][][] ops) {
        ops[0] = new CompositionProduct[subOps.params + 1][];
        ops[0][0] = new CompositionProduct[] { new CompositionProduct(0, 1,
                new IntPair[0]) };

        for (int i = 1; i <= subOps.params; ++i) {
            ops[0][i] = new CompositionProduct[] { new CompositionProduct(1, 1,
                    new IntPair[] { new IntPair(0, i) }) };
        }

        if (order > 0) {
            final TDOperations smaller = getInstance(order - 1, subOps.params);

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

    public final void compInd(final double[] f, final PDNumber[] a,
            final PDNumber[] target) {
        for (int i = 0; i < compOps.length; ++i) {
            if (target[i] == null)
                target[i] = new PDNumber(subOps, new double[subOps.params + 1]);

            for (int j = 0; j < compOps[i].length; ++j) {
                target[i].values[j] = 0;
                for (final CompositionProduct p : compOps[i][j]) {
                    double d = p.f_factor * f[p.key.f_order];
                    for (final IntPair k : p.key.keys) {
                        d *= a[k.x].values[k.y];
                    }
                    target[i].values[j] += d;
                }
            }
        }
    }

    public final void multInd(final PDNumber[] a, final PDNumber[] b,
            final PDNumber[] target) {

        for (int i = 0; i < multOps.length; ++i) {
            if (target[i] == null)
                target[i] = new PDNumber(subOps, new double[subOps.params + 1]);

            final Product[][] multOpsRow = multOps[i];

            for (int j = 0; j < multOpsRow.length; ++j) {
                target[i].values[j] = 0;
                for (Product product : multOpsRow[j]) {
                    target[i].values[j] += a[product.elements.lhs_row].values[product.elements.lhs_column]
                            * b[product.elements.rhs_row].values[product.elements.rhs_column]
                            * product.factor;
                }
            }
        }

    }

    public final void mult(final PDNumber[] a, final PDNumber[] b,
            final PDNumber[] target) {
        multInd(a, b, target);
    }

    // public final void mult(final PDNumber[] a, final PDNumber[] b,
    // final PDNumber[] target) {
    // if (target[0] == null)
    // target[0] = new PDNumber(a[0].getParams());
    //
    // final double a0 = a[0].values[0];
    // final double b0 = b[0].values[0];
    //
    // target[0].values[0] = a0 * b0;
    //
    // for (int i1 = 1; i1 <= subOps.params; i1++) {
    // target[0].values[i1] = a[0].values[i1] * b0 + b[0].values[i1] * a0;
    // }
    //
    // if (target.length > 1) {
    // final TDOperations sm = smaller();
    //
    // final PDNumber[] tmp1 = new PDNumber[target.length - 1];
    // sm.mult(diff(a), antiDiff(b), tmp1);
    //
    // final PDNumber[] tmp2 = new PDNumber[target.length - 1];
    // sm.mult(antiDiff(a), diff(b), tmp2);
    //
    // for (int i = 1; i < target.length; i++) {
    // target[i] = new PDNumber(a[0].getParams());
    // target[i].m_add(tmp1[i - 1].values);
    // target[i].m_add(tmp2[i - 1].values);
    // }
    // }
    // }

    public void compose(double f[], final PDNumber[] a, final PDNumber[] target) {
        // compose(f, 0, a, target);
        compInd(f, a, target);
    }

    public void compose(double f[], int order, final PDNumber[] a,
            final PDNumber[] target) {
        if (target[0] == null)
            target[0] = new PDNumber(a[0].getParams());

        target[0].values[0] = f[order];
        for (int i1 = 1; i1 <= subOps.params; i1++) {
            target[0].values[i1] = a[0].values[i1] * f[order + 1];
        }

        if (target.length > 1) {
            final TDOperations sm = smaller();
            final PDNumber[] tmp1 = new PDNumber[target.length - 1];
            sm.compose(f, order + 1, antiDiff(a), tmp1);

            final PDNumber[] tmp2 = new PDNumber[target.length - 1];
            sm.mult(diff(a), tmp1, tmp2);

            for (int i = 1; i < target.length; i++)
                target[i] = tmp2[i - 1];
        }
    }

    public TDOperations smaller() {
        return getInstance(order - 1, subOps.params);
    }

    private PDNumber[] antiDiff(PDNumber[] a) {
        return Arrays.copyOfRange(a, 0, a.length - 1);
    }

    private PDNumber[] diff(PDNumber[] a) {
        return Arrays.copyOfRange(a, 1, a.length);
    }

    public final void sin(final PDNumber[] a, final PDNumber[] target) {
        final double[] f = new double[order + 2];
        f[0] = Math.sin(a[0].values[0]);
        f[1] = Math.cos(a[0].values[0]);
        for (int n = 2; n < order + 2; n++)
            f[n] = -f[n - 2];

        compose(f, a, target);
    }

    public final void cos(final PDNumber[] a, final PDNumber[] target) {
        final double[] f = new double[order + 2];
        f[0] = Math.cos(a[0].values[0]);
        f[1] = -Math.sin(a[0].values[0]);
        for (int n = 2; n < order + 2; n++)
            f[n] = -f[n - 2];

        compose(f, a, target);
    }

    public final void pow(int n, final PDNumber[] a, final PDNumber[] target) {
        // create the power function value and derivatives
        // [x^n, nx^(n-1), n(n-1)x^(n-2), ... ]
        double[] f = new double[order + 2];

        if (n > 0) {
            // strictly positive power
            final int maxOrder = FastMath.min(order + 1, n);
            double xk = FastMath.pow(a[0].values[0], n - maxOrder);
            for (int i = maxOrder; i > 0; --i) {
                f[i] = xk;
                xk *= a[0].values[0];
            }
            f[0] = xk;
        } else {
            // strictly negative power
            final double inv = 1.0 / a[0].values[0];
            double xk = FastMath.pow(inv, -n);
            for (int i = 0; i <= order + 1; ++i) {
                f[i] = xk;
                xk *= inv;
            }
        }

        double coefficient = n;
        for (int i = 1; i <= order + 1; ++i) {
            f[i] *= coefficient;
            coefficient *= n - i;
        }

        compose(f, a, target);
    }

    public final void pow(double n, final PDNumber[] a, final PDNumber[] target) {
        final double[] f = new double[order + 2];
        f[0] = FastMath.pow(a[0].values[0], n);
        f[1] = n * FastMath.pow(a[0].values[0], n - 1);
        compose(f, a, target);
    }

    public TDNumber constantVar(int offset, double... dt) {
        final PDNumber c = subOps.constant(dt[offset]);
        final PDNumber[] c_values = new PDNumber[order + 1];
        c_values[0] = c;

        for (int i = 1; i < Math.min(dt.length - offset, order + 1); ++i)
            c_values[i] = subOps.constant(dt[i + offset]);

        return new TDNumber(c_values);
    }

    public TDNumber constant(double d, double... dt) {
        final PDNumber c = subOps.constant(d);
        final PDNumber[] c_values = new PDNumber[order + 1];
        c_values[0] = c;

        for (int i = 0; i < Math.min(dt.length, order); ++i)
            c_values[i + 1] = subOps.constant(dt[i]);

        for (int i = dt.length; i < order; ++i)
            c_values[i + 1] = subOps.constant(0.0);

        return new TDNumber(c_values);
    }

    public TDNumber variable(int idx, double... der) {
        return variable(idx, 0, der);
    }

    public TDNumber variable(int idx, int derivatives, double... der) {
        final PDNumber[] vals = new PDNumber[order + 1];
        for (int i = 0; i < order + 1; i++)
            vals[i] = subOps.constant(der[i]);

        if (subOps.params > 0)
            for (int i = 0; i <= derivatives; i++)
                vals[i].values[1 + idx + i] = 1;

        return new TDNumber(vals);
    }

    public String toString() {
        return String.format("TD with %d parameters, %d-times derived",
                subOps.params, order);
    }

    public static String compStr(CompositionProduct[] compositionProducts) {
        return Joiner.on(" + ").join(compositionProducts).toString();
    }

}
