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

/**
 * @author choeger
 * 
 */
public final class PDNumber {

    public final double[] values;
    public final PDOperations ops;

    public PDNumber(double[] values) {
        super();
        this.ops = new PDOperations(values.length);
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
        PDNumber other = (PDNumber) obj;
        if (!Arrays.equals(values, other.values))
            return false;
        return true;
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
        final double[] target = new double[values.length];
        for (int i = 0; i < target.length; i++)
            target[i] = values[i] + value;
        return new PDNumber(target);
    }

    public PDNumber add(final int value) {
        final double[] target = new double[values.length];
        for (int i = 0; i < target.length; i++)
            target[i] = values[i] + value;
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
}
