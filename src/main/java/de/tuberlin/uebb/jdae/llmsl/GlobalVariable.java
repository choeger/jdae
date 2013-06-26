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

import com.google.common.collect.ComparisonChain;

public final class GlobalVariable implements Comparable<GlobalVariable> {

    public final String name;
    public final int index;
    public final int der;

    public GlobalVariable(String name, int index, int der) {
        super();
        this.name = name;
        if (index >= 1) {
            if (der >= 0) {
                this.index = index;
                this.der = der;
            } else {
                throw new IllegalArgumentException(
                        "Derivative order needs to be positive. Got: " + der);
            }
        } else {
            throw new IllegalArgumentException(
                    "Variable index needs to be greater than 0. Got: " + index);
        }

    }

    public GlobalVariable der(int i) {
        if (i == 0)
            return this;
        return new GlobalVariable(name, index, der + i);
    }

    public GlobalVariable der() {
        return der(1);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + der;
        result = prime * result + index;
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
        GlobalVariable other = (GlobalVariable) obj;
        if (der != other.der)
            return false;
        if (index != other.index)
            return false;
        return true;
    }

    /**
     * @return true, iff this is <br>
     *         exactly</br> one of the provided variables
     */
    public boolean isOneOf(GlobalVariable... vars) {
        for (int i = 0; i < vars.length; i++)
            if (equals(vars[i]))
                return noneOf(i + 1, vars);
        return false;
    }

    private boolean noneOf(int start, GlobalVariable... vars) {
        for (int i = start; i < vars.length; i++)
            if (equals(vars[i]))
                return false;
        return true;
    }

    @Override
    public int compareTo(GlobalVariable that) {
        return ComparisonChain.start().compare(index, that.index)
                .compare(der, that.der).result();
    }

    public GlobalVariable integrate() {
        return new GlobalVariable(name, index, der - 1);
    }

    public String toString() {
        if (der == 0)
            return name;
        else
            return String.format("der^%d(%s)", der, name);
    }

    public GlobalVariable base() {
        if (der == 0)
            return this;
        else
            return new GlobalVariable(name, index, 0);
    }

}
