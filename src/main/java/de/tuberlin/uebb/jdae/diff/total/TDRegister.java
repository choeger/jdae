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

public final class TDRegister {

    private final TDOperations ops;
    private int current;
    private final TDNumber[] numbers = new TDNumber[2];

    public TDRegister(final TDNumber initial) {
        this.numbers[0] = initial;
        this.current = 0;
        this.numbers[1] = new TDNumber(initial.ops);
        this.ops = initial.ops;
    }

    public final TDNumber unsafe() {
        return numbers[current];
    }

    public final TDNumber get() {
        return numbers[current].copy();
    }

    public final void add(int n) {
        numbers[current].values[0].values[0] += n;
    }

    public final void add(double d) {
        numbers[current].values[0].values[0] += d;
    }

    public final void add(TDNumber o) {
        final int next = (current + 1) % 2;
        ops.add(numbers[current].values, o.values, numbers[next].values);
        current = next;
    }

    public final void mult(TDNumber o) {
        final int next = (current + 1) % 2;
        ops.mult(numbers[current].values, o.values, numbers[next].values);
        current = next;
    }

    public final void pow(final int p) {
        final int next = (current + 1) % 2;
        ops.pow(p, numbers[current].values, numbers[next].values);
        current = next;
    }

    public void mult(int n) {
        for (int i = 0; i < numbers[current].values.length; ++i)
            for (int j = 0; j < numbers[current].values[i].values.length; ++j)
                numbers[current].values[i].values[j] *= n;

    }

    public void square() {
        mult(numbers[current]);
    }
}
