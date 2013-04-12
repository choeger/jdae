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

package de.tuberlin.uebb.jdae.dae;

import java.util.List;

/**
 * An interface for linear equations
 * 
 * @author choeger
 * 
 */
public interface ConstantLinear extends Equation {

    /**
     * 
     * @return All unknowns of this linear equation. (Note: The first unknown is
     *         not listed here, as it is the independent variable).
     */
    public List<Unknown> unknowns();

    /**
     * 
     * @return All but the first coefficient of this linear equation in order
     */
    public double[] coefficients();

    /**
     * 
     * @return The first left hand side coefficient of this linear equation
     */
    public double timeCoefficient();

    /**
     * 
     * @return The right hand side of this linear equation
     */
    public double constant();
}
