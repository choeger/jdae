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
package de.tuberlin.uebb.jdae.builtins;

import de.tuberlin.uebb.jdae.dae.Unknown;

/**
 * This is just a simple mockup for unknowns. Usually this class should not be
 * used directly, except for testing purposes.
 * 
 * @author choeger
 */
public final class SimpleDer implements Unknown {

    final String name;

    /*
     * (nicht-Javadoc)
     * 
     * @see de.tuberlin.uebb.jdae.dae.Unknown#isDerivative()
     */
    @Override
    public boolean isDerivative() {
        return true;
    }

    public SimpleDer(String name) {
        super();
        this.name = name;
    }

    @Override
    public String toString() {
        return "der(" + name + ")";
    }

}
