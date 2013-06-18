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

package de.tuberlin.uebb.jdae.hlmsl.specials;

import java.util.List;
import java.util.Map;

import com.google.common.base.Functions;
import com.google.common.collect.Lists;

import de.tuberlin.uebb.jdae.hlmsl.Equation;
import de.tuberlin.uebb.jdae.hlmsl.Unknown;
import de.tuberlin.uebb.jdae.llmsl.GlobalEquation;
import de.tuberlin.uebb.jdae.llmsl.GlobalVariable;
import de.tuberlin.uebb.jdae.llmsl.specials.LinearGlobalEquation;

/**
 * An interface for linear equations
 * 
 * @author choeger
 * 
 */
public class ConstantLinear implements Equation {

    public final double timeCoefficient;
    public final double constant;
    public final double[] coefficients;
    public final List<Unknown> variables;

    public ConstantLinear(double timeCoefficient, double constant,
            double[] coefficients, List<Unknown> variables) {
        super();
        this.timeCoefficient = timeCoefficient;
        this.constant = constant;
        this.coefficients = coefficients;
        this.variables = variables;
    }

    /**
     * 
     * @return All unknowns of this linear equation. (Note: The first unknown is
     *         not listed here, as it is the independent variable).
     */
    public List<Unknown> unknowns() {
        return variables;
    }

    @Override
    public GlobalEquation bind(Map<Unknown, GlobalVariable> ctxt) {
        return new LinearGlobalEquation(timeCoefficient, constant,
                coefficients,
                Lists.transform(variables, Functions.forMap(ctxt)));
    }
}
