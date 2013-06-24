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

public final class MutablePDNumber {

    public final double[] values;
    public final PDOperations ops;

    public MutablePDNumber(int size) {
        super();
        this.values = new double[size];
        this.ops = new PDOperations(values.length);
    }

    public MutablePDNumber(double[] values) {
        super();
        this.ops = new PDOperations(values.length);
        this.values = values;
    }

    public PDNumber asNumber() {
        return new PDNumber(values.clone());
    }

    public void add(final double[] other) {
        assert other.length == values.length : "Cannot add two numbers of different dimensions!";
        ops.add(values, other, values);
    }

    public void add(final double value) {
        for (int i = 0; i < values.length; i++)
            values[i] += values[i] + value;
    }

    public void add(final int value) {
        for (int i = 0; i < values.length; i++)
            values[i] += values[i] + value;
    }

    public void mult(final double[] other) {
        assert other.length == values.length : "Cannot multiply two numbers of different dimensions!";
        ops.mult(values, other, values);
    }

    public void mult(final double value) {
        for (int i = 0; i < values.length; i++)
            values[i] *= value;
    }

    public void mult(final int value) {
        for (int i = 0; i < values.length; i++)
            values[i] *= value;
    }

    public void sin() {
        ops.sin(values, values);
    }

    public void cos() {
        ops.cos(values, values);
    }

    public void pow(int n) {
        ops.pow(n, values, values);
    }

    public void pow(double d) {
        ops.pow(d, values, values);
    }

    public void zero() {
        Arrays.fill(values, 0.0);
    }

}
