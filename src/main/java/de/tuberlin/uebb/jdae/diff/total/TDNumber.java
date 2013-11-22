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

import java.util.Arrays;

public final class TDNumber {

    public final double[] values; 
    public final int width;
    public final TDOperations ops;

    public TDNumber(int order, int params) {
        this.values = new double[(order + 1) * (params + 1)];
        this.width = params+1;
        this.ops = TDOpsFactory.getInstance(order, params);        
    }

    public TDNumber(final int order, double[] values) {
        super();
        this.values = values;
        this.width = values.length / (order + 1);
        this.ops = TDOpsFactory.getInstance(order,
                width -1);
    }

    public TDNumber(TDOperations ops, double[] values) {
        this.ops = ops;
        this.width = values.length / (ops.order() + 1);
        this.values = values;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(values);
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
        TDNumber other = (TDNumber) obj;
        if (!Arrays.equals(values, other.values))
            return false;
        return true;
    }

    public TDNumber add(final TDNumber other) {
        assert other.values.length == values.length : "Cannot add two numbers of different dimensions!";
        final double[] newVal = new double[values.length];
        ops.add(values, other.values, newVal);
        return new TDNumber(ops, newVal);
    }

    public TDNumber add(final double value) {
        final double[] target = new double[values.length];
        target[0] = values[0] + value;
        for (int i = 1; i < target.length; i++)
            target[i] = values[i];
        return new TDNumber(ops, target);
    }

    public TDNumber add(final int value) {
        final double[] target = Arrays.copyOf(values, values.length);
        target[0] = values[0] + value;
        return new TDNumber(ops, target);
    }

    public TDNumber mult(final TDNumber other) {
        final double[] target = new double[values.length];
        ops.mult(values, other.values, target);
        return new TDNumber(ops, target);
    }

    public TDNumber mult(final double value) {
        final double[] target = new double[values.length];
        for (int i = 0; i < target.length; i++)
            target[i] = values[i] * value;
        return new TDNumber(ops, target);
    }

    public TDNumber mult(final int value) {
        final double[] target = new double[values.length];
        for (int i = 0; i < target.length; i++)
            target[i] = values[i] * value;
        return new TDNumber(ops, target);
    }

    public TDNumber sin() {
        final double[] target = new double[values.length];
        ops.sin(values, target);
        return new TDNumber(ops, target);
    }

    public TDNumber cos() {
        final double[] target = new double[values.length];
        ops.cos(values, target);
        return new TDNumber(ops, target);
    }

    public TDNumber pow(int n) {
        final double[] target = new double[values.length];
        ops.pow(n, values, target);
        return new TDNumber(ops, target);
    }

    public TDNumber pow(double n) {
        final double[] target = new double[values.length];
        ops.pow(n, values, target);
        return new TDNumber(ops, target);
    }

    public TDNumber one() {
        return ops.constant(1.0);
    }

    public TDNumber zero() {
        return ops.constant(0.0);
    }

    public String toString() {
        return Arrays.toString(values);
    }

    public TDNumber constant(double d) {
        return ops.constant(d);
    }

    public double der(final int dt) {
        return values[dt * width];
    }

    public double der(int dt, int idx) {
        return values[dt * width + (idx+1)];
    }

    public double getValue() {
        return values[0];
    }

    public TDNumber subtract(double c) {
        return add(-c);
    }

    public TDNumber subtract(TDNumber o) {
        return add(o.mult(-1));
    }

    public TDNumber copy() {
        return new TDNumber(ops, Arrays.copyOf(values, values.length));
    }

}
