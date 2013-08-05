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

import de.tuberlin.uebb.jdae.diff.partial.PDNumber;

public final class TDNumber {

    public final PDNumber[] values;
    public final TDOperations ops;

    public TDNumber(int order, int params) {
        this.values = new PDNumber[order + 1];
        this.ops = TDOperations.getInstance(order, params);
        for (int i = 0; i <= order; i++)
            values[i] = new PDNumber(params);
    }

    public TDNumber(PDNumber[] values) {
        super();
        this.values = values;
        this.ops = TDOperations.getInstance(values.length - 1,
                values[0].values.length - 1);
    }

    public TDNumber(TDOperations ops, PDNumber[] values) {
        this.ops = ops;
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

    public TDNumber diff() {
        return new TDNumber(Arrays.copyOfRange(values, 1, values.length));
    }

    public TDNumber antiDiff() {
        return new TDNumber(Arrays.copyOfRange(values, 0, values.length - 1));
    }

    public TDNumber add(final TDNumber other) {
        assert other.values.length == values.length : "Cannot add two numbers of different dimensions!";
        final PDNumber[] newVal = new PDNumber[values.length];
        ops.add(values, other.values, newVal);
        return new TDNumber(newVal);
    }

    public TDNumber add(final double value) {
        final PDNumber[] target = new PDNumber[values.length];
        target[0] = values[0].add(value);
        for (int i = 1; i < target.length; i++)
            target[i] = values[i];
        return new TDNumber(target);
    }

    public TDNumber add(final int value) {
        final PDNumber[] target = new PDNumber[values.length];
        target[0] = values[0].add(value);
        for (int i = 1; i < target.length; i++)
            target[i] = values[i];
        return new TDNumber(target);
    }

    public TDNumber mult(final TDNumber other) {
        final PDNumber[] target = new PDNumber[values.length];
        ops.mult(values, other.values, target);
        return new TDNumber(target);
    }

    public TDNumber mult(final double value) {
        final PDNumber[] target = new PDNumber[values.length];
        for (int i = 0; i < target.length; i++)
            target[i] = values[i].mult(value);
        return new TDNumber(target);
    }

    public TDNumber mult(final int value) {
        final PDNumber[] target = new PDNumber[values.length];
        for (int i = 0; i < target.length; i++)
            target[i] = values[i].mult(value);
        return new TDNumber(target);
    }

    public TDNumber sin() {
        final PDNumber[] target = new PDNumber[values.length];
        ops.sin(values, target);
        return new TDNumber(target);
    }

    public TDNumber cos() {
        final PDNumber[] target = new PDNumber[values.length];
        ops.cos(values, target);
        return new TDNumber(target);
    }

    public TDNumber pow(int n) {
        final PDNumber[] target = new PDNumber[values.length];
        ops.pow(n, values, target);
        return new TDNumber(target);
    }

    public TDNumber pow(double n) {
        final PDNumber[] target = new PDNumber[values.length];
        ops.pow(n, values, target);
        return new TDNumber(target);
    }

    public TDNumber one() {
        return ops.constant(1.0);
    }

    public TDNumber zero() {
        return ops.constant(0.0);
    }

    public String toString() {
        return Arrays.deepToString(values);
    }

    public TDNumber constant(double d) {
        return ops.constant(d);
    }

    public double der(final int dt) {
        return values[dt].values[0];
    }

    public double der(int dt, int idx) {
        return values[dt].values[idx + 1];
    }

    public double getValue() {
        return values[0].values[0];
    }

    public TDNumber subtract(double c) {
        return add(-c);
    }

    public TDNumber subtract(TDNumber o) {
        return add(o.mult(-1));
    }

    public TDNumber copy() {
        final PDNumber[] values = new PDNumber[this.values.length];
        for (int i = 0; i < values.length; ++i)
            values[i] = this.values[i].copy();
        return new TDNumber(values);
    }

}
