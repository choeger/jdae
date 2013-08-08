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
package de.tuberlin.uebb.jdae.llmsl.events;

import de.tuberlin.uebb.jdae.llmsl.ExecutableDAE;

/**
 * @author choeger
 * 
 */
public abstract class DiscreteModification implements EventEffect {

    public abstract void modify();

    /*
     * (nicht-Javadoc)
     * 
     * @see
     * de.tuberlin.uebb.jdae.llmsl.events.EventEffect#apply(de.tuberlin.uebb
     * .jdae.llmsl.ExecutableDAE)
     */
    @Override
    public ExecutableDAE apply(ExecutableDAE source) {

        return source;
    }

}
