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

public final class BlockVariable {

    public static final BlockVariable TIME = new BlockVariable(0, 0, 0);

    public final int[] relativeDerivatives;
    public final int absoluteIndex;
    public final int derivationOrder;
    public final int relativeIndex;

    public String toString() {
        if (derivationOrder == 0)
            return String.format("x_(%d => %d)", absoluteIndex, relativeIndex);
        else
            return String.format("x_(%d => %d) / dt^%d", absoluteIndex,
                    relativeIndex, derivationOrder);
    }

    public BlockVariable(int absoluteIndex, int derivationOrder,
            int relativeIndex, int... relativeDerivatives) {
        super();
        this.relativeDerivatives = relativeDerivatives;
        this.absoluteIndex = absoluteIndex;
        this.derivationOrder = derivationOrder;
        this.relativeIndex = relativeIndex;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + absoluteIndex;
        result = prime * result + derivationOrder;
        result = prime * result + relativeIndex;
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
        BlockVariable other = (BlockVariable) obj;
        if (absoluteIndex != other.absoluteIndex)
            return false;
        if (derivationOrder != other.derivationOrder)
            return false;
        if (relativeIndex != other.relativeIndex)
            return false;
        return true;
    }

    public final GlobalVariable global(DataLayout layout) {
        return new GlobalVariable(layout.rows[absoluteIndex - 1].name,
                absoluteIndex, derivationOrder);
    }

}
