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
package de.tuberlin.uebb.jdae.diff.partial;

import java.util.Arrays;

import de.tuberlin.uebb.jdae.diff.total.TDNumber;

/**
 * A number holding not just the result of a computation, but also its partial
 * derivatives.
 * 
 * @author choeger
 * 
 */
public final class PDNumber {

    public final double[] values;
    public final PDOperations ops;

    public PDNumber(double[] values) {
        super();
        this.ops = new PDOperations(values.length - 1);
        this.values = values;
    }

    public PDNumber(int params) {
        this.ops = new PDOperations(params);
        this.values = new double[params + 1];
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
        PDNumber other = (PDNumber) obj;
        if (!Arrays.equals(values, other.values))
            return false;
        return true;
    }

    public TDNumber cons(final TDNumber other) {
        final PDNumber[] vals = new PDNumber[other.values.length + 1];
        vals[0] = this;
        for (int i = 1; i < vals.length; i++)
            vals[i] = other.values[i - 1];
        return new TDNumber(vals);
    }

    public PDNumber compose(double[] f, int order) {
        final PDNumber ret = new PDNumber(getParams());
        ops.compose(f[order], f[order + 1], values, ret.values);
        return ret;
    }

    public String toString() {
        return Arrays.toString(values);
    }

    public PDNumber add(final PDNumber other) {
        assert other.values.length == values.length : "Cannot add two numbers of different dimensions!";
        final double[] newVal = new double[values.length];
        ops.add(values, other.values, newVal);
        return new PDNumber(newVal);
    }

    public PDNumber add(final double value) {
        final double[] target = values.clone();
        target[0] += value;
        return new PDNumber(target);
    }

    public PDNumber add(final int value) {
        final double[] target = values.clone();
        target[0] += value;
        return new PDNumber(target);
    }

    public PDNumber mult(final PDNumber other) {
        final double[] otherVal = other.values;
        assert otherVal.length == values.length : "Cannot multiply two numbers of different dimensions!";
        final double[] newVal = new double[values.length];

        ops.mult(values, otherVal, newVal);
        return new PDNumber(newVal);
    }

    public PDNumber mult(final double value) {
        final double[] target = new double[values.length];
        for (int i = 0; i < target.length; i++)
            target[i] = values[i] * value;
        return new PDNumber(target);
    }

    public PDNumber mult(final int value) {
        final double[] target = new double[values.length];
        for (int i = 0; i < target.length; i++)
            target[i] = values[i] * value;
        return new PDNumber(target);
    }

    public PDNumber sin() {
        final double[] target = new double[values.length];
        ops.sin(values, target);
        return new PDNumber(target);
    }

    public PDNumber cos() {
        final double[] target = new double[values.length];
        ops.cos(values, target);
        return new PDNumber(target);
    }

    public PDNumber pow(int n) {
        final double[] target = new double[values.length];
        ops.pow(n, values, target);
        return new PDNumber(target);
    }

    public PDNumber pow(double d) {
        final double[] target = new double[values.length];
        ops.pow(d, values, target);
        return new PDNumber(target);
    }

    public double der(int i) {
        return values[i + 1];
    }

    public void m_add(final double[] other) {
        assert other.length == values.length : "Cannot add two numbers of different dimensions!";
        ops.add(values, other, values);
    }

    public void m_add(final double value) {
        values[0] += value;
    }

    public void m_add(final int value) {
        values[0] += value;
    }

    public void m_mult(final double[] other) {
        assert other.length == values.length : "Cannot multiply two numbers of different dimensions!";
        ops.mult(values, other, values);
    }

    public void m_mult(final double value) {
        for (int i = 0; i < values.length; i++)
            values[i] *= value;
    }

    public void m_mult(final int value) {
        for (int i = 0; i < values.length; i++)
            values[i] *= value;
    }

    public void m_sin() {
        ops.sin(values, values);
    }

    public void m_cos() {
        ops.cos(values, values);
    }

    public void m_pow(int n) {
        ops.pow(n, values, values);
    }

    public void m_pow(double d) {
        ops.pow(d, values, values);
    }

    public PDNumber zero() {
        return new PDNumber(new double[values.length]);
    }

    public PDNumber one() {
        final double[] values = new double[this.values.length];
        values[0] = 1.0;
        return new PDNumber(values);
    }

    public int getParams() {
        return values.length - 1;
    }

    public PDNumber copy() {
        return new PDNumber(Arrays.copyOf(this.values, this.values.length));
    }

}
