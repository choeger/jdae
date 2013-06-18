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

package de.tuberlin.uebb.jdae.hlmsl;

import com.google.common.collect.ComparisonChain;

/**
 * @author choeger
 * 
 */
public final class Unknown implements Comparable<Unknown> {

    public final String name;
    public final int nr;
    public final int der;

    public Unknown(String name, int nr, int der) {
        super();
        this.name = name;
        this.nr = nr;
        this.der = der;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + der;
        result = prime * result + nr;
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
        Unknown other = (Unknown) obj;
        if (der != other.der)
            return false;
        if (nr != other.nr)
            return false;
        return true;
    }

    public String toString() {
        if (der == 0)
            return name;
        else
            return String.format("d^%d%s", der, name, der);
    }

    public Unknown der() {
        return new Unknown(name, nr, der + 1);
    }

    @Override
    public int compareTo(Unknown o) {
        return ComparisonChain.start().compare(der, o.der).compare(nr, o.nr)
                .result();
    }

    public Unknown der(int d) {
        if (d == 0)
            return this;
        else
            return new Unknown(name, nr, d + der);
    }

    public Unknown base() {
        if (der == 0)
            return this;
        else
            return new Unknown(name, nr, 0);
    }

}
