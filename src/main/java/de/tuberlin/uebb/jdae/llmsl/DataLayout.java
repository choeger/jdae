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

package de.tuberlin.uebb.jdae.llmsl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

public final class DataLayout implements Iterable<GlobalVariable> {

    public static final class VariableRow {
        public final int number;
        public final int derOrder;
        public final String name;

        public VariableRow(int number, int derOrder, String name) {
            super();
            this.number = number;
            this.derOrder = derOrder;
            this.name = name;
        }
    }

    private final class VariableIterator implements Iterator<GlobalVariable> {

        private int index = 0;
        private int der = 0;

        @Override
        public boolean hasNext() {
            return (index < rows.length - 1) || (der < rows[index].derOrder);
        }

        @Override
        public GlobalVariable next() {
            final GlobalVariable v = new GlobalVariable(rows[index].name,
                    index + 1, der);

            if (der < rows[index].derOrder)
                der++;
            else {
                index++;
                der = 0;
            }

            return v;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

    }

    public final VariableRow[] rows;

    public double[][] alloc() {
        double[][] data = new double[rows.length + 1][];
        data[0] = new double[1];

        for (int i = 0; i < rows.length; i++) {
            data[i + 1] = new double[rows[i].derOrder + 1];
        }

        return data;
    }

    public DataLayout(final VariableRow[] rows) {
        this.rows = Arrays.copyOf(rows, rows.length);
    }

    public DataLayout(int size, final Collection<GlobalVariable> vars) {
        String[] names = new String[size];
        int[] der = new int[size];
        this.rows = new VariableRow[size];

        for (GlobalVariable v : vars) {
            der[v.index - 1] = Math.max(der[v.index - 1], v.der);
            names[v.index - 1] = v.name;
        }

        for (int i = 0; i < der.length; i++) {
            rows[i] = new VariableRow(i + 1, der[i], names[i]);
        }

    }

    @Override
    public Iterator<GlobalVariable> iterator() {
        return new VariableIterator();
    }
}
