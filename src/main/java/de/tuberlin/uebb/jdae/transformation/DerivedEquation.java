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

package de.tuberlin.uebb.jdae.transformation;

import java.util.Collection;
import java.util.Set;

import com.google.common.collect.Sets;

import de.tuberlin.uebb.jdae.llmsl.GlobalEquation;
import de.tuberlin.uebb.jdae.llmsl.GlobalVariable;

public final class DerivedEquation {
    public final GlobalEquation eqn;
    public final int minOrder;
    public final int maxOrder;
    private Set<GlobalVariable> set;

    public DerivedEquation(GlobalEquation eqn, int derOrder) {
        super();
        this.eqn = eqn;
        this.maxOrder = derOrder;
        this.minOrder = 0;
    }

    public DerivedEquation(GlobalEquation eqn, int minOrder, int maxOrder) {
        super();
        this.eqn = eqn;
        this.maxOrder = maxOrder;
        this.minOrder = minOrder;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((eqn == null) ? 0 : eqn.hashCode());
        result = prime * result + maxOrder;
        result = prime * result + minOrder;
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
        DerivedEquation other = (DerivedEquation) obj;
        if (eqn == null) {
            if (other.eqn != null)
                return false;
        } else if (!eqn.equals(other.eqn))
            return false;
        if (maxOrder != other.maxOrder)
            return false;
        if (minOrder != other.minOrder)
            return false;
        return true;
    }

    public Collection<GlobalVariable> need() {
        if (set == null) {
            set = Sets.newTreeSet();
            for (int d = 0; d <= (maxOrder - minOrder); d++) {
                for (GlobalVariable v : eqn.need()) {
                    set.add(v.der(d));
                }
            }
        }
        return set;
    }

}